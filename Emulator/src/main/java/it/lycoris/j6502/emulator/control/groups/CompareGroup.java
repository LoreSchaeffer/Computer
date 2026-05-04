package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;

import java.util.Map;

/**
 * Registers comparison instructions (CMP, CPX, CPY).
 * Operates by computing the difference and explicitly asserting hardware flags.
 */
public class CompareGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- CMP (Compare Accumulator) ---
        registry.put(0xC9, new OpcodeMetadata("CMP #", cpu -> this.cmp(cpu, cpu.getAccumulator(), this.fetchImmediate(cpu))));
        registry.put(0xC5, new OpcodeMetadata("CMP $zp", cpu -> this.cmp(cpu, cpu.getAccumulator(), this.fetchZeroPage(cpu))));
        registry.put(0xCD, new OpcodeMetadata("CMP $abs", cpu -> this.cmp(cpu, cpu.getAccumulator(), this.fetchAbsolute(cpu))));

        // --- CPX (Compare X Register) ---
        registry.put(0xE0, new OpcodeMetadata("CPX #", cpu -> this.cmp(cpu, cpu.getRegisterX(), this.fetchImmediate(cpu))));
        registry.put(0xE4, new OpcodeMetadata("CPX $zp", cpu -> this.cmp(cpu, cpu.getRegisterX(), this.fetchZeroPage(cpu))));
        registry.put(0xEC, new OpcodeMetadata("CPX $abs", cpu -> this.cmp(cpu, cpu.getRegisterX(), this.fetchAbsolute(cpu))));

        // --- CPY (Compare Y Register) ---
        registry.put(0xC0, new OpcodeMetadata("CPY #", cpu -> this.cmp(cpu, cpu.getRegisterY(), this.fetchImmediate(cpu))));
        registry.put(0xC4, new OpcodeMetadata("CPY $zp", cpu -> this.cmp(cpu, cpu.getRegisterY(), this.fetchZeroPage(cpu))));
        registry.put(0xCC, new OpcodeMetadata("CPY $abs", cpu -> this.cmp(cpu, cpu.getRegisterY(), this.fetchAbsolute(cpu))));
    }

    // ========================================================================
    // ADDRESSING MODE RESOLUTION
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int fetchZeroPage(Cpu cpu) {
        int address = cpu.fetchNextByte();
        return cpu.readSystemBus(address);
    }

    private int fetchAbsolute(Cpu cpu) {
        int address = cpu.fetchNextAddress();
        return cpu.readSystemBus(address);
    }

    // ========================================================================
    // HARDWARE EXECUTION LOGIC
    // ========================================================================

    private void cmp(Cpu cpu, int registerValue, int memoryValue) {
        int maskedMemoryValue = memoryValue & 0xFF;
        boolean carryCondition = registerValue >= maskedMemoryValue;
        int difference = (registerValue - maskedMemoryValue) & 0xFF;

        cpu.forceFlag('C', carryCondition);
        cpu.forceZeroAndNegativeFlags(difference);
    }
}