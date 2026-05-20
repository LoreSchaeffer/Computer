package it.lycoris.lyco8.emulator.instructions.microcode;

import it.lycoris.lyco8.emulator.core.cpu.GateLevelCpu;
import it.lycoris.lyco8.emulator.instructions.InstructionGroup;
import it.lycoris.lyco8.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers logical instructions (AND, ORA, EOR, BIT).
 * Executes bitwise operations directly through the gate-level ALU.
 */
public class LogicalGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ORA (Logical Inclusive OR) ---
        registry.put(0x09, new OpcodeMetadata("ORA #", cpu -> this.executeOra(cpu, this.fetchImmediate(cpu))));
        registry.put(0x05, new OpcodeMetadata("ORA $zp", cpu -> this.executeOra(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x15, new OpcodeMetadata("ORA $zp,X", cpu -> this.executeOra(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0x0D, new OpcodeMetadata("ORA $abs", cpu -> this.executeOra(cpu, this.fetchAbsolute(cpu))));
        registry.put(0x1D, new OpcodeMetadata("ORA $abs,X", cpu -> this.executeOra(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0x19, new OpcodeMetadata("ORA $abs,Y", cpu -> this.executeOra(cpu, this.fetchAbsoluteY(cpu))));
        registry.put(0x01, new OpcodeMetadata("ORA ($zp,X)", cpu -> this.executeOra(cpu, this.fetchIndexedIndirectX(cpu))));
        registry.put(0x11, new OpcodeMetadata("ORA ($zp),Y", cpu -> this.executeOra(cpu, this.fetchIndirectIndexedY(cpu))));

        // --- AND (Logical AND) ---
        registry.put(0x29, new OpcodeMetadata("AND #", cpu -> this.executeAnd(cpu, this.fetchImmediate(cpu))));
        registry.put(0x25, new OpcodeMetadata("AND $zp", cpu -> this.executeAnd(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x35, new OpcodeMetadata("AND $zp,X", cpu -> this.executeAnd(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0x2D, new OpcodeMetadata("AND $abs", cpu -> this.executeAnd(cpu, this.fetchAbsolute(cpu))));
        registry.put(0x3D, new OpcodeMetadata("AND $abs,X", cpu -> this.executeAnd(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0x39, new OpcodeMetadata("AND $abs,Y", cpu -> this.executeAnd(cpu, this.fetchAbsoluteY(cpu))));
        registry.put(0x21, new OpcodeMetadata("AND ($zp,X)", cpu -> this.executeAnd(cpu, this.fetchIndexedIndirectX(cpu))));
        registry.put(0x31, new OpcodeMetadata("AND ($zp),Y", cpu -> this.executeAnd(cpu, this.fetchIndirectIndexedY(cpu))));

        // --- EOR (Exclusive OR) ---
        registry.put(0x49, new OpcodeMetadata("EOR #", cpu -> this.executeEor(cpu, this.fetchImmediate(cpu))));
        registry.put(0x45, new OpcodeMetadata("EOR $zp", cpu -> this.executeEor(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x55, new OpcodeMetadata("EOR $zp,X", cpu -> this.executeEor(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0x4D, new OpcodeMetadata("EOR $abs", cpu -> this.executeEor(cpu, this.fetchAbsolute(cpu))));
        registry.put(0x5D, new OpcodeMetadata("EOR $abs,X", cpu -> this.executeEor(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0x59, new OpcodeMetadata("EOR $abs,Y", cpu -> this.executeEor(cpu, this.fetchAbsoluteY(cpu))));
        registry.put(0x41, new OpcodeMetadata("EOR ($zp,X)", cpu -> this.executeEor(cpu, this.fetchIndexedIndirectX(cpu))));
        registry.put(0x51, new OpcodeMetadata("EOR ($zp),Y", cpu -> this.executeEor(cpu, this.fetchIndirectIndexedY(cpu))));

        // --- BIT (Bit Test) ---
        registry.put(0x24, new OpcodeMetadata("BIT $zp", cpu -> this.executeBit(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x2C, new OpcodeMetadata("BIT $abs", cpu -> this.executeBit(cpu, this.fetchAbsolute(cpu))));
    }

    // ========================================================================
    // ADDRESSING MODE FETCH ABSTRACTIONS
    // ========================================================================

    private int fetchImmediate(GateLevelCpu cpu) {
        return cpu.fetchNextByte();
    }

    private int fetchZeroPage(GateLevelCpu cpu) {
        return cpu.readSystemBus(cpu.fetchNextByte());
    }

    private int fetchZeroPageX(GateLevelCpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF);
    }

    private int fetchAbsolute(GateLevelCpu cpu) {
        return cpu.readSystemBus(cpu.fetchNextAddress());
    }

    private int fetchAbsoluteX(GateLevelCpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF);
    }

    private int fetchAbsoluteY(GateLevelCpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextAddress() + cpu.getRegisterY()) & 0xFFFF);
    }

    private int fetchIndexedIndirectX(GateLevelCpu cpu) {
        int zpAddress = (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        return cpu.readSystemBus((highByte << 8) | lowByte);
    }

    private int fetchIndirectIndexedY(GateLevelCpu cpu) {
        int zpAddress = cpu.fetchNextByte();
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        int baseAddress = (highByte << 8) | lowByte;
        return cpu.readSystemBus((baseAddress + cpu.getRegisterY()) & 0xFFFF);
    }

    // ========================================================================
    // HARDWARE LOGIC ABSTRACTIONS
    // ========================================================================

    private void executeAnd(GateLevelCpu cpu, int value) {
        this.loadAccumulator(cpu, cpu.getAccumulator() & value);
    }

    private void executeEor(GateLevelCpu cpu, int value) {
        this.loadAccumulator(cpu, cpu.getAccumulator() ^ value);
    }

    private void executeOra(GateLevelCpu cpu, int value) {
        this.loadAccumulator(cpu, cpu.getAccumulator() | value);
    }

    private void executeBit(GateLevelCpu cpu, int value) {
        int accumulator = cpu.getAccumulator();
        cpu.forceFlag('Z', (accumulator & value) == 0);
        cpu.forceFlag('N', (value & 0x80) != 0); // Bit 7 to N
        cpu.forceFlag('V', (value & 0x40) != 0); // Bit 6 to V
    }

    private void loadAccumulator(GateLevelCpu cpu, int value) {
        int result = value & 0xFF;
        MOS6502 datapath = cpu.getDatapath();

        cpu.assertDataBus(result);
        datapath.BypassALU = true;
        datapath.LoadA = true;

        cpu.pulseClock();

        datapath.BypassALU = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(result);
    }
}