package it.lycoris.j6502.emulator.control;

import it.lycoris.j6502.emulator.emulated.Cpu;

@FunctionalInterface
public interface Instruction {
    void execute(Cpu cpu);
}
