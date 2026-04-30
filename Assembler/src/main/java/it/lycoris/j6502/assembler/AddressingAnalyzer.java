package it.lycoris.j6502.assembler;

public class AddressingAnalyzer {

    public AddressingMode determineMode(String mnemonic, String operand) {
        if (operand == null || operand.isEmpty()) return AddressingMode.IMPLIED;
        if (operand.equals("A")) return AddressingMode.ACCUMULATOR;
        if (operand.startsWith("#")) return AddressingMode.IMMEDIATE;
        if (operand.startsWith("(")) {
            if (operand.endsWith(",X)")) return AddressingMode.INDIRECT_X;
            if (operand.endsWith("),Y")) return AddressingMode.INDIRECT_Y;
            if (operand.endsWith(")")) return AddressingMode.INDIRECT;
        }
        if (isBranchInstruction(mnemonic)) return AddressingMode.RELATIVE;

        boolean isIndexedX = operand.endsWith(",X");
        boolean isIndexedY = operand.endsWith(",Y");

        String base = operand.replace(",X", "").replace(",Y", "");

        if (base.startsWith("$")) {
            String hexStr = base.substring(1);

            if (hexStr.length() <= 2) {
                if (isIndexedX) return AddressingMode.ZERO_PAGE_X;
                if (isIndexedY) return AddressingMode.ZERO_PAGE_Y;
                return AddressingMode.ZERO_PAGE;
            } else {
                if (isIndexedX) return AddressingMode.ABSOLUTE_X;
                if (isIndexedY) return AddressingMode.ABSOLUTE_Y;
                return AddressingMode.ABSOLUTE;
            }
        }

        // In this pass we don't know if the label is in zero page or absolute memory
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
