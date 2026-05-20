package it.lycoris.lyco8.emulator.instructions.microcode;

import it.lycoris.lyco8.emulator.core.cpu.GateLevelCpu;
import it.lycoris.lyco8.emulator.instructions.InstructionGroup;
import it.lycoris.lyco8.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers stack manipulation instructions (PHA, PLA, PHP, PLP, TSX, TXS).
 * Utilizes the internal hardware Stack Pointer (SP) increment/decrement pins.
 */
public class StackGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x48, new OpcodeMetadata("PHA", cpu -> cpu.pushStack(cpu.getAccumulator())));
        registry.put(0x08, new OpcodeMetadata("PHP", cpu -> cpu.pushStack(cpu.getStatusRegister() | 0x30)));
        registry.put(0x68, new OpcodeMetadata("PLA", this::pullAccumulator));
        registry.put(0x28, new OpcodeMetadata("PLP", cpu -> cpu.setStatusRegister((cpu.pullStack() & 0xEF) | 0x20)));
    }

    private void pullAccumulator(GateLevelCpu cpu) {
        int value = cpu.pullStack();
        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(value);
        datapath.BypassALU = true;
        datapath.LoadA = true;
        cpu.pulseClock();
        datapath.BypassALU = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
        cpu.forceZeroAndNegativeFlags(value);
    }
}