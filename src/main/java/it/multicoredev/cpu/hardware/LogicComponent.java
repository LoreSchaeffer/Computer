package it.multicoredev.cpu.hardware;

public sealed interface LogicComponent permits Gate, ComplexChip {

    String getName();

    Wire[] getInputs();

    Wire[] getOutputs();

    void update();
}
