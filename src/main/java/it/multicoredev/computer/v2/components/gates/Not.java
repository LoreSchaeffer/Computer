package it.multicoredev.computer.v2.components.gates;

import it.multicoredev.computer.v2.components.ChipComponent;

public class Not extends ChipComponent {

    public Not() {
        super(1, 1, "NOT");
        run();
    }

    @Override
    protected void update() {
        out[0] = !in[0];
    }
}
