package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.simulation.SimulationContext;

public sealed interface LogicComponent permits Gate, ComplexChip {

    String getName();

    Wire[] getInputs();

    Wire[] getOutputs();

    void update(SimulationContext ctx);
}
