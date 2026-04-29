package it.lycoris.j6502.emulator.control;

import it.lycoris.j6502.emulator.hardware.MOS6502;

@FunctionalInterface
public interface Instruction {
    void execute(MOS6502 cpu);
}
