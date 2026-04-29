package it.lycoris.j6502.assembler;

public enum AddressingMode {
    IMPLIED(0),         // E.g., INX, CLC (No operand, just the 1-byte opcode)
    ACCUMULATOR(0),     // E.g., ASL A
    IMMEDIATE(1),       // E.g., #$10 (1 byte operand)
    ZERO_PAGE(1),       // E.g., $10 (1 byte operand)
    ZERO_PAGE_X(1),     // E.g., $10,X
    ZERO_PAGE_Y(1),     // E.g., $10,Y
    RELATIVE(1),        // E.g., BNE LOOP (1 byte offset)
    ABSOLUTE(2),        // E.g., $1234 (2 byte operand)
    ABSOLUTE_X(2),      // E.g., $1234,X
    ABSOLUTE_Y(2),      // E.g., $1234,Y
    INDIRECT(2),        // E.g., ($1234)
    INDIRECT_X(1),      // E.g., ($10,X) - Not used in our basic test, but good to have
    INDIRECT_Y(1);      // E.g., ($10),Y

    private final int operandBytes;

    AddressingMode(int operandBytes) {
        this.operandBytes = operandBytes;
    }

    public int getInstructionLength() {
        return 1 + operandBytes;
    }
}
