package it.multicoredev.computer.components.gates.compound;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.gates.Or;

public class MultiOr extends ChipComponent {
    private final Or[] ors;

    public MultiOr(int inputs) {
        super(inputs, 1, "mOR");

        if (inputs < 3) throw new IllegalArgumentException("Inputs must be at least 3");

        ors = new Or[inputs - 1];
        for (int i = 0; i < ors.length; i++) {
            ors[i] = new Or();
        }

        run();
    }

    @Override
    protected void update() {
        ors[0].in(in[0], in[1]);

        for (int i = 1; i < ors.length; i++) {
            ors[i].in(ors[i - 1].out()[0], in[i + 1]);
        }

        out[0] = ors[ors.length - 1].out()[0];
    }
}
