package it.multicoredev.computer.v2.components.gates;

import it.multicoredev.computer.v2.components.ChipComponent;

public class And extends ChipComponent {

    public And() {
        super(2, 1, "AND");
        run();
    }

    @Override
    protected void update() {
        out[0] = in[0] && in[1];
    }
}
