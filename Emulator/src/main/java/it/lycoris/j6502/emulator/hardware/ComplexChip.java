package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.emulated.EmulationContext;

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
    public void update(EmulationContext ctx) {
    }

    public void powerOnReset(EmulationContext ctx) {
        for (LogicComponent comp : this.internalComponents) {
            if (comp instanceof ComplexChip cc) {
                cc.powerOnReset(ctx);
            } else {
                ctx.schedule(comp);
            }
        }
    }
}
