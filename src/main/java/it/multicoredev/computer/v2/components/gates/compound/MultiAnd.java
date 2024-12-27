package it.multicoredev.computer.v2.components.gates.compound;

import it.multicoredev.computer.v2.components.ChipComponent;
import it.multicoredev.computer.v2.components.gates.And;

public class MultiAnd extends ChipComponent {
    private final And[] ands;

    public MultiAnd(int inputs) {
        super(inputs, 1, "mAND");

        if (inputs < 3) throw new IllegalArgumentException("Inputs must be at least 3");

        ands = new And[inputs - 1];
        for (int i = 0; i < ands.length; i++) {
            ands[i] = new And();
        }

        run();
    }

    @Override
    protected void update() {
        ands[0].in(in[0], in[1]);

        for (int i = 1; i < ands.length; i++) {
            ands[i].in(ands[i - 1].out()[0], in[i + 1]);
        }

        out[0] = ands[ands.length - 1].out()[0];
    }
}
