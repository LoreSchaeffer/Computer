package it.lycoris.j6502.emulator.core.cpu;

import it.lycoris.j6502.emulator.core.SystemBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The High-Level Emulation (HLE) implementation of the 6502 CPU.
 * Executes instructions directly using native Java constructs for maximum performance,
 * achieving cycle accuracy through programmatic accounting.
 */
public class HighLevelCpu implements Cpu {
    private static final Logger LOG = LoggerFactory.getLogger(HighLevelCpu.class);

    // CPU Status Flags (Bitmasks)
    private static final int FLAG_C = 0x01; // Carry
    private static final int FLAG_Z = 0x02; // Zero
    private static final int FLAG_I = 0x04; // Interrupt Disable
    private static final int FLAG_D = 0x08; // Decimal Mode
    private static final int FLAG_B = 0x10; // Break Command
    private static final int FLAG_U = 0x20; // Unused (Always 1)
    private static final int FLAG_V = 0x40; // Overflow
    private static final int FLAG_N = 0x80; // Negative

    private final SystemBus bus;

    // Architectural CPU Registers
    private int accumulator;
    private int registerX;
    private int registerY;
    private int programCounter;
    private int stackPointer;
    private int statusRegister;

    // Emulator State
    private long totalClockCycles;
    private int suspendedCycles;
    private boolean nmiLineActive;
    private boolean irqLineActive;

    public HighLevelCpu(SystemBus bus) {
        this.bus = bus;
        this.totalClockCycles = 0L;
        this.statusRegister = FLAG_U;
        this.stackPointer = 0xFD;
    }

    @Override
    public void reset() {
        LOG.info("Initiating HLE hardware reset sequence...");
        int lowByte = this.bus.read(0xFFFC);
        int highByte = this.bus.read(0xFFFD);
        this.programCounter = (highByte << 8) | lowByte;

        this.stackPointer = (this.stackPointer - 3) & 0xFF;
        this.setFlag(FLAG_I, true);
        this.totalClockCycles += 7;

        LOG.info("HLE CPU Reset complete. Execution begins at: ${}", String.format("%04X", this.programCounter));
    }

    @Override
    public void step() {
        if (this.suspendedCycles > 0) {
            this.suspendedCycles--;
            this.totalClockCycles++;
            return;
        }

        if (this.nmiLineActive) {
            this.nmiLineActive = false;
            this.serviceInterrupt(0xFFFA, false);
            return;
        }

        if (this.irqLineActive && !this.getFlag(FLAG_I)) {
            this.irqLineActive = false;
            this.serviceInterrupt(0xFFFE, false);
            return;
        }

        int opcode = this.bus.read(this.programCounter);
        this.programCounter = (this.programCounter + 1) & 0xFFFF;

        this.executeOpcode(opcode);
    }

    private void executeOpcode(int opcode) {
        switch (opcode) {
            // ================================================================
            // LOAD & STORE
            // ================================================================
            // LDA
            case 0xA9 -> {
                this.executeLda(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xA5 -> {
                this.executeLda(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xB5 -> {
                this.executeLda(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0xAD -> {
                this.executeLda(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0xBD -> {
                this.executeLda(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0xB9 -> {
                this.executeLda(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0xA1 -> {
                this.executeLda(this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0xB1 -> {
                this.executeLda(this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            // LDX
            case 0xA2 -> {
                this.executeLdx(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xA6 -> {
                this.executeLdx(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xB6 -> {
                this.executeLdx(this.bus.read(this.resolveZeroPageY()));
                this.totalClockCycles += 4;
            }
            case 0xAE -> {
                this.executeLdx(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0xBE -> {
                this.executeLdx(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }

            // LDY
            case 0xA0 -> {
                this.executeLdy(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xA4 -> {
                this.executeLdy(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xB4 -> {
                this.executeLdy(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0xAC -> {
                this.executeLdy(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0xBC -> {
                this.executeLdy(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }

            // STA
            case 0x85 -> {
                this.bus.write(this.resolveZeroPage(), this.accumulator);
                this.totalClockCycles += 3;
            }
            case 0x95 -> {
                this.bus.write(this.resolveZeroPageX(), this.accumulator);
                this.totalClockCycles += 4;
            }
            case 0x8D -> {
                this.bus.write(this.resolveAbsolute(), this.accumulator);
                this.totalClockCycles += 4;
            }
            case 0x9D -> {
                this.bus.write(this.resolveAbsoluteX(false), this.accumulator);
                this.totalClockCycles += 5;
            }
            case 0x99 -> {
                this.bus.write(this.resolveAbsoluteY(false), this.accumulator);
                this.totalClockCycles += 5;
            }
            case 0x81 -> {
                this.bus.write(this.resolveIndexedIndirectX(), this.accumulator);
                this.totalClockCycles += 6;
            }
            case 0x91 -> {
                this.bus.write(this.resolveIndirectIndexedY(false), this.accumulator);
                this.totalClockCycles += 6;
            }

            // STX
            case 0x86 -> {
                this.bus.write(this.resolveZeroPage(), this.registerX);
                this.totalClockCycles += 3;
            }
            case 0x96 -> {
                this.bus.write(this.resolveZeroPageY(), this.registerX);
                this.totalClockCycles += 4;
            }
            case 0x8E -> {
                this.bus.write(this.resolveAbsolute(), this.registerX);
                this.totalClockCycles += 4;
            }

            // STY
            case 0x84 -> {
                this.bus.write(this.resolveZeroPage(), this.registerY);
                this.totalClockCycles += 3;
            }
            case 0x94 -> {
                this.bus.write(this.resolveZeroPageX(), this.registerY);
                this.totalClockCycles += 4;
            }
            case 0x8C -> {
                this.bus.write(this.resolveAbsolute(), this.registerY);
                this.totalClockCycles += 4;
            }

            // ================================================================
            // ARITHMETIC (ADC / SBC)
            // ================================================================
            case 0x69 -> {
                this.executeAdc(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0x65 -> {
                this.executeAdc(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0x75 -> {
                this.executeAdc(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0x6D -> {
                this.executeAdc(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0x7D -> {
                this.executeAdc(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0x79 -> {
                this.executeAdc(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0x61 -> {
                this.executeAdc(this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0x71 -> {
                this.executeAdc(this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            case 0xE9 -> {
                this.executeSbc(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xE5 -> {
                this.executeSbc(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xF5 -> {
                this.executeSbc(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0xED -> {
                this.executeSbc(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0xFD -> {
                this.executeSbc(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0xF9 -> {
                this.executeSbc(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0xE1 -> {
                this.executeSbc(this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0xF1 -> {
                this.executeSbc(this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            // ================================================================
            // LOGICAL (AND, ORA, EOR, BIT)
            // ================================================================
            // AND
            case 0x29 -> {
                this.executeAnd(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0x25 -> {
                this.executeAnd(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0x35 -> {
                this.executeAnd(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0x2D -> {
                this.executeAnd(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0x3D -> {
                this.executeAnd(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0x39 -> {
                this.executeAnd(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0x21 -> {
                this.executeAnd(this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0x31 -> {
                this.executeAnd(this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            // ORA
            case 0x09 -> {
                this.executeOra(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0x05 -> {
                this.executeOra(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0x15 -> {
                this.executeOra(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0x0D -> {
                this.executeOra(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0x1D -> {
                this.executeOra(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0x19 -> {
                this.executeOra(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0x01 -> {
                this.executeOra(this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0x11 -> {
                this.executeOra(this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            // EOR
            case 0x49 -> {
                this.executeEor(this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0x45 -> {
                this.executeEor(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0x55 -> {
                this.executeEor(this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0x4D -> {
                this.executeEor(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0x5D -> {
                this.executeEor(this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0x59 -> {
                this.executeEor(this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0x41 -> {
                this.executeEor(this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0x51 -> {
                this.executeEor(this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            // BIT
            case 0x24 -> {
                this.executeBit(this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0x2C -> {
                this.executeBit(this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }

            // ================================================================
            // COMPARES (CMP, CPX, CPY)
            // ================================================================
            case 0xC9 -> {
                this.executeCompare(this.accumulator, this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xC5 -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xD5 -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveZeroPageX()));
                this.totalClockCycles += 4;
            }
            case 0xCD -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }
            case 0xDD -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveAbsoluteX(true)));
                this.totalClockCycles += 4;
            }
            case 0xD9 -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveAbsoluteY(true)));
                this.totalClockCycles += 4;
            }
            case 0xC1 -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveIndexedIndirectX()));
                this.totalClockCycles += 6;
            }
            case 0xD1 -> {
                this.executeCompare(this.accumulator, this.bus.read(this.resolveIndirectIndexedY(true)));
                this.totalClockCycles += 5;
            }

            case 0xE0 -> {
                this.executeCompare(this.registerX, this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xE4 -> {
                this.executeCompare(this.registerX, this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xEC -> {
                this.executeCompare(this.registerX, this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }

            case 0xC0 -> {
                this.executeCompare(this.registerY, this.fetchImmediate());
                this.totalClockCycles += 2;
            }
            case 0xC4 -> {
                this.executeCompare(this.registerY, this.bus.read(this.resolveZeroPage()));
                this.totalClockCycles += 3;
            }
            case 0xCC -> {
                this.executeCompare(this.registerY, this.bus.read(this.resolveAbsolute()));
                this.totalClockCycles += 4;
            }

            // ================================================================
            // SHIFTS & ROTATES (ASL, LSR, ROL, ROR)
            // ================================================================
            case 0x0A -> {
                this.accumulator = this.executeAsl(this.accumulator);
                this.totalClockCycles += 2;
            }
            case 0x06 -> {
                this.executeMemoryShift(this.resolveZeroPage(), ShiftType.ASL);
                this.totalClockCycles += 5;
            }
            case 0x16 -> {
                this.executeMemoryShift(this.resolveZeroPageX(), ShiftType.ASL);
                this.totalClockCycles += 6;
            }
            case 0x0E -> {
                this.executeMemoryShift(this.resolveAbsolute(), ShiftType.ASL);
                this.totalClockCycles += 6;
            }
            case 0x1E -> {
                this.executeMemoryShift(this.resolveAbsoluteX(false), ShiftType.ASL);
                this.totalClockCycles += 7;
            }

            case 0x4A -> {
                this.accumulator = this.executeLsr(this.accumulator);
                this.totalClockCycles += 2;
            }
            case 0x46 -> {
                this.executeMemoryShift(this.resolveZeroPage(), ShiftType.LSR);
                this.totalClockCycles += 5;
            }
            case 0x56 -> {
                this.executeMemoryShift(this.resolveZeroPageX(), ShiftType.LSR);
                this.totalClockCycles += 6;
            }
            case 0x4E -> {
                this.executeMemoryShift(this.resolveAbsolute(), ShiftType.LSR);
                this.totalClockCycles += 6;
            }
            case 0x5E -> {
                this.executeMemoryShift(this.resolveAbsoluteX(false), ShiftType.LSR);
                this.totalClockCycles += 7;
            }

            case 0x2A -> {
                this.accumulator = this.executeRol(this.accumulator);
                this.totalClockCycles += 2;
            }
            case 0x26 -> {
                this.executeMemoryShift(this.resolveZeroPage(), ShiftType.ROL);
                this.totalClockCycles += 5;
            }
            case 0x36 -> {
                this.executeMemoryShift(this.resolveZeroPageX(), ShiftType.ROL);
                this.totalClockCycles += 6;
            }
            case 0x2E -> {
                this.executeMemoryShift(this.resolveAbsolute(), ShiftType.ROL);
                this.totalClockCycles += 6;
            }
            case 0x3E -> {
                this.executeMemoryShift(this.resolveAbsoluteX(false), ShiftType.ROL);
                this.totalClockCycles += 7;
            }

            case 0x6A -> {
                this.accumulator = this.executeRor(this.accumulator);
                this.totalClockCycles += 2;
            }
            case 0x66 -> {
                this.executeMemoryShift(this.resolveZeroPage(), ShiftType.ROR);
                this.totalClockCycles += 5;
            }
            case 0x76 -> {
                this.executeMemoryShift(this.resolveZeroPageX(), ShiftType.ROR);
                this.totalClockCycles += 6;
            }
            case 0x6E -> {
                this.executeMemoryShift(this.resolveAbsolute(), ShiftType.ROR);
                this.totalClockCycles += 6;
            }
            case 0x7E -> {
                this.executeMemoryShift(this.resolveAbsoluteX(false), ShiftType.ROR);
                this.totalClockCycles += 7;
            }

            // ================================================================
            // INCREMENTS & DECREMENTS
            // ================================================================
            case 0xE8 -> {
                this.registerX = (this.registerX + 1) & 0xFF;
                this.updateZnFlags(this.registerX);
                this.totalClockCycles += 2;
            } // INX
            case 0xCA -> {
                this.registerX = (this.registerX - 1) & 0xFF;
                this.updateZnFlags(this.registerX);
                this.totalClockCycles += 2;
            } // DEX
            case 0xC8 -> {
                this.registerY = (this.registerY + 1) & 0xFF;
                this.updateZnFlags(this.registerY);
                this.totalClockCycles += 2;
            } // INY
            case 0x88 -> {
                this.registerY = (this.registerY - 1) & 0xFF;
                this.updateZnFlags(this.registerY);
                this.totalClockCycles += 2;
            } // DEY

            case 0xE6 -> {
                this.executeMemoryIncDec(this.resolveZeroPage(), 1);
                this.totalClockCycles += 5;
            } // INC
            case 0xF6 -> {
                this.executeMemoryIncDec(this.resolveZeroPageX(), 1);
                this.totalClockCycles += 6;
            }
            case 0xEE -> {
                this.executeMemoryIncDec(this.resolveAbsolute(), 1);
                this.totalClockCycles += 6;
            }
            case 0xFE -> {
                this.executeMemoryIncDec(this.resolveAbsoluteX(false), 1);
                this.totalClockCycles += 7;
            }

            case 0xC6 -> {
                this.executeMemoryIncDec(this.resolveZeroPage(), -1);
                this.totalClockCycles += 5;
            } // DEC
            case 0xD6 -> {
                this.executeMemoryIncDec(this.resolveZeroPageX(), -1);
                this.totalClockCycles += 6;
            }
            case 0xCE -> {
                this.executeMemoryIncDec(this.resolveAbsolute(), -1);
                this.totalClockCycles += 6;
            }
            case 0xDE -> {
                this.executeMemoryIncDec(this.resolveAbsoluteX(false), -1);
                this.totalClockCycles += 7;
            }

            // ================================================================
            // REGISTER TRANSFERS
            // ================================================================
            case 0xAA -> {
                this.registerX = this.accumulator;
                this.updateZnFlags(this.registerX);
                this.totalClockCycles += 2;
            } // TAX
            case 0xA8 -> {
                this.registerY = this.accumulator;
                this.updateZnFlags(this.registerY);
                this.totalClockCycles += 2;
            } // TAY
            case 0x8A -> {
                this.accumulator = this.registerX;
                this.updateZnFlags(this.accumulator);
                this.totalClockCycles += 2;
            } // TXA
            case 0x98 -> {
                this.accumulator = this.registerY;
                this.updateZnFlags(this.accumulator);
                this.totalClockCycles += 2;
            } // TYA
            case 0xBA -> {
                this.registerX = this.stackPointer;
                this.updateZnFlags(this.registerX);
                this.totalClockCycles += 2;
            } // TSX
            case 0x9A -> {
                this.stackPointer = this.registerX;
                this.totalClockCycles += 2;
            } // TXS (Does not affect flags)

            // ================================================================
            // STACK OPERATIONS
            // ================================================================
            case 0x48 -> {
                this.push8(this.accumulator);
                this.totalClockCycles += 3;
            } // PHA
            case 0x68 -> {
                this.accumulator = this.pull8();
                this.updateZnFlags(this.accumulator);
                this.totalClockCycles += 4;
            } // PLA
            case 0x08 -> {
                this.push8(this.statusRegister | FLAG_B | FLAG_U);
                this.totalClockCycles += 3;
            } // PHP
            case 0x28 -> {
                this.statusRegister = (this.pull8() & ~FLAG_B) | FLAG_U;
                this.totalClockCycles += 4;
            } // PLP

            // ================================================================
            // FLAG CONTROLS
            // ================================================================
            case 0x18 -> {
                this.setFlag(FLAG_C, false);
                this.totalClockCycles += 2;
            } // CLC
            case 0x38 -> {
                this.setFlag(FLAG_C, true);
                this.totalClockCycles += 2;
            }  // SEC
            case 0x58 -> {
                this.setFlag(FLAG_I, false);
                this.totalClockCycles += 2;
            } // CLI
            case 0x78 -> {
                this.setFlag(FLAG_I, true);
                this.totalClockCycles += 2;
            }  // SEI
            case 0xB8 -> {
                this.setFlag(FLAG_V, false);
                this.totalClockCycles += 2;
            } // CLV
            case 0xD8 -> {
                this.setFlag(FLAG_D, false);
                this.totalClockCycles += 2;
            } // CLD
            case 0xF8 -> {
                this.setFlag(FLAG_D, true);
                this.totalClockCycles += 2;
            }  // SED

            // ================================================================
            // JUMPS & SUBROUTINES
            // ================================================================
            case 0x4C -> {
                this.programCounter = this.resolveAbsolute();
                this.totalClockCycles += 3;
            } // JMP Absolute
            case 0x6C -> {
                int pointer = this.resolveAbsolute();
                int low = this.bus.read(pointer);
                int high = this.bus.read((pointer & 0xFF00) | ((pointer + 1) & 0x00FF)); // Hardware page-wrap bug
                this.programCounter = (high << 8) | low;
                this.totalClockCycles += 5;
            } // JMP Indirect
            case 0x20 -> {
                int target = this.resolveAbsolute();
                this.push16(this.programCounter - 1);
                this.programCounter = target;
                this.totalClockCycles += 6;
            } // JSR
            case 0x60 -> {
                this.programCounter = this.pull16() + 1;
                this.totalClockCycles += 6;
            } // RTS
            case 0x40 -> {
                this.statusRegister = (this.pull8() & ~FLAG_B) | FLAG_U;
                this.programCounter = this.pull16();
                this.totalClockCycles += 6;
            } // RTI

            // ================================================================
            // BRANCHES
            // ================================================================
            case 0x90 -> {
                this.executeBranch(!this.getFlag(FLAG_C));
            } // BCC
            case 0xB0 -> {
                this.executeBranch(this.getFlag(FLAG_C));
            }  // BCS
            case 0xF0 -> {
                this.executeBranch(this.getFlag(FLAG_Z));
            }  // BEQ
            case 0xD0 -> {
                this.executeBranch(!this.getFlag(FLAG_Z));
            } // BNE
            case 0x30 -> {
                this.executeBranch(this.getFlag(FLAG_N));
            }  // BMI
            case 0x10 -> {
                this.executeBranch(!this.getFlag(FLAG_N));
            } // BPL
            case 0x50 -> {
                this.executeBranch(!this.getFlag(FLAG_V));
            } // BVC
            case 0x70 -> {
                this.executeBranch(this.getFlag(FLAG_V));
            }  // BVS

            // ================================================================
            // SYSTEM & MISC
            // ================================================================
            case 0x00 -> {
                this.serviceInterrupt(0xFFFE, true);
            } // BRK
            case 0xEA -> {
                this.totalClockCycles += 2;
            } // NOP

            default -> {
                LOG.warn("Unimplemented or Illegal HLE Opcode: ${}", String.format("%02X", opcode));
                this.totalClockCycles += 2;
            }
        }
    }

    // ========================================================================
    // ALU EXECUTION HELPERS
    // ========================================================================

    private void executeLda(int value) {
        this.accumulator = value;
        this.updateZnFlags(this.accumulator);
    }

    private void executeLdx(int value) {
        this.registerX = value;
        this.updateZnFlags(this.registerX);
    }

    private void executeLdy(int value) {
        this.registerY = value;
        this.updateZnFlags(this.registerY);
    }

    private void executeAnd(int value) {
        this.accumulator &= value;
        this.updateZnFlags(this.accumulator);
    }

    private void executeOra(int value) {
        this.accumulator |= value;
        this.updateZnFlags(this.accumulator);
    }

    private void executeEor(int value) {
        this.accumulator ^= value;
        this.updateZnFlags(this.accumulator);
    }

    private void executeBit(int value) {
        this.setFlag(FLAG_Z, (this.accumulator & value) == 0);
        this.setFlag(FLAG_N, (value & 0x80) != 0);
        this.setFlag(FLAG_V, (value & 0x40) != 0);
    }

    private void executeCompare(int register, int memory) {
        int diff = register - memory;
        this.setFlag(FLAG_C, register >= memory);
        this.setFlag(FLAG_Z, (diff & 0xFF) == 0);
        this.setFlag(FLAG_N, (diff & 0x80) != 0);
    }

    private void executeAdc(int value) {
        int carry = this.getFlag(FLAG_C) ? 1 : 0;
        int sum = this.accumulator + value + carry;

        // Overflow is set if signs of inputs are the same, but sign of result is different
        boolean overflow = (~(this.accumulator ^ value) & (this.accumulator ^ sum) & 0x80) != 0;

        if (this.getFlag(FLAG_D)) {
            // Decimal Mode BCD addition
            int lowerNibble = (this.accumulator & 0x0F) + (value & 0x0F) + carry;
            int upperNibble = (this.accumulator >> 4) + (value >> 4) + (lowerNibble > 0x09 ? 1 : 0);

            if (lowerNibble > 0x09) lowerNibble += 0x06;
            boolean bcdCarry = upperNibble > 0x09;
            if (bcdCarry) upperNibble += 0x06;

            this.accumulator = ((upperNibble << 4) | (lowerNibble & 0x0F)) & 0xFF;
            this.setFlag(FLAG_C, bcdCarry);
            this.setFlag(FLAG_Z, (sum & 0xFF) == 0);
            this.setFlag(FLAG_N, (sum & 0x80) != 0);
            this.setFlag(FLAG_V, overflow);
        } else {
            this.setFlag(FLAG_C, sum > 0xFF);
            this.accumulator = sum & 0xFF;
            this.setFlag(FLAG_V, overflow);
            this.updateZnFlags(this.accumulator);
        }
    }

    private void executeSbc(int value) {
        int carry = this.getFlag(FLAG_C) ? 1 : 0;
        int inverted = (~value) & 0xFF;
        int sum = this.accumulator + inverted + carry;

        // Overflow is based on the binary addition in all modes
        boolean overflow = ((this.accumulator ^ sum) & (inverted ^ sum) & 0x80) != 0;

        if (this.getFlag(FLAG_D)) {
            // Decimal Mode BCD subtraction
            int lowerNibble = (this.accumulator & 0x0F) - (value & 0x0F) - (1 - carry);
            int upperNibble = (this.accumulator >> 4) - (value >> 4) - (lowerNibble < 0 ? 1 : 0);

            if (lowerNibble < 0) {
                lowerNibble -= 0x06;
            }
            if (upperNibble < 0) {
                upperNibble -= 0x06;
            }

            this.accumulator = ((upperNibble << 4) | (lowerNibble & 0x0F)) & 0xFF;

            // In the original NMOS 6502, the SBC carry out in BCD mode
            // is perfectly identical to the binary carry out behavior.
            this.setFlag(FLAG_C, sum > 0xFF);

            // Flags Z, N, and V remain strictly based on the unadjusted binary result
            this.setFlag(FLAG_Z, (sum & 0xFF) == 0);
            this.setFlag(FLAG_N, (sum & 0x80) != 0);
            this.setFlag(FLAG_V, overflow);
        } else {
            // Standard Binary Subtraction
            this.setFlag(FLAG_C, sum > 0xFF);
            this.accumulator = sum & 0xFF;
            this.setFlag(FLAG_V, overflow);
            this.updateZnFlags(this.accumulator);
        }
    }

    private enum ShiftType {ASL, LSR, ROL, ROR}

    private void executeMemoryShift(int address, ShiftType type) {
        int value = this.bus.read(address);
        int result = 0;

        switch (type) {
            case ASL -> result = this.executeAsl(value);
            case LSR -> result = this.executeLsr(value);
            case ROL -> result = this.executeRol(value);
            case ROR -> result = this.executeRor(value);
        }

        this.bus.write(address, result);
    }

    private int executeAsl(int value) {
        this.setFlag(FLAG_C, (value & 0x80) != 0);
        int result = (value << 1) & 0xFF;
        this.updateZnFlags(result);
        return result;
    }

    private int executeLsr(int value) {
        this.setFlag(FLAG_C, (value & 0x01) != 0);
        int result = (value >> 1) & 0xFF;
        this.updateZnFlags(result);
        return result;
    }

    private int executeRol(int value) {
        int carryIn = this.getFlag(FLAG_C) ? 1 : 0;
        this.setFlag(FLAG_C, (value & 0x80) != 0);
        int result = ((value << 1) | carryIn) & 0xFF;
        this.updateZnFlags(result);
        return result;
    }

    private int executeRor(int value) {
        int carryIn = this.getFlag(FLAG_C) ? 0x80 : 0x00;
        this.setFlag(FLAG_C, (value & 0x01) != 0);
        int result = ((value >> 1) | carryIn) & 0xFF;
        this.updateZnFlags(result);
        return result;
    }

    private void executeMemoryIncDec(int address, int amount) {
        int value = this.bus.read(address);
        int result = (value + amount) & 0xFF;
        this.bus.write(address, result);
        this.updateZnFlags(result);
    }

    private void executeBranch(boolean condition) {
        byte offset = (byte) this.fetchImmediate(); // Read as signed byte
        this.totalClockCycles += 2;

        if (condition) {
            int targetAddress = (this.programCounter + offset) & 0xFFFF;
            this.totalClockCycles++; // Taken branch penalty

            if ((this.programCounter & 0xFF00) != (targetAddress & 0xFF00)) {
                this.totalClockCycles++; // Page crossing penalty
            }

            this.programCounter = targetAddress;
        }
    }

    // ========================================================================
    // ADDRESS RESOLUTION (Returns Effective Memory Address)
    // ========================================================================

    private int fetchImmediate() {
        int data = this.bus.read(this.programCounter);
        this.programCounter = (this.programCounter + 1) & 0xFFFF;
        return data;
    }

    private int resolveZeroPage() {
        return this.fetchImmediate();
    }

    private int resolveZeroPageX() {
        return (this.fetchImmediate() + this.registerX) & 0xFF;
    }

    private int resolveZeroPageY() {
        return (this.fetchImmediate() + this.registerY) & 0xFF;
    }

    private int resolveAbsolute() {
        int low = this.fetchImmediate();
        int high = this.fetchImmediate();
        return (high << 8) | low;
    }

    private int resolveAbsoluteX(boolean appliesPenalty) {
        int base = this.resolveAbsolute();
        int effective = (base + this.registerX) & 0xFFFF;
        if (appliesPenalty && (base & 0xFF00) != (effective & 0xFF00)) {
            this.totalClockCycles++;
        }
        return effective;
    }

    private int resolveAbsoluteY(boolean appliesPenalty) {
        int base = this.resolveAbsolute();
        int effective = (base + this.registerY) & 0xFFFF;
        if (appliesPenalty && (base & 0xFF00) != (effective & 0xFF00)) {
            this.totalClockCycles++;
        }
        return effective;
    }

    private int resolveIndexedIndirectX() {
        int zp = (this.fetchImmediate() + this.registerX) & 0xFF;
        int low = this.bus.read(zp);
        int high = this.bus.read((zp + 1) & 0xFF);
        return (high << 8) | low;
    }

    private int resolveIndirectIndexedY(boolean appliesPenalty) {
        int zp = this.fetchImmediate();
        int low = this.bus.read(zp);
        int high = this.bus.read((zp + 1) & 0xFF);
        int base = (high << 8) | low;
        int effective = (base + this.registerY) & 0xFFFF;
        if (appliesPenalty && (base & 0xFF00) != (effective & 0xFF00)) {
            this.totalClockCycles++;
        }
        return effective;
    }

    // ========================================================================
    // HARDWARE STACK & SYSTEM
    // ========================================================================

    private void push8(int value) {
        this.bus.write(0x0100 | this.stackPointer, value & 0xFF);
        this.stackPointer = (this.stackPointer - 1) & 0xFF;
    }

    private int pull8() {
        this.stackPointer = (this.stackPointer + 1) & 0xFF;
        return this.bus.read(0x0100 | this.stackPointer);
    }

    private void push16(int value) {
        this.push8((value >> 8) & 0xFF);
        this.push8(value & 0xFF);
    }

    private int pull16() {
        int low = this.pull8();
        int high = this.pull8();
        return (high << 8) | low;
    }

    private void serviceInterrupt(int vector, boolean isSoftwareBreak) {
        if (isSoftwareBreak) {
            this.programCounter = (this.programCounter + 1) & 0xFFFF;
            this.push16(this.programCounter);
            this.push8(this.statusRegister | FLAG_B | FLAG_U);
        } else {
            this.push16(this.programCounter);
            this.push8((this.statusRegister & ~FLAG_B) | FLAG_U);
        }

        this.setFlag(FLAG_I, true);
        int lowByte = this.bus.read(vector);
        int highByte = this.bus.read(vector + 1);
        this.programCounter = (highByte << 8) | lowByte;
        this.totalClockCycles += 7;
    }

    private void updateZnFlags(int value) {
        this.setFlag(FLAG_Z, value == 0);
        this.setFlag(FLAG_N, (value & 0x80) != 0);
    }

    private void setFlag(int flagMask, boolean state) {
        if (state) this.statusRegister |= flagMask;
        else this.statusRegister &= ~flagMask;
    }

    private boolean getFlag(int flagMask) {
        return (this.statusRegister & flagMask) != 0;
    }

    @Override
    public void triggerNmi() {
        this.nmiLineActive = true;
    }

    @Override
    public void triggerIrq() {
        this.irqLineActive = true;
    }

    @Override
    public void suspendCycles(int cycles) {
        this.suspendedCycles += cycles;
    }

    @Override
    public long getTotalClockCycles() {
        return this.totalClockCycles;
    }

    @Override
    public int getProgramCounter() {
        return this.programCounter;
    }

    @Override
    public CpuState snapshot() {
        return new CpuState(
                this.programCounter, this.accumulator, this.registerX,
                this.registerY, this.stackPointer, this.statusRegister, this.totalClockCycles
        );
    }
}