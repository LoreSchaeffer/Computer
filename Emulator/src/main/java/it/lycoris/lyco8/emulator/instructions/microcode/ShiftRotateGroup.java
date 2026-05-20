package it.lycoris.lyco8.emulator.instructions.microcode;

import it.lycoris.lyco8.emulator.core.cpu.GateLevelCpu;
import it.lycoris.lyco8.emulator.instructions.InstructionGroup;
import it.lycoris.lyco8.emulator.instructions.OpcodeMetadata;

import java.util.Map;

/**
 * Registers shift and rotate instructions (ASL, LSR, ROL, ROR).
 */
public class ShiftRotateGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ASL (Arithmetic Shift Left) ---
        registry.put(0x0A, new OpcodeMetadata("ASL A", this::aslAccumulator));
        registry.put(0x06, new OpcodeMetadata("ASL $zp", cpu -> this.aslMemory(cpu, this.fetchZeroPageAddress(cpu))));
        registry.put(0x16, new OpcodeMetadata("ASL $zp,X", cpu -> this.aslMemory(cpu, this.fetchZeroPageXAddress(cpu))));
        registry.put(0x0E, new OpcodeMetadata("ASL $abs", cpu -> this.aslMemory(cpu, this.fetchAbsoluteAddress(cpu))));
        registry.put(0x1E, new OpcodeMetadata("ASL $abs,X", cpu -> this.aslMemory(cpu, this.fetchAbsoluteXAddress(cpu))));

        // --- LSR (Logical Shift Right) ---
        registry.put(0x4A, new OpcodeMetadata("LSR A", this::lsrAccumulator));
        registry.put(0x46, new OpcodeMetadata("LSR $zp", cpu -> this.lsrMemory(cpu, this.fetchZeroPageAddress(cpu))));
        registry.put(0x56, new OpcodeMetadata("LSR $zp,X", cpu -> this.lsrMemory(cpu, this.fetchZeroPageXAddress(cpu))));
        registry.put(0x4E, new OpcodeMetadata("LSR $abs", cpu -> this.lsrMemory(cpu, this.fetchAbsoluteAddress(cpu))));
        registry.put(0x5E, new OpcodeMetadata("LSR $abs,X", cpu -> this.lsrMemory(cpu, this.fetchAbsoluteXAddress(cpu))));

        // --- ROL (Rotate Left) ---
        registry.put(0x2A, new OpcodeMetadata("ROL A", this::rolAccumulator));
        registry.put(0x26, new OpcodeMetadata("ROL $zp", cpu -> this.rolMemory(cpu, this.fetchZeroPageAddress(cpu))));
        registry.put(0x36, new OpcodeMetadata("ROL $zp,X", cpu -> this.rolMemory(cpu, this.fetchZeroPageXAddress(cpu))));
        registry.put(0x2E, new OpcodeMetadata("ROL $abs", cpu -> this.rolMemory(cpu, this.fetchAbsoluteAddress(cpu))));
        registry.put(0x3E, new OpcodeMetadata("ROL $abs,X", cpu -> this.rolMemory(cpu, this.fetchAbsoluteXAddress(cpu))));

        // --- ROR (Rotate Right) ---
        registry.put(0x6A, new OpcodeMetadata("ROR A", this::rorAccumulator));
        registry.put(0x66, new OpcodeMetadata("ROR $zp", cpu -> this.rorMemory(cpu, this.fetchZeroPageAddress(cpu))));
        registry.put(0x76, new OpcodeMetadata("ROR $zp,X", cpu -> this.rorMemory(cpu, this.fetchZeroPageXAddress(cpu))));
        registry.put(0x6E, new OpcodeMetadata("ROR $abs", cpu -> this.rorMemory(cpu, this.fetchAbsoluteAddress(cpu))));
        registry.put(0x7E, new OpcodeMetadata("ROR $abs,X", cpu -> this.rorMemory(cpu, this.fetchAbsoluteXAddress(cpu))));
    }

    // ========================================================================
    // ADDRESS FETCHING
    // ========================================================================

    private int fetchZeroPageAddress(GateLevelCpu cpu) {
        return cpu.fetchNextByte();
    }

    private int fetchZeroPageXAddress(GateLevelCpu cpu) {
        return (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
    }

    private int fetchAbsoluteAddress(GateLevelCpu cpu) {
        return cpu.fetchNextAddress();
    }

    private int fetchAbsoluteXAddress(GateLevelCpu cpu) {
        return (cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF;
    }

    // ========================================================================
    // HARDWARE OPERATIONS
    // ========================================================================

    private void aslAccumulator(GateLevelCpu cpu) {
        int value = cpu.getAccumulator();
        cpu.forceFlag('C', (value & 0x80) != 0); // Bit 7 shifted into Carry
        int result = (value << 1) & 0xFF;
        this.storeAccumulator(cpu, result);
    }

    private void aslMemory(GateLevelCpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        cpu.forceFlag('C', (value & 0x80) != 0);
        int result = (value << 1) & 0xFF;
        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void lsrAccumulator(GateLevelCpu cpu) {
        int value = cpu.getAccumulator();
        cpu.forceFlag('C', (value & 0x01) != 0); // Bit 0 shifted into Carry
        int result = (value >> 1) & 0xFF;
        this.storeAccumulator(cpu, result);
    }

    private void lsrMemory(GateLevelCpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        cpu.forceFlag('C', (value & 0x01) != 0);
        int result = (value >> 1) & 0xFF;
        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void rolAccumulator(GateLevelCpu cpu) {
        int value = cpu.getAccumulator();
        int carryIn = cpu.isFlagSet('C') ? 1 : 0;
        cpu.forceFlag('C', (value & 0x80) != 0);
        int result = ((value << 1) | carryIn) & 0xFF;
        this.storeAccumulator(cpu, result);
    }

    private void rolMemory(GateLevelCpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        int carryIn = cpu.isFlagSet('C') ? 1 : 0;
        cpu.forceFlag('C', (value & 0x80) != 0);
        int result = ((value << 1) | carryIn) & 0xFF;
        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void rorAccumulator(GateLevelCpu cpu) {
        int value = cpu.getAccumulator();
        int carryIn = cpu.isFlagSet('C') ? 0x80 : 0x00;
        cpu.forceFlag('C', (value & 0x01) != 0);
        int result = ((value >> 1) | carryIn) & 0xFF;
        this.storeAccumulator(cpu, result);
    }

    private void rorMemory(GateLevelCpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        int carryIn = cpu.isFlagSet('C') ? 0x80 : 0x00;
        cpu.forceFlag('C', (value & 0x01) != 0);
        int result = ((value >> 1) | carryIn) & 0xFF;
        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void storeAccumulator(GateLevelCpu cpu, int value) {
        cpu.assertDataBus(value);
        cpu.getDatapath().BypassALU = true;
        cpu.getDatapath().LoadA = true;
        cpu.pulseClock();
        cpu.getDatapath().BypassALU = false;
        cpu.getDatapath().LoadA = false;
        cpu.getDatapath().evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(value);
    }
}