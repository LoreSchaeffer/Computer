package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers shift and rotate instructions (ASL, LSR, ROL, ROR).
 */
public class ShiftRotateGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ASL (Arithmetic Shift Left) ---
        registry.put(0x0A, new OpcodeMetadata("ASL A", cpu -> this.executeAsl(cpu, -1)));
        registry.put(0x06, new OpcodeMetadata("ASL $zp", cpu -> this.executeAsl(cpu, this.resolveZeroPage(cpu))));
        registry.put(0x16, new OpcodeMetadata("ASL $zp,X", cpu -> this.executeAsl(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0x0E, new OpcodeMetadata("ASL $abs", cpu -> this.executeAsl(cpu, this.resolveAbsolute(cpu))));
        registry.put(0x1E, new OpcodeMetadata("ASL $abs,X", cpu -> this.executeAsl(cpu, this.resolveAbsoluteX(cpu))));

        // --- LSR (Logical Shift Right) ---
        registry.put(0x4A, new OpcodeMetadata("LSR A", cpu -> this.executeLsr(cpu, -1)));
        registry.put(0x46, new OpcodeMetadata("LSR $zp", cpu -> this.executeLsr(cpu, this.resolveZeroPage(cpu))));
        registry.put(0x56, new OpcodeMetadata("LSR $zp,X", cpu -> this.executeLsr(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0x4E, new OpcodeMetadata("LSR $abs", cpu -> this.executeLsr(cpu, this.resolveAbsolute(cpu))));
        registry.put(0x5E, new OpcodeMetadata("LSR $abs,X", cpu -> this.executeLsr(cpu, this.resolveAbsoluteX(cpu))));

        // --- ROL (Rotate Left) ---
        registry.put(0x2A, new OpcodeMetadata("ROL A", cpu -> this.executeRol(cpu, -1)));
        registry.put(0x26, new OpcodeMetadata("ROL $zp", cpu -> this.executeRol(cpu, this.resolveZeroPage(cpu))));
        registry.put(0x36, new OpcodeMetadata("ROL $zp,X", cpu -> this.executeRol(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0x2E, new OpcodeMetadata("ROL $abs", cpu -> this.executeRol(cpu, this.resolveAbsolute(cpu))));
        registry.put(0x3E, new OpcodeMetadata("ROL $abs,X", cpu -> this.executeRol(cpu, this.resolveAbsoluteX(cpu))));

        // --- ROR (Rotate Right) ---
        registry.put(0x6A, new OpcodeMetadata("ROR A", cpu -> this.executeRor(cpu, -1)));
        registry.put(0x66, new OpcodeMetadata("ROR $zp", cpu -> this.executeRor(cpu, this.resolveZeroPage(cpu))));
        registry.put(0x76, new OpcodeMetadata("ROR $zp,X", cpu -> this.executeRor(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0x6E, new OpcodeMetadata("ROR $abs", cpu -> this.executeRor(cpu, this.resolveAbsolute(cpu))));
        registry.put(0x7E, new OpcodeMetadata("ROR $abs,X", cpu -> this.executeRor(cpu, this.resolveAbsoluteX(cpu))));
    }

    // ========================================================================
    // ADDRESS RESOLUTION ABSTRACTIONS
    // ========================================================================

    private int resolveZeroPage(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int resolveZeroPageX(Cpu cpu) {
        return (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
    }

    private int resolveAbsolute(Cpu cpu) {
        return cpu.fetchNextAddress();
    }

    private int resolveAbsoluteX(Cpu cpu) {
        return (cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF;
    }

    // ========================================================================
    // VALUE FETCHING AND WRITING HELPERS
    // ========================================================================

    private int readValue(Cpu cpu, int address) {
        if (address == -1) {
            return cpu.getAccumulator();
        } else {
            return cpu.readSystemBus(address);
        }
    }

    private void writeBack(Cpu cpu, int address, int value) {
        if (address == -1) {
            MOS6502 datapath = cpu.getDatapath();
            cpu.assertDataBus(value);
            datapath.BypassALU = true;
            datapath.LoadA = true;
            cpu.pulseClock();
            datapath.BypassALU = false;
            datapath.LoadA = false;
            datapath.evaluateCombinational();
        } else {
            cpu.writeSystemBus(address, value);
        }
        cpu.forceZeroAndNegativeFlags(value);
    }

    // ========================================================================
    // HARDWARE EXECUTION LOGIC
    // ========================================================================

    private void executeAsl(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean newCarry = (value & 0x80) != 0;
        int result = (value << 1) & 0xFF;

        cpu.forceFlag('C', newCarry);
        this.writeBack(cpu, address, result);
    }

    private void executeLsr(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean newCarry = (value & 0x01) != 0;
        int result = (value >> 1) & 0xFF;

        cpu.forceFlag('C', newCarry);
        this.writeBack(cpu, address, result);
    }

    private void executeRol(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean oldCarry = cpu.isFlagSet('C');
        boolean newCarry = (value & 0x80) != 0;
        int result = ((value << 1) | (oldCarry ? 1 : 0)) & 0xFF;

        cpu.forceFlag('C', newCarry);
        this.writeBack(cpu, address, result);
    }

    private void executeRor(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean oldCarry = cpu.isFlagSet('C');
        boolean newCarry = (value & 0x01) != 0;
        int result = ((value >> 1) | (oldCarry ? 0x80 : 0)) & 0xFF;

        cpu.forceFlag('C', newCarry);
        this.writeBack(cpu, address, result);
    }
}