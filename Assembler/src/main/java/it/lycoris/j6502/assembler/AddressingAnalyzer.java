package it.lycoris.j6502.assembler;

import java.util.Map;

/**
 * Analyzes assembly operands to determine the correct MOS 6502 addressing mode.
 */
public class AddressingAnalyzer {

    /**
     * Determines the addressing mode of an instruction.
     * Uses the symbol table to optimize Zero Page addressing for known labels.
     *
     * @param mnemonic    The instruction mnemonic (e.g., "LDA").
     * @param operand     The raw operand string (e.g., "$10,X", "TILE_X").
     * @param symbolTable The populated symbol table to resolve label addresses.
     * @return The corresponding AddressingMode enum value.
     */
    public AddressingMode determineMode(String mnemonic, String operand, Map<String, Integer> symbolTable) {
        if (operand == null || operand.isEmpty()) return AddressingMode.IMPLIED;
        if (operand.equals("A")) return AddressingMode.ACCUMULATOR;
        if (operand.startsWith("#")) return AddressingMode.IMMEDIATE;
        if (operand.startsWith("(")) {
            if (operand.endsWith(",X)")) return AddressingMode.INDIRECT_X;
            if (operand.endsWith("),Y")) return AddressingMode.INDIRECT_Y;
            if (operand.endsWith(")")) return AddressingMode.INDIRECT;
        }

        if (this.isBranchInstruction(mnemonic)) return AddressingMode.RELATIVE;

        boolean isIndexedX = operand.endsWith(",X");
        boolean isIndexedY = operand.endsWith(",Y");

        String base = operand.replace(",X", "").replace(",Y", "").trim();

        // Direct Hexadecimal Definition
        if (base.startsWith("$")) {
            String hexString = base.substring(1);
            if (hexString.length() <= 2) {
                if (isIndexedX) return AddressingMode.ZERO_PAGE_X;
                if (isIndexedY) return AddressingMode.ZERO_PAGE_Y;
                return AddressingMode.ZERO_PAGE;
            } else {
                if (isIndexedX) return AddressingMode.ABSOLUTE_X;
                if (isIndexedY) return AddressingMode.ABSOLUTE_Y;
                return AddressingMode.ABSOLUTE;
            }
        }

        // Label Resolution Optimization (Zero Page fallback check)
        if (symbolTable != null && symbolTable.containsKey(base)) {
            int memoryAddress = symbolTable.get(base);
            if (memoryAddress >= 0x00 && memoryAddress <= 0xFF) {
                if (isIndexedX) return AddressingMode.ZERO_PAGE_X;
                if (isIndexedY) return AddressingMode.ZERO_PAGE_Y;
                return AddressingMode.ZERO_PAGE;
            }
        }

        // Default Fallback
        if (isIndexedX) return AddressingMode.ABSOLUTE_X;
        if (isIndexedY) return AddressingMode.ABSOLUTE_Y;
        return AddressingMode.ABSOLUTE;
    }

    private boolean isBranchInstruction(String mnemonic) {
        if (mnemonic == null) return false;

        return mnemonic.equals("BCC") ||
                mnemonic.equals("BCS") ||
                mnemonic.equals("BEQ") ||
                mnemonic.equals("BMI") ||
                mnemonic.equals("BNE") ||
                mnemonic.equals("BPL") ||
                mnemonic.equals("BVC") ||
                mnemonic.equals("BVS");
    }
}