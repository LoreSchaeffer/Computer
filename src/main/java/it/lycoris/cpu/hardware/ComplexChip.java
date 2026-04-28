package it.lycoris.cpu.hardware;

import it.lycoris.cpu.simulation.SimulationContext;

public final class ComplexChip implements LogicComponent {
    private final String name;
    private final Wire[] inputs;
    private final Wire[] outputs;
    private final LogicComponent[] internalComponents;

    public ComplexChip(String name, Wire[] inputs, Wire[] outputs, LogicComponent[] internalComponents) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internalComponents = internalComponents;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Wire[] getInputs() {
        return this.inputs;
    }

    @Override
    public Wire[] getOutputs() {
        return this.outputs;
    }

    @Override
    public void update(SimulationContext ctx) {
    }

    public void powerOnReset(SimulationContext ctx) {
        for (LogicComponent comp : this.internalComponents) {
            if (comp instanceof ComplexChip cc) {
                cc.powerOnReset(ctx);
            } else {
                ctx.schedule(comp);
            }
        }
    }
}
