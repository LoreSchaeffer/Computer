package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.ChipComponent;

public class Nor extends ChipComponent {
    private final Or or = new Or();
    private final Not not = new Not();

    public Nor() {
        super(2, 1, "NOR");
        run();
    }


    @Override
    protected void update() {
        or.in(in);
        not.in(or.out());
        out[0] = not.out()[0];
    }
}
