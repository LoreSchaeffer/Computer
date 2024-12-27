package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.ChipComponent;

public class Nand extends ChipComponent {
    private final And and = new And();
    private final Not not = new Not();

    public Nand() {
        super(2, 1, "NAND");
        run();
    }

    @Override
    protected void update() {
        and.in(in);
        not.in(and.out());
        out[0] = not.out()[0];
    }
}
