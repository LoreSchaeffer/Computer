package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.ChipComponent;

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
