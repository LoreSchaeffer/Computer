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

public class Assembler {
    private static final Logger LOG = LoggerFactory.getLogger(Assembler.class);
    private final AddressingAnalyzer analyzer = new AddressingAnalyzer();
    private final InstructionSet instructionSet = new InstructionSet();
    private final Map<String, Integer> symbolTable = new HashMap<>();
    private int pc = 0x0000;

    public void pass1(List<TokenLine> tokens) {
        LOG.info("--- Starting Pass 1: Symbol Resolution ---");

        for (TokenLine token : tokens) {
            if (token.label() != null) {
                String cleanLabel = token.label().replace(":", "");
                symbolTable.put(cleanLabel, pc);
                LOG.debug("Label found: {} -> ${}", cleanLabel, Integer.toHexString(pc).toUpperCase());
            }

            if (token.mnemonic() != null) {
                if (token.mnemonic().equals("*")) {
                    pc = parseOriginDirective(token.operand());
                    LOG.debug("Origin Directive (*): PC moved to ${}", Integer.toHexString(pc).toUpperCase());
                    continue;
                }

                AddressingMode mode = analyzer.determineMode(token.mnemonic(), token.operand());
                int len = mode.getInstructionLength();

                pc += len;
            }
        }

        LOG.info("Pass 1 completed. Symbol Table contains {} entries", symbolTable.size());
    }

    public void pass2(List<TokenLine> tokens, File outputFile) throws IOException {
        LOG.info("--- Starting Pass 2: Code Generation ---");

        pc = 0x0000;

        ByteArrayOutputStream binOut = new ByteArrayOutputStream();
        for (TokenLine token : tokens) {
            if (token.mnemonic() == null) continue;

            if (token.mnemonic().equals("*")) {
                pc = parseOriginDirective(token.operand());
                continue;
            }

            AddressingMode mode = analyzer.determineMode(token.mnemonic(), token.operand());
            Integer opcode = instructionSet.getOpcode(token.mnemonic(), mode);

            if (opcode == null) throw new IllegalStateException(String.format(
                    "Syntax error at line %d: Unknown instruction or invalid addressing mode '%s %s'",
                    token.lineNumber(),
                    token.mnemonic(),
                    token.operand() != null ? token.operand() : ""
            ));

            binOut.write(opcode);
            pc += mode.getInstructionLength();

            if (mode != AddressingMode.IMPLIED && mode != AddressingMode.ACCUMULATOR) {
                int operandValue = resolveOperandValue(token.operand());

                if (mode == AddressingMode.RELATIVE) {
                    int offset = operandValue - pc;
                    if (offset < -128 || offset > 127) throw new IllegalStateException(String.format(
                            "Branch target out of range at line %d: %s",
                            token.lineNumber(),
                            token.operand()
                    ));

                    binOut.write(offset & 0xFF);
                } else if (mode.getInstructionLength() == 2) {
                    binOut.write(operandValue & 0xFF);
                } else if (mode.getInstructionLength() == 3) {
                    binOut.write(operandValue & 0xFF); // Low byte
                    binOut.write((operandValue >> 8) & 0xFF); // High byte
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            binOut.writeTo(fos);
        }

        LOG.info("Pass 2 completed. Binary file generated ({} bytes): {}", binOut.size(), outputFile.getAbsolutePath());
    }

    private int parseOriginDirective(String operand) {
        if (operand == null) return 0;

        String cleanHex = operand.replace("=", "").replace("$", "").trim();

        try {
            return Integer.parseInt(cleanHex, 16);
        } catch (NumberFormatException e) {
            LOG.error("Invalid syntax for Origin Directive: {}", operand);
            throw new IllegalArgumentException("Invalid Origin Directive syntax");
        }
    }

    private int resolveOperandValue(String operand) {
        if (operand == null) return 0;

        if (operand.contains("$")) {
            String hexOnly = operand.replaceAll("[^0-9A-Fa-f]", "");
            return Integer.parseInt(hexOnly, 16);
        }

        String label = operand.replaceAll(",[XxYy]", "").trim();

        if (symbolTable.containsKey(label)) return symbolTable.get(label);

        throw new IllegalArgumentException("Symbol not resolved or missing label: " + label);
    }
}
