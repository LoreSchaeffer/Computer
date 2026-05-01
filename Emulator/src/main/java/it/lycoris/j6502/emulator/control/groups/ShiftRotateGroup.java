package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers shift and rotate instructions (ASL, LSR, ROL, ROR).
 * Supports polymorphic execution across different CPU emulation strategies.
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
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            if (address == -1) {
                hardwareCpu.writeToBus(value, 0);
                hardwareCpu.loadAccumulatorDirect(0);
            } else {
                hardwareCpu.writeSystemBus(address, value);
            }
            hardwareCpu.updateZAndNFlags(value);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            if (address == -1) {
                fastCpu.setAccumulator(value);
            } else {
                fastCpu.writeSystemBus(address, value);
            }
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private boolean readCarry(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.isFlagSet('C');
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.isFlagC();
        throw new UnsupportedOperationException("Unsupported CPU architecture for Carry read.");
    }

    private void updateCarry(Cpu cpu, boolean state) {
        if (cpu instanceof GateLevelCpu hardwareCpu) hardwareCpu.forceFlag('C', state);
        else if (cpu instanceof InstructionLevelCpu fastCpu) fastCpu.setFlagC(state);
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void executeAsl(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean newCarry = (value & 0x80) != 0; // Old bit 7 becomes Carry
        int result = (value << 1) & 0xFF;

        this.updateCarry(cpu, newCarry);
        this.writeBack(cpu, address, result);
    }

    private void executeLsr(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean newCarry = (value & 0x01) != 0; // Old bit 0 becomes Carry
        int result = (value >> 1) & 0xFF;

        this.updateCarry(cpu, newCarry);
        this.writeBack(cpu, address, result);
    }

    private void executeRol(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean oldCarry = this.readCarry(cpu);
        boolean newCarry = (value & 0x80) != 0;
        int result = ((value << 1) | (oldCarry ? 1 : 0)) & 0xFF;

        this.updateCarry(cpu, newCarry);
        this.writeBack(cpu, address, result);
    }

    private void executeRor(Cpu cpu, int address) {
        int value = this.readValue(cpu, address);
        boolean oldCarry = this.readCarry(cpu);
        boolean newCarry = (value & 0x01) != 0;
        int result = ((value >> 1) | (oldCarry ? 0x80 : 0)) & 0xFF;

        this.updateCarry(cpu, newCarry);
        this.writeBack(cpu, address, result);
    }
}
