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
    private final DirectiveParser directiveParser = new DirectiveParser();
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
            if (token.label() != null) {
                String cleanLabel = token.label().replace(":", "");
                this.symbolTable.put(cleanLabel, this.pc);
            }

            if (token.mnemonic() != null) {
                if (token.mnemonic().equals("*")) {
                    this.pc = this.parseHexValue(token.operand());
                    continue;
                }

                if (token.operand() != null && token.operand().trim().startsWith("=")) {
                    int constantValue = this.parseHexValue(token.operand());
                    this.symbolTable.put(token.mnemonic(), constantValue);
                    continue;
                }

                if (token.mnemonic().startsWith(".")) {
                    if (token.mnemonic().equals(".BYTE")) {
                        List<Byte> bytes = this.directiveParser.parseByteDirective(token.operand());
                        this.pc += bytes.size();
                    }
                    continue;
                }

                AddressingMode mode = this.analyzer.determineMode(token.mnemonic(), token.operand());
                this.pc += mode.getInstructionLength();
            }
        }
        LOG.info("Pass 1 completed.");
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

            if (token.mnemonic().equals("*")) {
                this.pc = this.parseHexValue(token.operand());
                continue;
            }

            if (token.operand() != null && token.operand().trim().startsWith("=")) continue;

            if (token.mnemonic().startsWith(".")) {
                if (token.mnemonic().equals(".BYTE")) {
                    List<Byte> bytes = this.directiveParser.parseByteDirective(token.operand());
                    for (Byte rawByte : bytes) {
                        binOut.write(rawByte & 0xFF);
                    }
                    this.pc += bytes.size();
                }
                continue;
            }

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

            binOut.write(opcode);
            this.pc += mode.getInstructionLength();

            if (mode != AddressingMode.IMPLIED && mode != AddressingMode.ACCUMULATOR) {
                int operandValue = this.resolveOperandValue(token.operand());

                if (mode == AddressingMode.RELATIVE) {
                    int offset = operandValue - this.pc;

                    // Hardware limitation of MOS 6502: Relative branches are limited to 8-bit signed integers.
                    if (offset < -128 || offset > 127) {
                        String errorMessage = String.format(
                                "Hardware limitation hit at line %d: Branch target '%s' is out of range. " +
                                        "Distance is %+d bytes, but the 6502 relative addressing limit is -128 to +127 bytes. " +
                                        "Consider refactoring your assembly using a JMP trampoline (Branch Inversion).",
                                token.lineNumber(),
                                token.operand(),
                                offset
                        );
                        throw new IllegalStateException(errorMessage);
                    }
                    binOut.write(offset & 0xFF);
                } else if (mode.getInstructionLength() == 2) {
                    binOut.write(operandValue & 0xFF);
                } else if (mode.getInstructionLength() == 3) {
                    binOut.write(operandValue & 0xFF);
                    binOut.write((operandValue >> 8) & 0xFF);
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            binOut.writeTo(fos);
        }
        LOG.info("Pass 2 completed.");
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
     * It also evaluates simple arithmetic expressions (addition and subtraction).
     *
     * @param operand The raw operand string (e.g., "#COLOR_BLUE", "$4000", "(PPU_BASE + 1),Y").
     * @return The resolved integer value.
     */
    private int resolveOperandValue(String operand) {
        if (operand == null) {
            return 0;
        }

        // 1. Strip addressing mode modifiers
        String cleanOperand = operand.replace("#", "")
                .replaceAll("(?i),[xy]", "")
                .replace("(", "")
                .replace(")", "")
                .trim();

        // 2. Evaluate the resulting expression
        return this.evaluateExpression(cleanOperand);
    }

    /**
     * Evaluates simple arithmetic expressions within the operand.
     * Currently supports addition (+) and subtraction (-).
     *
     * @param expression The mathematical expression or single token to evaluate.
     * @return The computed integer result.
     */
    private int evaluateExpression(String expression) {
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            int sum = 0;
            for (String part : parts) {
                sum += this.resolveSingleToken(part.trim());
            }
            return sum;
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
            int result = this.resolveSingleToken(parts[0].trim());
            for (int i = 1; i < parts.length; i++) {
                result -= this.resolveSingleToken(parts[i].trim());
            }
            return result;
        }

        // If no operators are found, resolve it as a single token
        return this.resolveSingleToken(expression);
    }

    /**
     * Resolves a single isolated token into its integer representation.
     * The token can be a hexadecimal literal, a decimal literal, or a known symbol.
     *
     * @param token The isolated string token.
     * @return The integer value of the token.
     * @throws IllegalArgumentException if the token is malformed or missing from the symbol table.
     */
    private int resolveSingleToken(String token) {
        if (token.isEmpty()) {
            return 0;
        }

        // Handle hexadecimal literals (e.g., $2000)
        if (token.startsWith("$")) {
            String hexOnly = token.substring(1).replaceAll("[^0-9A-Fa-f]", "");
            return Integer.parseInt(hexOnly, 16);
        }

        // Handle binary literals (e.g., %10101010)
        if (token.startsWith("%")) {
            String binOnly = token.substring(1).replaceAll("[^01]", "");
            return Integer.parseInt(binOnly, 2);
        }

        // Handle standard decimal literals
        if (token.matches("\\d+")) {
            return Integer.parseInt(token);
        }

        // Handle Symbol Table lookup
        if (this.symbolTable.containsKey(token)) {
            return this.symbolTable.get(token);
        }

        throw new IllegalArgumentException("Symbol not resolved or missing label/constant: '" + token + "'");
    }
}
