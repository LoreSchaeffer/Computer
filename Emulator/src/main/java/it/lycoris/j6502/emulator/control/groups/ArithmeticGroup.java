package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers arithmetic instructions (ADC, SBC, INC, DEC, INX, DEX, INY, DEY)
 * into the execution environment.
 * Supports polymorphic execution across different CPU emulation strategies.
 */
public class ArithmeticGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ADC (Add with Carry) ---
        registry.put(0x69, new OpcodeMetadata("ADC #", cpu -> this.adc(cpu, this.fetchImmediate(cpu))));
        registry.put(0x65, new OpcodeMetadata("ADC $zp", cpu -> this.adc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x75, new OpcodeMetadata("ADC $zp,X", cpu -> this.adc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0x6D, new OpcodeMetadata("ADC $abs", cpu -> this.adc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0x7D, new OpcodeMetadata("ADC $abs,X", cpu -> this.adc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0x79, new OpcodeMetadata("ADC $abs,Y", cpu -> this.adc(cpu, this.fetchAbsoluteY(cpu))));

        // --- SBC (Subtract with Carry) ---
        registry.put(0xE9, new OpcodeMetadata("SBC #", cpu -> this.sbc(cpu, this.fetchImmediate(cpu))));
        registry.put(0xE5, new OpcodeMetadata("SBC $zp", cpu -> this.sbc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0xF5, new OpcodeMetadata("SBC $zp,X", cpu -> this.sbc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0xED, new OpcodeMetadata("SBC $abs", cpu -> this.sbc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0xFD, new OpcodeMetadata("SBC $abs,X", cpu -> this.sbc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0xF9, new OpcodeMetadata("SBC $abs,Y", cpu -> this.sbc(cpu, this.fetchAbsoluteY(cpu))));

        // --- Increments/Decrements (Register) ---
        registry.put(0xE8, new OpcodeMetadata("INX", this::executeInx));
        registry.put(0xCA, new OpcodeMetadata("DEX", this::executeDex));
        registry.put(0xC8, new OpcodeMetadata("INY", this::executeIny));
        registry.put(0x88, new OpcodeMetadata("DEY", this::executeDey));

        // --- INC (Increment Memory) ---
        registry.put(0xE6, new OpcodeMetadata("INC $zp", cpu -> this.incMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xF6, new OpcodeMetadata("INC $zp,X", cpu -> this.incMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xEE, new OpcodeMetadata("INC $abs", cpu -> this.incMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xFE, new OpcodeMetadata("INC $abs,X", cpu -> this.incMem(cpu, this.resolveAbsoluteX(cpu))));

        // --- DEC (Decrement Memory) ---
        registry.put(0xC6, new OpcodeMetadata("DEC $zp", cpu -> this.decMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xD6, new OpcodeMetadata("DEC $zp,X", cpu -> this.decMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xCE, new OpcodeMetadata("DEC $abs", cpu -> this.decMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xDE, new OpcodeMetadata("DEC $abs,X", cpu -> this.decMem(cpu, this.resolveAbsoluteX(cpu))));
    }

    // ========================================================================
    // VALUE FETCHING ABSTRACTIONS (Used by ADC, SBC)
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.fetchOperand();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextByte();
        throw new UnsupportedOperationException("Unsupported CPU architecture for immediate fetch.");
    }

    private int fetchZeroPage(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrZeroPage());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus(fastCpu.fetchNextByte());
        throw new UnsupportedOperationException("Unsupported CPU architecture for Zero Page fetch.");
    }

    private int fetchZeroPageX(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrZeroPageX());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus((fastCpu.fetchNextByte() + fastCpu.getRegisterX()) & 0xFF);
        throw new UnsupportedOperationException("Unsupported CPU architecture for Zero Page X fetch.");
    }

    private int fetchAbsolute(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrAbsolute());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus(fastCpu.fetchNextAddress());
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute fetch.");
    }

    private int fetchAbsoluteX(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrAbsoluteX());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus((fastCpu.fetchNextAddress() + fastCpu.getRegisterX()) & 0xFFFF);
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute X fetch.");
    }

    private int fetchAbsoluteY(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrAbsoluteY());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus((fastCpu.fetchNextAddress() + fastCpu.getRegisterY()) & 0xFFFF);
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute Y fetch.");
    }

    // ========================================================================
    // ADDRESS RESOLUTION ABSTRACTIONS (Used by INC, DEC)
    // ========================================================================

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

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void adc(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.executeALU("OpADD", value, hardwareCpu.isFlagSet('C'), true);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int sum = fastCpu.getAccumulator() + value + (fastCpu.isFlagC() ? 1 : 0);
            fastCpu.setFlagC(sum > 0xFF);
            fastCpu.setFlagV(((fastCpu.getAccumulator() ^ sum) & (value ^ sum) & 0x80) != 0);
            fastCpu.setAccumulator(sum);
            fastCpu.updateZeroAndNegativeFlags(sum);
        }
    }

    private void sbc(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.executeALU("OpADD", (~value) & 0xFF, hardwareCpu.isFlagSet('C'), true);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int invertedValue = (~value) & 0xFF;
            int sum = fastCpu.getAccumulator() + invertedValue + (fastCpu.isFlagC() ? 1 : 0);
            fastCpu.setFlagC(sum > 0xFF);
            fastCpu.setFlagV(((fastCpu.getAccumulator() ^ sum) & (invertedValue ^ sum) & 0x80) != 0);
            fastCpu.setAccumulator(sum);
            fastCpu.updateZeroAndNegativeFlags(sum);
        }
    }

    private void executeInx(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.indexOp("IncX");
            hardwareCpu.updateZAndNFlags(hardwareCpu.snapshot().x());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = (fastCpu.getRegisterX() + 1) & 0xFF;
            fastCpu.setRegisterX(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void executeDex(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.indexOp("DecX");
            hardwareCpu.updateZAndNFlags(hardwareCpu.snapshot().x());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = (fastCpu.getRegisterX() - 1) & 0xFF;
            fastCpu.setRegisterX(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void executeIny(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.indexOp("IncY");
            hardwareCpu.updateZAndNFlags(hardwareCpu.snapshot().y());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = (fastCpu.getRegisterY() + 1) & 0xFF;
            fastCpu.setRegisterY(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void executeDey(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.indexOp("DecY");
            hardwareCpu.updateZAndNFlags(hardwareCpu.snapshot().y());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = (fastCpu.getRegisterY() - 1) & 0xFF;
            fastCpu.setRegisterY(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void incMem(Cpu cpu, int address) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            int value = (hardwareCpu.readSystemBus(address) + 1) & 0xFF;
            hardwareCpu.writeSystemBus(address, value);
            hardwareCpu.updateZAndNFlags(value);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int value = (fastCpu.readSystemBus(address) + 1) & 0xFF;
            fastCpu.writeSystemBus(address, value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void decMem(Cpu cpu, int address) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            int value = (hardwareCpu.readSystemBus(address) - 1) & 0xFF;
            hardwareCpu.writeSystemBus(address, value);
            hardwareCpu.updateZAndNFlags(value);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int value = (fastCpu.readSystemBus(address) - 1) & 0xFF;
            fastCpu.writeSystemBus(address, value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }
}