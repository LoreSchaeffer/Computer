package it.lycoris.cpu.hardware;

import it.lycoris.cpu.simulation.SimulationContext;

public final class NorGate implements Gate {
    private final String name;
    private final Wire inputA;
    private final Wire inputB;
    private final Wire output;

    private final Wire[] inputsArray;
    private final Wire[] outputsArray;

    public NorGate(String name, Wire inputA, Wire inputB, Wire output) {
        this.name = name;
        this.inputA = inputA;
        this.inputB = inputB;
        this.output = output;

        this.inputsArray = new Wire[]{this.inputA, this.inputB};
        this.outputsArray = new Wire[]{this.output};

        Wire.Listener scheduleMe = (newState, ctx) -> ctx.schedule(this);
        this.inputA.addListener(scheduleMe);
        this.inputB.addListener(scheduleMe);
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
    public void update(SimulationContext ctx) {
        this.output.setState(!(this.inputA.getState() | this.inputB.getState()), ctx);
    }
}
