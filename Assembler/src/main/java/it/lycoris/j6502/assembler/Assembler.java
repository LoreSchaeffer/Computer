package it.lycoris.j6502.assembler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core Assembler class responsible for translating Lyco-8 Assembly source code
 * into 6502 machine code binaries through a Two-Pass compilation process.
 */
public class Assembler {
    private static final Logger LOG = LoggerFactory.getLogger(Assembler.class);
    private final AddressingAnalyzer analyzer = new AddressingAnalyzer();
    private final InstructionSet instructionSet = new InstructionSet();
    private final Map<String, Integer> symbolTable = new HashMap<>();
    private int pc = 0x0000;

    /**
     * Pass 1: Scans the token stream to build the Symbol Table.
     * Evaluates Labels, Origin directives (* = $XXXX), and Equates (NAME = $XX).
     *
     * @param tokens The list of parsed TokenLines from the lexer.
     */
    public void pass1(List<TokenLine> tokens) {
        LOG.info("--- Starting Pass 1: Symbol Resolution ---");

        for (TokenLine token : tokens) {
            // Register standard labels
            if (token.label() != null) {
                String cleanLabel = token.label().replace(":", "");
                this.symbolTable.put(cleanLabel, this.pc);
                LOG.debug("Label mapped: {} -> ${}", cleanLabel, String.format("%04X", this.pc));
            }

            if (token.mnemonic() != null) {
                // Intercept Origin Directives
                if (token.mnemonic().equals("*")) {
                    this.pc = this.parseHexValue(token.operand());
                    LOG.debug("Origin Directive (*): PC shifted to ${}", String.format("%04X", this.pc));
                    continue;
                }

                // Intercept Constant Definitions (Equates) e.g., "KEYBOARD_ADDR =$4000"
                if (token.operand() != null && token.operand().trim().startsWith("=")) {
                    int constantValue = this.parseHexValue(token.operand());
                    this.symbolTable.put(token.mnemonic(), constantValue);
                    LOG.debug("Constant mapped: {} = ${}", token.mnemonic(), String.format("%04X", constantValue));
                    continue;
                }

                // Evaluate standard instruction length to increment the Program Counter
                AddressingMode mode = this.analyzer.determineMode(token.mnemonic(), token.operand());
                int len = mode.getInstructionLength();

                this.pc += len;
            }
        }

        LOG.info("Pass 1 completed. Symbol Table populated with {} entries", this.symbolTable.size());
    }

    /**
     * Pass 2: Generates the actual machine code binary using the resolved Symbol Table.
     *
     * @param tokens     The list of parsed TokenLines.
     * @param outputFile The target binary file to write.
     * @throws IOException If file writing fails.
     */
    public void pass2(List<TokenLine> tokens, File outputFile) throws IOException {
        LOG.info("--- Starting Pass 2: Code Generation ---");

        this.pc = 0x0000;

        ByteArrayOutputStream binOut = new ByteArrayOutputStream();

        for (TokenLine token : tokens) {
            if (token.mnemonic() == null) continue;

            // Execute Origin Directive
            if (token.mnemonic().equals("*")) {
                this.pc = this.parseHexValue(token.operand());
                continue;
            }

            // Skip Constant Definitions (they generate no machine code)
            if (token.operand() != null && token.operand().trim().startsWith("=")) continue;

            AddressingMode mode = this.analyzer.determineMode(token.mnemonic(), token.operand());
            Integer opcode = this.instructionSet.getOpcode(token.mnemonic(), mode);

            if (opcode == null) {
                throw new IllegalStateException(String.format(
                        "Syntax error at line %d: Unknown instruction or invalid addressing mode '%s %s'",
                        token.lineNumber(),
                        token.mnemonic(),
                        token.operand() != null ? token.operand() : ""
                ));
            }

            // Write the Opcode
            binOut.write(opcode);
            this.pc += mode.getInstructionLength();

            // Resolve and write Operands
            if (mode != AddressingMode.IMPLIED && mode != AddressingMode.ACCUMULATOR) {
                int operandValue = this.resolveOperandValue(token.operand());

                if (mode == AddressingMode.RELATIVE) {
                    int offset = operandValue - this.pc;
                    if (offset < -128 || offset > 127) {
                        throw new IllegalStateException(String.format(
                                "Branch target out of range at line %d: %s",
                                token.lineNumber(),
                                token.operand()
                        ));
                    }
                    binOut.write(offset & 0xFF);
                } else if (mode.getInstructionLength() == 2) {
                    binOut.write(operandValue & 0xFF); // Single byte payload
                } else if (mode.getInstructionLength() == 3) {
                    binOut.write(operandValue & 0xFF);         // Low byte (Little-Endian)
                    binOut.write((operandValue >> 8) & 0xFF);  // High byte
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            binOut.writeTo(fos);
        }

        LOG.info("Pass 2 completed. Binary file generated ({} bytes): {}", binOut.size(), outputFile.getAbsolutePath());
    }

    /**
     * Parses a hexadecimal string value, cleaning it of directives and formatting.
     *
     * @param operand The string representing the hex value (e.g., "=$8000" or "$4000").
     * @return The parsed integer value.
     */
    private int parseHexValue(String operand) {
        if (operand == null) return 0;

        String cleanHex = operand.replace("=", "").replace("$", "").trim();

        try {
            return Integer.parseInt(cleanHex, 16);
        } catch (NumberFormatException e) {
            LOG.error("Invalid syntax for hex value: {}", operand);
            throw new IllegalArgumentException("Invalid hex value syntax: " + operand, e);
        }
    }

    /**
     * Resolves the final integer value of an operand, looking up labels and constants
     * in the Symbol Table while safely stripping addressing mode modifiers.
     *
     * @param operand The raw operand string (e.g., "#COLOR_BLUE", "$4000", "(ZP_PTR_LO),Y").
     * @return The resolved integer value.
     */
    private int resolveOperandValue(String operand) {
        if (operand == null) return 0;

        // Direct hex address or value bypassing the symbol table
        if (operand.contains("$")) {
            String hexOnly = operand.replaceAll("[^0-9A-Fa-f]", "");
            return Integer.parseInt(hexOnly, 16);
        }

        // It is a label or a constant.
        // We must strip modifiers: Immediate '#', Indexed ',X' or ',Y', and Indirect Parentheses '()'.
        String symbolToResolve = operand
                .replace("#", "")
                .replaceAll("(?i),[xy]", "")
                .replace("(", "")
                .replace(")", "")
                .trim();

        if (this.symbolTable.containsKey(symbolToResolve)) {
            return this.symbolTable.get(symbolToResolve);
        }

        throw new IllegalArgumentException("Symbol not resolved or missing label/constant: " + symbolToResolve);
    }
}
