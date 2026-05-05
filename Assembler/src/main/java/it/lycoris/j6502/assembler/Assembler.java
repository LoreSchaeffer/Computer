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

    private int programCounter = 0x0000;

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
                this.symbolTable.put(cleanLabel, this.programCounter);
            }

            if (token.mnemonic() != null) {
                if (token.mnemonic().equals("*")) {
                    this.programCounter = this.parseHexValue(token.operand(), token);
                    continue;
                }

                if (token.operand() != null && token.operand().trim().startsWith("=")) {
                    int constantValue = this.parseHexValue(token.operand(), token);
                    this.symbolTable.put(token.mnemonic(), constantValue);
                    continue;
                }

                if (token.mnemonic().startsWith(".")) {
                    if (token.mnemonic().equals(".BYTE")) {
                        // FIX: Passing the symbol table context to the parser
                        List<Byte> bytes = this.directiveParser.parseByteDirective(token.operand(), this.symbolTable);
                        this.programCounter += bytes.size();
                    }
                    continue;
                }

                AddressingMode mode = this.analyzer.determineMode(token.mnemonic(), token.operand(), this.symbolTable);
                this.programCounter += mode.getInstructionLength();
            }
        }

        LOG.info("Pass 1 completed. Symbol table holds {} entries.", this.symbolTable.size());
    }

    /**
     * Pass 2: Generates the actual machine code binary using the resolved Symbol Table.
     * Implements binary padding for discrete Origin directives.
     *
     * @param tokens     The list of parsed TokenLines.
     * @param outputFile The target binary file to write.
     * @throws IOException If file writing fails.
     */
    public void pass2(List<TokenLine> tokens, File outputFile) throws IOException {
        LOG.info("--- Starting Pass 2: Code Generation ---");

        this.programCounter = 0x0000;
        int initialBaseAddress = -1;

        ByteArrayOutputStream binaryOutput = new ByteArrayOutputStream();

        for (TokenLine token : tokens) {
            if (token.mnemonic() == null) continue;

            // Origin Directive Handler with Binary Padding logic
            if (token.mnemonic().equals("*")) {
                int targetAddress = this.parseHexValue(token.operand(), token);

                if (initialBaseAddress == -1) {
                    initialBaseAddress = targetAddress;
                    this.programCounter = targetAddress;
                } else {
                    if (targetAddress > this.programCounter) {
                        int paddingSize = targetAddress - this.programCounter;
                        for (int i = 0; i < paddingSize; i++) {
                            binaryOutput.write(0x00);
                        }
                        this.programCounter = targetAddress;
                    } else if (targetAddress < this.programCounter) {
                        throw new IllegalStateException("Memory overlap: Origin directive attempts to move the Program Counter backwards.");
                    }
                }
                continue;
            }

            if (token.operand() != null && token.operand().trim().startsWith("=")) continue;

            if (token.mnemonic().startsWith(".")) {
                if (token.mnemonic().equals(".BYTE")) {
                    // FIX: Passing the symbol table context to the parser
                    List<Byte> bytes = this.directiveParser.parseByteDirective(token.operand(), this.symbolTable);
                    for (Byte rawByte : bytes) {
                        binaryOutput.write(rawByte & 0xFF);
                    }
                    this.programCounter += bytes.size();
                }
                continue;
            }

            AddressingMode mode = this.analyzer.determineMode(token.mnemonic(), token.operand(), this.symbolTable);
            Integer opcode = this.instructionSet.getOpcode(token.mnemonic(), mode);

            if (opcode == null) {
                throw new IllegalStateException(String.format(
                        "Syntax error at line %d: Unknown instruction or invalid addressing mode '%s %s'",
                        token.lineNumber(),
                        token.mnemonic(),
                        token.operand() != null ? token.operand() : ""
                ));
            }

            binaryOutput.write(opcode);
            this.programCounter += mode.getInstructionLength();

            if (mode != AddressingMode.IMPLIED && mode != AddressingMode.ACCUMULATOR) {
                int operandValue = this.resolveOperandValue(token);

                if (mode == AddressingMode.RELATIVE) {
                    int offset = operandValue - this.programCounter;

                    if (offset < -128 || offset > 127) throw new IllegalStateException(String.format("Branch target '%s' is out of range at line %d.", token.operand(), token.lineNumber()));

                    binaryOutput.write(offset & 0xFF);

                } else if (mode.getInstructionLength() == 2) {
                    binaryOutput.write(operandValue & 0xFF);
                } else if (mode.getInstructionLength() == 3) {
                    binaryOutput.write(operandValue & 0xFF);         // Little-Endian Low Byte
                    binaryOutput.write((operandValue >> 8) & 0xFF);  // Little-Endian High Byte
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            binaryOutput.writeTo(outputStream);
        }

        LOG.info("Pass 2 completed. Binary generated successfully.");
    }

    private int parseHexValue(String operand, TokenLine token) {
        if (operand == null) return 0;

        String cleanHex = operand.replace("=", "").replace("$", "").trim();

        try {
            return Integer.parseInt(cleanHex, 16);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(String.format("Invalid hex value syntax at line %d: '%s'", token.lineNumber(), operand), exception);
        }
    }

    private int resolveOperandValue(TokenLine token) {
        if (token.operand() == null) return 0;

        String cleanOperand = token.operand().replace("#", "")
                .replaceAll("(?i),[xy]", "")
                .replace("(", "")
                .replace(")", "")
                .trim();

        return this.evaluateExpression(cleanOperand, token);
    }

    private int evaluateExpression(String expression, TokenLine token) {
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            int sum = 0;
            for (String part : parts) {
                sum += this.resolveSingleToken(part.trim(), token);
            }
            return sum;
        } else if (expression.contains("-")) {
            String[] parts = expression.split("-");
            int result = this.resolveSingleToken(parts[0].trim(), token);
            for (int i = 1; i < parts.length; i++) {
                result -= this.resolveSingleToken(parts[i].trim(), token);
            }
            return result;
        }

        return this.resolveSingleToken(expression, token);
    }

    private int resolveSingleToken(String tokenStr, TokenLine token) {
        if (tokenStr.isEmpty()) {
            return 0;
        }

        if (tokenStr.startsWith("$")) {
            String hexOnly = tokenStr.substring(1).replaceAll("[^0-9A-Fa-f]", "");
            return Integer.parseInt(hexOnly, 16);
        }

        if (tokenStr.startsWith("%")) {
            String binOnly = tokenStr.substring(1).replaceAll("[^01]", "");
            return Integer.parseInt(binOnly, 2);
        }

        if (tokenStr.matches("\\d+")) {
            return Integer.parseInt(tokenStr);
        }

        if (this.symbolTable.containsKey(tokenStr)) {
            return this.symbolTable.get(tokenStr);
        }

        throw new IllegalArgumentException(String.format(
                "Symbol not resolved at line %d: '%s' in instruction '%s %s'",
                token.lineNumber(), tokenStr, token.mnemonic(), token.operand() != null ? token.operand() : ""
        ));
    }
}