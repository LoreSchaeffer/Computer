package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.emulated.EmulationContext;

public final class VccGate implements Gate {
    private final String name;
    private final Wire output;

    private final Wire[] outputsArray;

    public VccGate(String name, Wire output) {
        this.name = name;
        this.output = output;

        this.outputsArray = new Wire[]{this.output};
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Wire[] getInputs() {
        return new Wire[0];
    }

    @Override
    public Wire[] getOutputs() {
        return this.outputsArray;
    }

    @Override
    public void update(EmulationContext ctx) {
        this.output.setState(true, ctx);
    }
}
