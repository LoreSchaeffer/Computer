package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers Load and Store instructions (LDA, LDX, LDY, STA, STX, STY).
 * Demonstrates architectural elegance: store operations resolve natively via the Cpu interface
 * without requiring engine-specific polymorphic execution.
 */
public class LoadStoreGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- LDA (Load Accumulator) ---
        registry.put(0xA9, new OpcodeMetadata("LDA #", cpu -> this.loadA(cpu, this.fetchImmediate(cpu))));
        registry.put(0xA5, new OpcodeMetadata("LDA $zp", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveZeroPage(cpu)))));
        registry.put(0xB5, new OpcodeMetadata("LDA $zp,X", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveZeroPageX(cpu)))));
        registry.put(0xAD, new OpcodeMetadata("LDA $abs", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveAbsolute(cpu)))));
        registry.put(0xBD, new OpcodeMetadata("LDA $abs,X", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveAbsoluteX(cpu)))));
        registry.put(0xB9, new OpcodeMetadata("LDA $abs,Y", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveAbsoluteY(cpu)))));
        registry.put(0xA1, new OpcodeMetadata("LDA ($zp,X)", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveIndexedIndirectX(cpu)))));
        registry.put(0xB1, new OpcodeMetadata("LDA ($zp),Y", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveIndirectIndexedY(cpu)))));

        // --- LDX (Load X Register) ---
        registry.put(0xA2, new OpcodeMetadata("LDX #", cpu -> this.loadX(cpu, this.fetchImmediate(cpu))));
        registry.put(0xA6, new OpcodeMetadata("LDX $zp", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveZeroPage(cpu)))));
        registry.put(0xB6, new OpcodeMetadata("LDX $zp,Y", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveZeroPageY(cpu)))));
        registry.put(0xAE, new OpcodeMetadata("LDX $abs", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveAbsolute(cpu)))));
        registry.put(0xBE, new OpcodeMetadata("LDX $abs,Y", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveAbsoluteY(cpu)))));

        // --- LDY (Load Y Register) ---
        registry.put(0xA0, new OpcodeMetadata("LDY #", cpu -> this.loadY(cpu, this.fetchImmediate(cpu))));
        registry.put(0xA4, new OpcodeMetadata("LDY $zp", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveZeroPage(cpu)))));
        registry.put(0xB4, new OpcodeMetadata("LDY $zp,X", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveZeroPageX(cpu)))));
        registry.put(0xAC, new OpcodeMetadata("LDY $abs", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveAbsolute(cpu)))));
        registry.put(0xBC, new OpcodeMetadata("LDY $abs,X", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveAbsoluteX(cpu)))));

        // --- STA (Store Accumulator) ---
        registry.put(0x85, new OpcodeMetadata("STA $zp", cpu -> cpu.writeSystemBus(this.resolveZeroPage(cpu), cpu.getAccumulator())));
        registry.put(0x95, new OpcodeMetadata("STA $zp,X", cpu -> cpu.writeSystemBus(this.resolveZeroPageX(cpu), cpu.getAccumulator())));
        registry.put(0x8D, new OpcodeMetadata("STA $abs", cpu -> cpu.writeSystemBus(this.resolveAbsolute(cpu), cpu.getAccumulator())));
        registry.put(0x9D, new OpcodeMetadata("STA $abs,X", cpu -> cpu.writeSystemBus(this.resolveAbsoluteX(cpu), cpu.getAccumulator())));
        registry.put(0x99, new OpcodeMetadata("STA $abs,Y", cpu -> cpu.writeSystemBus(this.resolveAbsoluteY(cpu), cpu.getAccumulator())));
        registry.put(0x81, new OpcodeMetadata("STA ($zp,X)", cpu -> cpu.writeSystemBus(this.resolveIndexedIndirectX(cpu), cpu.getAccumulator())));
        registry.put(0x91, new OpcodeMetadata("STA ($zp),Y", cpu -> cpu.writeSystemBus(this.resolveIndirectIndexedY(cpu), cpu.getAccumulator())));

        // --- STX & STY (Store X and Y Registers) ---
        registry.put(0x86, new OpcodeMetadata("STX $zp", cpu -> cpu.writeSystemBus(this.resolveZeroPage(cpu), cpu.readRegisterDirectly("X"))));
        registry.put(0x8E, new OpcodeMetadata("STX $abs", cpu -> cpu.writeSystemBus(this.resolveAbsolute(cpu), cpu.readRegisterDirectly("X"))));
        registry.put(0x84, new OpcodeMetadata("STY $zp", cpu -> cpu.writeSystemBus(this.resolveZeroPage(cpu), cpu.readRegisterDirectly("Y"))));
        registry.put(0x8C, new OpcodeMetadata("STY $abs", cpu -> cpu.writeSystemBus(this.resolveAbsolute(cpu), cpu.readRegisterDirectly("Y"))));
    }

    // ========================================================================
    // ADDRESSING MODE RESOLUTION
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.fetchOperand();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextByte();
        throw new UnsupportedOperationException("Unsupported CPU architecture for immediate fetch.");
    }

    private int resolveZeroPage(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrZeroPage();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextByte();
        throw new UnsupportedOperationException("Unsupported CPU architecture for Zero Page address.");
    }

    private int resolveZeroPageX(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrZeroPageX();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return (fastCpu.fetchNextByte() + fastCpu.getRegisterX()) & 0xFF;
        throw new UnsupportedOperationException("Unsupported CPU architecture for Zero Page X address.");
    }

    private int resolveZeroPageY(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrZeroPageY();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return (fastCpu.fetchNextByte() + fastCpu.getRegisterY()) & 0xFF;
        throw new UnsupportedOperationException("Unsupported CPU architecture for Zero Page Y address.");
    }

    private int resolveAbsolute(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrAbsolute();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextAddress();
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute address.");
    }

    private int resolveAbsoluteX(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrAbsoluteX();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return (fastCpu.fetchNextAddress() + fastCpu.getRegisterX()) & 0xFFFF;
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute X address.");
    }

    private int resolveAbsoluteY(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrAbsoluteY();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return (fastCpu.fetchNextAddress() + fastCpu.getRegisterY()) & 0xFFFF;
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute Y address.");
    }

    private int resolveIndexedIndirectX(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrIndexedIndirectX();
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int zpAddress = (fastCpu.fetchNextByte() + fastCpu.getRegisterX()) & 0xFF;
            int lowByte = fastCpu.readSystemBus(zpAddress);
            int highByte = fastCpu.readSystemBus((zpAddress + 1) & 0xFF);
            return (highByte << 8) | lowByte;
        }
        throw new UnsupportedOperationException("Unsupported CPU architecture for Indexed Indirect X.");
    }

    private int resolveIndirectIndexedY(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrIndirectIndexedY();
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int zpAddress = fastCpu.fetchNextByte();
            int lowByte = fastCpu.readSystemBus(zpAddress);
            int highByte = fastCpu.readSystemBus((zpAddress + 1) & 0xFF);
            int baseAddress = (highByte << 8) | lowByte;
            return (baseAddress + fastCpu.getRegisterY()) & 0xFFFF;
        }
        throw new UnsupportedOperationException("Unsupported CPU architecture for Indirect Indexed Y.");
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void loadA(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.writeToBus(value, 0);
            hardwareCpu.loadAccumulatorDirect(0);
            hardwareCpu.updateZAndNFlags(hardwareCpu.getAccumulator());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setAccumulator(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void loadX(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.writeToBus(value, 0);
            hardwareCpu.pulseRegister("LoadX");
            hardwareCpu.updateZAndNFlags(hardwareCpu.readRegisterDirectly("X"));
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setRegisterX(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void loadY(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.writeToBus(value, 0);
            hardwareCpu.pulseRegister("LoadY");
            hardwareCpu.updateZAndNFlags(hardwareCpu.readRegisterDirectly("Y"));
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setRegisterY(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }
}
