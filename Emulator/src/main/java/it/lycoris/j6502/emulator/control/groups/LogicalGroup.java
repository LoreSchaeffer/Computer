package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers logical instructions (AND, ORA, EOR, BIT).
 * Executes bitwise operations directly through the gate-level ALU.
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

    private void executeAnd(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        cpu.assertDataBus(value);
        datapath.OpAND = true;
        datapath.LoadA = true;

        cpu.pulseClock();

        datapath.OpAND = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
    }

    private void executeOra(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        cpu.assertDataBus(value);
        datapath.OpOR = true;
        datapath.LoadA = true;

        cpu.pulseClock();

        datapath.OpOR = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
    }

    private void executeEor(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        cpu.assertDataBus(value);
        datapath.OpXOR = true;
        datapath.LoadA = true;

        cpu.pulseClock();

        datapath.OpXOR = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
    }

    private void executeBit(Cpu cpu, int value) {
        // BIT instruction is peculiar:
        // 1. Z flag is set to the result of (A AND M).
        // 2. N flag is set to bit 7 of the memory value.
        // 3. V flag is set to bit 6 of the memory value.
        // It does NOT modify the Accumulator.

        // Emulating this via control pins depends on the exact JSON layout of your ALU.
        // Assuming your ALU sets flags on OpAND without needing LoadA, but typically,
        // emulators manage this manually if the datapath doesn't have a specific 'BIT' pin.

        int resultAnd = cpu.getAccumulator() & value;

        cpu.forceFlag('Z', resultAnd == 0);
        cpu.forceFlag('N', (value & 0x80) != 0);
        cpu.forceFlag('V', (value & 0x40) != 0);

        // This process consumes cycles on a real 6502. We simulate the timing.
        cpu.pulseClock();
    }
}