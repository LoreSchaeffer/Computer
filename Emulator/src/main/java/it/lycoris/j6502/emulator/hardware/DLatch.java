package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.simulation.SimulationContext;

public final class DLatch implements Gate {
    private final String name;
    private final Wire inputD;
    private final Wire enable;
    private final Wire outputQ;
    private final Wire outputNotQ;

    private final Wire[] inputsArray;
    private final Wire[] outputsArray;

    private boolean lastEn = false;

    public DLatch(String name, Wire inputD, Wire enable, Wire outputQ, Wire outputNotQ) {
        this.name = name;
        this.inputD = inputD;
        this.enable = enable;
        this.outputQ = outputQ;
        this.outputNotQ = outputNotQ;

        this.inputsArray = new Wire[]{this.inputD, this.enable};
        this.outputsArray = new Wire[]{this.outputQ, this.outputNotQ};

        Wire.Listener listener = (newState, ctx) -> ctx.schedule(this);
        this.inputD.addListener(listener);
        this.enable.addListener(listener);
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
        boolean currentEn = this.enable.getState();

        if (currentEn && !lastEn) {
            this.outputQ.setState(this.inputD.getState(), ctx);
            this.outputNotQ.setState(!this.inputD.getState(), ctx);
        }

        this.lastEn = currentEn;
    }
}
