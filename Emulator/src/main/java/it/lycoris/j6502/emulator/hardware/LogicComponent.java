package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.emulated.EmulationContext;

public sealed interface LogicComponent permits Gate, ComplexChip {

    String getName();

    Wire[] getInputs();

    Wire[] getOutputs();

    void update(EmulationContext ctx);
}
