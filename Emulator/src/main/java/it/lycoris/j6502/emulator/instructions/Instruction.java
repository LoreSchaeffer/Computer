package it.lycoris.j6502.emulator.instructions;

import it.lycoris.j6502.emulator.core.Cpu;

/**
 * Functional interface representing a single CPU instruction execution sequence.
 * Implementations of this interface will act as microcode sequencers,
 * manipulating the hardware pins of the MOS6502 datapath.
 */
@FunctionalInterface
public interface Instruction {
    /**
     * Executes the instruction microcode on the provided CPU.
     *
     * @param cpu The target CPU executing the instruction.
     */
    void execute(Cpu cpu);
}
