package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;

import java.util.Map;

/**
 * Registers comparison instructions (CMP, CPX, CPY).
 * Operates by computing the difference and explicitly asserting hardware flags.
 */
public class CompareGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- CMP (Compare Accumulator) ---
        registry.put(0xC9, new OpcodeMetadata("CMP #", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchImmediate(cpu))));
        registry.put(0xC5, new OpcodeMetadata("CMP $zp", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchZeroPage(cpu))));
        registry.put(0xD5, new OpcodeMetadata("CMP $zp,X", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchZeroPageX(cpu))));
        registry.put(0xCD, new OpcodeMetadata("CMP $abs", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchAbsolute(cpu))));
        registry.put(0xDD, new OpcodeMetadata("CMP $abs,X", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchAbsoluteX(cpu))));
        registry.put(0xD9, new OpcodeMetadata("CMP $abs,Y", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchAbsoluteY(cpu))));
        registry.put(0xC1, new OpcodeMetadata("CMP ($zp,X)", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchIndexedIndirectX(cpu))));
        registry.put(0xD1, new OpcodeMetadata("CMP ($zp),Y", cpu -> this.compare(cpu, cpu.getAccumulator(), this.fetchIndirectIndexedY(cpu))));

        // --- CPX (Compare X Register) ---
        registry.put(0xE0, new OpcodeMetadata("CPX #", cpu -> this.compare(cpu, cpu.getRegisterX(), this.fetchImmediate(cpu))));
        registry.put(0xE4, new OpcodeMetadata("CPX $zp", cpu -> this.compare(cpu, cpu.getRegisterX(), this.fetchZeroPage(cpu))));
        registry.put(0xEC, new OpcodeMetadata("CPX $abs", cpu -> this.compare(cpu, cpu.getRegisterX(), this.fetchAbsolute(cpu))));

        // --- CPY (Compare Y Register) ---
        registry.put(0xC0, new OpcodeMetadata("CPY #", cpu -> this.compare(cpu, cpu.getRegisterY(), this.fetchImmediate(cpu))));
        registry.put(0xC4, new OpcodeMetadata("CPY $zp", cpu -> this.compare(cpu, cpu.getRegisterY(), this.fetchZeroPage(cpu))));
        registry.put(0xCC, new OpcodeMetadata("CPY $abs", cpu -> this.compare(cpu, cpu.getRegisterY(), this.fetchAbsolute(cpu))));
    }

    /**
     * Executes the hardware comparison logic.
     *
     * @param cpu           The CPU instance.
     * @param registerValue The 8-bit unsigned value from the internal register (A, X, or Y).
     * @param memoryValue   The 8-bit unsigned value fetched from memory.
     */
    private void compare(Cpu cpu, int registerValue, int memoryValue) {
        int difference = registerValue - memoryValue;

        cpu.forceFlag('C', registerValue >= memoryValue);
        cpu.forceFlag('Z', (difference & 0xFF) == 0);
        cpu.forceFlag('N', (difference & 0x80) != 0);
    }

    // ========================================================================
    // ADDRESSING MODE FETCH ABSTRACTIONS
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int fetchZeroPage(Cpu cpu) {
        return cpu.readSystemBus(cpu.fetchNextByte());
    }

    private int fetchZeroPageX(Cpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF);
    }

    private int fetchAbsolute(Cpu cpu) {
        return cpu.readSystemBus(cpu.fetchNextAddress());
    }

    private int fetchAbsoluteX(Cpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF);
    }

    private int fetchAbsoluteY(Cpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextAddress() + cpu.getRegisterY()) & 0xFFFF);
    }

    private int fetchIndexedIndirectX(Cpu cpu) {
        int zpAddress = (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        int effectiveAddress = (highByte << 8) | lowByte;
        return cpu.readSystemBus(effectiveAddress);
    }

    private int fetchIndirectIndexedY(Cpu cpu) {
        int zpAddress = cpu.fetchNextByte();
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        int baseAddress = (highByte << 8) | lowByte;
        int effectiveAddress = (baseAddress + cpu.getRegisterY()) & 0xFFFF;
        return cpu.readSystemBus(effectiveAddress);
    }
}