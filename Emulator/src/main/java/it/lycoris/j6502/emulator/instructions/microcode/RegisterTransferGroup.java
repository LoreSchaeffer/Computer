package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers inter-register transfer instructions (TAX, TXA, TAY, TYA).
 * Bypasses memory completely to move data internally within the CPU datapath.
 */
public class RegisterTransferGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0xAA, new OpcodeMetadata("TAX", cpu -> this.transfer(cpu, cpu.getAccumulator(), true, false, false)));
        registry.put(0xA8, new OpcodeMetadata("TAY", cpu -> this.transfer(cpu, cpu.getAccumulator(), false, true, false)));
        registry.put(0x8A, new OpcodeMetadata("TXA", cpu -> this.transfer(cpu, cpu.getRegisterX(), false, false, true)));
        registry.put(0x98, new OpcodeMetadata("TYA", cpu -> this.transfer(cpu, cpu.getRegisterY(), false, false, true)));
        registry.put(0xBA, new OpcodeMetadata("TSX", cpu -> this.transfer(cpu, cpu.getStackPointer(), true, false, false)));

        // TXS strictly transfers X to SP without modifying N or Z flags
        registry.put(0x9A, new OpcodeMetadata("TXS", cpu -> {
            MOS6502 datapath = cpu.getDatapath();
            cpu.assertDataBus(cpu.getRegisterX());
            datapath.LoadSP = true;
            cpu.pulseClock();
            datapath.LoadSP = false;
            datapath.evaluateCombinational();
        }));
    }

    private void transfer(Cpu cpu, int value, boolean loadX, boolean loadY, boolean loadA) {
        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(value);
        if (loadA) datapath.BypassALU = true;

        datapath.LoadX = loadX;
        datapath.LoadY = loadY;
        datapath.LoadA = loadA;

        cpu.pulseClock();

        datapath.LoadX = false;
        datapath.LoadY = false;
        datapath.LoadA = false;
        datapath.BypassALU = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(value);
    }
}