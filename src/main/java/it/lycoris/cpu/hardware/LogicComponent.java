package it.lycoris.cpu.hardware;

import it.lycoris.cpu.simulation.SimulationContext;

public sealed interface LogicComponent permits Gate, ComplexChip {

    String getName();

    Wire[] getInputs();

    Wire[] getOutputs();

    void update(SimulationContext ctx);
}
