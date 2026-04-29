package it.lycoris.j6502.assembler;

import java.util.HashMap;
import java.util.Map;

public class InstructionSet {
    private final Map<String, Integer> opcodes = new HashMap<>();

    public InstructionSet() {
        initOpcodes();
    }

    public Integer getOpcode(String mnemonic, AddressingMode mode) {
        if (mnemonic == null || mode == null) return null;
        return opcodes.get(mnemonic.toUpperCase() + "_" + mode.name());
    }

    private void add(String mnemonic, AddressingMode mode, int opcode) {
        opcodes.put(mnemonic.toUpperCase() + "_" + mode.name(), opcode);
    }

    private void initOpcodes() {
        // ====================================================================
        // LOAD AND STORE OPERATIONS
        // ====================================================================

        // LDA - Load Accumulator
        add("LDA", AddressingMode.IMMEDIATE, 0xA9);
        add("LDA", AddressingMode.ZERO_PAGE, 0xA5);
        add("LDA", AddressingMode.ZERO_PAGE_X, 0xB5);
        add("LDA", AddressingMode.ABSOLUTE, 0xAD);
        add("LDA", AddressingMode.ABSOLUTE_X, 0xBD);
        add("LDA", AddressingMode.ABSOLUTE_Y, 0xB9);
        add("LDA", AddressingMode.INDIRECT_X, 0xA1);
        add("LDA", AddressingMode.INDIRECT_Y, 0xB1);

        // LDX - Load X Register
        add("LDX", AddressingMode.IMMEDIATE, 0xA2);
        add("LDX", AddressingMode.ZERO_PAGE, 0xA6);
        add("LDX", AddressingMode.ZERO_PAGE_Y, 0xB6);
        add("LDX", AddressingMode.ABSOLUTE, 0xAE);
        add("LDX", AddressingMode.ABSOLUTE_Y, 0xBE);

        // LDY - Load Y Register
        add("LDY", AddressingMode.IMMEDIATE, 0xA0);
        add("LDY", AddressingMode.ZERO_PAGE, 0xA4);
        add("LDY", AddressingMode.ZERO_PAGE_X, 0xB4);
        add("LDY", AddressingMode.ABSOLUTE, 0xAC);
        add("LDY", AddressingMode.ABSOLUTE_X, 0xBC);

        // STA - Store Accumulator
        add("STA", AddressingMode.ZERO_PAGE, 0x85);
        add("STA", AddressingMode.ZERO_PAGE_X, 0x95);
        add("STA", AddressingMode.ABSOLUTE, 0x8D);
        add("STA", AddressingMode.ABSOLUTE_X, 0x9D);
        add("STA", AddressingMode.ABSOLUTE_Y, 0x99);
        add("STA", AddressingMode.INDIRECT_X, 0x81);
        add("STA", AddressingMode.INDIRECT_Y, 0x91);

        // STX - Store X Register
        add("STX", AddressingMode.ZERO_PAGE, 0x86);
        add("STX", AddressingMode.ZERO_PAGE_Y, 0x96);
        add("STX", AddressingMode.ABSOLUTE, 0x8E);

        // STY - Store Y Register
        add("STY", AddressingMode.ZERO_PAGE, 0x84);
        add("STY", AddressingMode.ZERO_PAGE_X, 0x94);
        add("STY", AddressingMode.ABSOLUTE, 0x8C);


        // ====================================================================
        // ARITHMETIC OPERATIONS
        // ====================================================================

        // ADC - Add with Carry
        add("ADC", AddressingMode.IMMEDIATE, 0x69);
        add("ADC", AddressingMode.ZERO_PAGE, 0x65);
        add("ADC", AddressingMode.ZERO_PAGE_X, 0x75);
        add("ADC", AddressingMode.ABSOLUTE, 0x6D);
        add("ADC", AddressingMode.ABSOLUTE_X, 0x7D);
        add("ADC", AddressingMode.ABSOLUTE_Y, 0x79);
        add("ADC", AddressingMode.INDIRECT_X, 0x61);
        add("ADC", AddressingMode.INDIRECT_Y, 0x71);

        // SBC - Subtract with Carry
        add("SBC", AddressingMode.IMMEDIATE, 0xE9);
        add("SBC", AddressingMode.ZERO_PAGE, 0xE5);
        add("SBC", AddressingMode.ZERO_PAGE_X, 0xF5);
        add("SBC", AddressingMode.ABSOLUTE, 0xED);
        add("SBC", AddressingMode.ABSOLUTE_X, 0xFD);
        add("SBC", AddressingMode.ABSOLUTE_Y, 0xF9);
        add("SBC", AddressingMode.INDIRECT_X, 0xE1);
        add("SBC", AddressingMode.INDIRECT_Y, 0xF1);


        // ====================================================================
        // LOGICAL OPERATIONS
        // ====================================================================

        // AND - Logical AND
        add("AND", AddressingMode.IMMEDIATE, 0x29);
        add("AND", AddressingMode.ZERO_PAGE, 0x25);
        add("AND", AddressingMode.ZERO_PAGE_X, 0x35);
        add("AND", AddressingMode.ABSOLUTE, 0x2D);
        add("AND", AddressingMode.ABSOLUTE_X, 0x3D);
        add("AND", AddressingMode.ABSOLUTE_Y, 0x39);
        add("AND", AddressingMode.INDIRECT_X, 0x21);
        add("AND", AddressingMode.INDIRECT_Y, 0x31);

        // ORA - Logical Inclusive OR
        add("ORA", AddressingMode.IMMEDIATE, 0x09);
        add("ORA", AddressingMode.ZERO_PAGE, 0x05);
        add("ORA", AddressingMode.ZERO_PAGE_X, 0x15);
        add("ORA", AddressingMode.ABSOLUTE, 0x0D);
        add("ORA", AddressingMode.ABSOLUTE_X, 0x1D);
        add("ORA", AddressingMode.ABSOLUTE_Y, 0x19);
        add("ORA", AddressingMode.INDIRECT_X, 0x01);
        add("ORA", AddressingMode.INDIRECT_Y, 0x11);

        // EOR - Logical Exclusive OR
        add("EOR", AddressingMode.IMMEDIATE, 0x49);
        add("EOR", AddressingMode.ZERO_PAGE, 0x45);
        add("EOR", AddressingMode.ZERO_PAGE_X, 0x55);
        add("EOR", AddressingMode.ABSOLUTE, 0x4D);
        add("EOR", AddressingMode.ABSOLUTE_X, 0x5D);
        add("EOR", AddressingMode.ABSOLUTE_Y, 0x59);
        add("EOR", AddressingMode.INDIRECT_X, 0x41);
        add("EOR", AddressingMode.INDIRECT_Y, 0x51);


        // ====================================================================
        // SHIFT AND ROTATE OPERATIONS
        // ====================================================================

        // ASL - Arithmetic Shift Left
        add("ASL", AddressingMode.ACCUMULATOR, 0x0A);
        add("ASL", AddressingMode.ZERO_PAGE, 0x06);
        add("ASL", AddressingMode.ZERO_PAGE_X, 0x16);
        add("ASL", AddressingMode.ABSOLUTE, 0x0E);
        add("ASL", AddressingMode.ABSOLUTE_X, 0x1E);

        // LSR - Logical Shift Right
        add("LSR", AddressingMode.ACCUMULATOR, 0x4A);
        add("LSR", AddressingMode.ZERO_PAGE, 0x46);
        add("LSR", AddressingMode.ZERO_PAGE_X, 0x56);
        add("LSR", AddressingMode.ABSOLUTE, 0x4E);
        add("LSR", AddressingMode.ABSOLUTE_X, 0x5E);

        // ROL - Rotate Left
        add("ROL", AddressingMode.ACCUMULATOR, 0x2A);
        add("ROL", AddressingMode.ZERO_PAGE, 0x26);
        add("ROL", AddressingMode.ZERO_PAGE_X, 0x36);
        add("ROL", AddressingMode.ABSOLUTE, 0x2E);
        add("ROL", AddressingMode.ABSOLUTE_X, 0x3E);

        // ROR - Rotate Right
        add("ROR", AddressingMode.ACCUMULATOR, 0x6A);
        add("ROR", AddressingMode.ZERO_PAGE, 0x66);
        add("ROR", AddressingMode.ZERO_PAGE_X, 0x76);
        add("ROR", AddressingMode.ABSOLUTE, 0x6E);
        add("ROR", AddressingMode.ABSOLUTE_X, 0x7E);


        // ====================================================================
        // COMPARE AND BIT TEST
        // ====================================================================

        // CMP - Compare Accumulator
        add("CMP", AddressingMode.IMMEDIATE, 0xC9);
        add("CMP", AddressingMode.ZERO_PAGE, 0xC5);
        add("CMP", AddressingMode.ZERO_PAGE_X, 0xD5);
        add("CMP", AddressingMode.ABSOLUTE, 0xCD);
        add("CMP", AddressingMode.ABSOLUTE_X, 0xDD);
        add("CMP", AddressingMode.ABSOLUTE_Y, 0xD9);
        add("CMP", AddressingMode.INDIRECT_X, 0xC1);
        add("CMP", AddressingMode.INDIRECT_Y, 0xD1);

        // CPX - Compare X Register
        add("CPX", AddressingMode.IMMEDIATE, 0xE0);
        add("CPX", AddressingMode.ZERO_PAGE, 0xE4);
        add("CPX", AddressingMode.ABSOLUTE, 0xEC);

        // CPY - Compare Y Register
        add("CPY", AddressingMode.IMMEDIATE, 0xC0);
        add("CPY", AddressingMode.ZERO_PAGE, 0xC4);
        add("CPY", AddressingMode.ABSOLUTE, 0xCC);

        // BIT - Bit Test
        add("BIT", AddressingMode.ZERO_PAGE, 0x24);
        add("BIT", AddressingMode.ABSOLUTE, 0x2C);


        // ====================================================================
        // INCREMENT AND DECREMENT
        // ====================================================================

        add("INC", AddressingMode.ZERO_PAGE, 0xE6);
        add("INC", AddressingMode.ZERO_PAGE_X, 0xF6);
        add("INC", AddressingMode.ABSOLUTE, 0xEE);
        add("INC", AddressingMode.ABSOLUTE_X, 0xFE);

        add("INX", AddressingMode.IMPLIED, 0xE8);
        add("INY", AddressingMode.IMPLIED, 0xC8);

        add("DEC", AddressingMode.ZERO_PAGE, 0xC6);
        add("DEC", AddressingMode.ZERO_PAGE_X, 0xD6);
        add("DEC", AddressingMode.ABSOLUTE, 0xCE);
        add("DEC", AddressingMode.ABSOLUTE_X, 0xDE);

        add("DEX", AddressingMode.IMPLIED, 0xCA);
        add("DEY", AddressingMode.IMPLIED, 0x88);


        // ====================================================================
        // BRANCHES
        // ====================================================================

        add("BCC", AddressingMode.RELATIVE, 0x90);
        add("BCS", AddressingMode.RELATIVE, 0xB0);
        add("BEQ", AddressingMode.RELATIVE, 0xF0);
        add("BMI", AddressingMode.RELATIVE, 0x30);
        add("BNE", AddressingMode.RELATIVE, 0xD0);
        add("BPL", AddressingMode.RELATIVE, 0x10);
        add("BVC", AddressingMode.RELATIVE, 0x50);
        add("BVS", AddressingMode.RELATIVE, 0x70);


        // ====================================================================
        // JUMPS AND SUBROUTINES
        // ====================================================================

        add("JMP", AddressingMode.ABSOLUTE, 0x4C);
        add("JMP", AddressingMode.INDIRECT, 0x6C);
        add("JSR", AddressingMode.ABSOLUTE, 0x20);
        add("RTS", AddressingMode.IMPLIED, 0x60);


        // ====================================================================
        // REGISTER TRANSFERS AND STACK
        // ====================================================================

        add("TAX", AddressingMode.IMPLIED, 0xAA);
        add("TAY", AddressingMode.IMPLIED, 0xA8);
        add("TXA", AddressingMode.IMPLIED, 0x8A);
        add("TYA", AddressingMode.IMPLIED, 0x98);
        add("TSX", AddressingMode.IMPLIED, 0xBA);
        add("TXS", AddressingMode.IMPLIED, 0x9A);

        add("PHA", AddressingMode.IMPLIED, 0x48);
        add("PHP", AddressingMode.IMPLIED, 0x08);
        add("PLA", AddressingMode.IMPLIED, 0x68);
        add("PLP", AddressingMode.IMPLIED, 0x28);


        // ====================================================================
        // SYSTEM AND FLAG CONTROLS
        // ====================================================================

        add("CLC", AddressingMode.IMPLIED, 0x18);
        add("CLD", AddressingMode.IMPLIED, 0xD8);
        add("CLI", AddressingMode.IMPLIED, 0x58);
        add("CLV", AddressingMode.IMPLIED, 0xB8);
        add("SEC", AddressingMode.IMPLIED, 0x38);
        add("SED", AddressingMode.IMPLIED, 0xF8);
        add("SEI", AddressingMode.IMPLIED, 0x78);

        add("BRK", AddressingMode.IMPLIED, 0x00);
        add("NOP", AddressingMode.IMPLIED, 0xEA);
        add("RTI", AddressingMode.IMPLIED, 0x40);
    }
}
