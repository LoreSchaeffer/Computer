package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers logical instructions (AND, ORA, EOR, BIT).
 * Implements bitwise operations safely across both hardware and software emulation layers.
 */
public class LogicalGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- AND (Logical AND) ---
        registry.put(0x29, new OpcodeMetadata("AND #", cpu -> this.executeAnd(cpu, this.fetchImmediate(cpu))));
        registry.put(0x25, new OpcodeMetadata("AND $zp", cpu -> this.executeAnd(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x2D, new OpcodeMetadata("AND $abs", cpu -> this.executeAnd(cpu, this.fetchAbsolute(cpu))));

        // --- ORA (Logical Inclusive OR) ---
        registry.put(0x09, new OpcodeMetadata("ORA #", cpu -> this.executeOra(cpu, this.fetchImmediate(cpu))));
        registry.put(0x05, new OpcodeMetadata("ORA $zp", cpu -> this.executeOra(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x0D, new OpcodeMetadata("ORA $abs", cpu -> this.executeOra(cpu, this.fetchAbsolute(cpu))));

        // --- EOR (Exclusive OR / XOR) ---
        registry.put(0x49, new OpcodeMetadata("EOR #", cpu -> this.executeEor(cpu, this.fetchImmediate(cpu))));
        registry.put(0x45, new OpcodeMetadata("EOR $zp", cpu -> this.executeEor(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x4D, new OpcodeMetadata("EOR $abs", cpu -> this.executeEor(cpu, this.fetchAbsolute(cpu))));

        // --- BIT (Bit Test) ---
        registry.put(0x24, new OpcodeMetadata("BIT $zp", cpu -> this.executeBit(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x2C, new OpcodeMetadata("BIT $abs", cpu -> this.executeBit(cpu, this.fetchAbsolute(cpu))));
    }

    // ========================================================================
    // ADDRESSING MODE RESOLUTION
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

    private int fetchAbsolute(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrAbsolute());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus(fastCpu.fetchNextAddress());
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute fetch.");
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void executeAnd(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.executeALU("OpAND", value, false, true);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = fastCpu.getAccumulator() & value;
            fastCpu.setAccumulator(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void executeOra(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.executeALU("OpOR", value, false, true);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = fastCpu.getAccumulator() | value;
            fastCpu.setAccumulator(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void executeEor(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.executeALU("OpXOR", value, false, true);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int result = fastCpu.getAccumulator() ^ value;
            fastCpu.setAccumulator(result);
            fastCpu.updateZeroAndNegativeFlags(result);
        }
    }

    private void executeBit(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.bitTest(value);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setFlagZ((fastCpu.getAccumulator() & value) == 0);
            fastCpu.setFlagN((value & 0x80) != 0);
            fastCpu.setFlagV((value & 0x40) != 0);
        }
    }
}