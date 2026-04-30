package it.lycoris.j6502.assembler;

public record TokenLine(
        String label,
        String mnemonic,
        String operand,
        int lineNumber,
        String originalLine
) {

    public boolean isEmpty() {
        return label == null && mnemonic == null;
    }
}
