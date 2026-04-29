package it.lycoris.cpu.control;

import it.lycoris.cpu.hardware.MOS6502;

@FunctionalInterface
public interface Instruction {
    void execute(MOS6502 cpu);
}
