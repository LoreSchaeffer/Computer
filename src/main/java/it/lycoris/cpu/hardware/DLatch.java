package it.lycoris.cpu.hardware;

public final class DLatch implements Gate {
    private final String name;
    private final Wire inputD;
    private final Wire enable;
    private final Wire outputQ;
    private final Wire outputNotQ;

    private boolean latchedValue;

    private final Wire[] inputsArray;
    private final Wire[] outputsArray;

    public DLatch(String name, Wire inputD, Wire enable, Wire outputQ, Wire outputNotQ) {
        this.name = name;
        this.inputD = inputD;
        this.enable = enable;
        this.outputQ = outputQ;
        this.outputNotQ = outputNotQ;

        this.inputsArray = new Wire[]{this.inputD, this.enable};
        this.outputsArray = new Wire[]{this.outputQ, this.outputNotQ};

        this.latchedValue = false;
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
    public void update() {
        if (this.enable.getState()) {
            this.latchedValue = this.inputD.getState();
        }

        this.outputQ.setState(this.latchedValue);
        this.outputNotQ.setState(!this.latchedValue);
    }
}
