package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.emulated.EmulationContext;

public final class NotGate implements Gate {
    private final String name;
    private final Wire input;
    private final Wire output;

    private final Wire[] inputsArray;
    private final Wire[] outputsArray;

    public NotGate(String name, Wire input, Wire output) {
        this.name = name;
        this.input = input;
        this.output = output;

        this.inputsArray = new Wire[]{this.input};
        this.outputsArray = new Wire[]{this.output};

        Wire.Listener scheduleMe = (newState, ctx) -> ctx.schedule(this);
        this.input.addListener(scheduleMe);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Wire[] getInputs() {
        return this.inputsArray;
    }

    @Override
    public Wire[] getOutputs() {
        return this.outputsArray;
    }

    @Override
    public void update(EmulationContext ctx) {
        this.output.setState(!this.input.getState(), ctx);
    }
}
