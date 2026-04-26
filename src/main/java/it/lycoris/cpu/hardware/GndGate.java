package it.lycoris.cpu.hardware;

public final class GndGate implements Gate {
    private final String name;
    private final Wire output;

    private final Wire[] outputsArray;

    public GndGate(String name, Wire output) {
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
    public void update() {
        this.output.setState(false);
    }
}
