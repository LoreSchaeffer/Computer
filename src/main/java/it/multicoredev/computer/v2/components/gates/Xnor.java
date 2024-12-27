package it.multicoredev.computer.v2.components.gates;

import it.multicoredev.computer.v2.components.ChipComponent;

public class Xnor extends ChipComponent {
    private final Xor xor = new Xor();
    private final Not not = new Not();

    public Xnor() {
        super(2, 1, "XNOR");
        run();
    }

    @Override
    protected void update() {
        xor.in(in);
        not.in(xor.out());
        out[0] = not.out()[0];
    }
}
