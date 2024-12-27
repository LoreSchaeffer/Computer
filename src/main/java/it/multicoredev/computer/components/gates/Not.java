package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.ChipComponent;

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
