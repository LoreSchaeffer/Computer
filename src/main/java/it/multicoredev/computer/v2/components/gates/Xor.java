package it.multicoredev.computer.v2.components.gates;

import it.multicoredev.computer.v2.components.ChipComponent;

public class Xor extends ChipComponent {
    private final Or or = new Or();
    private final Nand nand = new Nand();
    private final And and = new And();

    public Xor() {
        super(2, 1, "XOR");
        run();
    }

    @Override
    protected void update() {
        or.in(in);
        nand.in(in);

        and.in(or.out()[0], nand.out()[0]);

        out[0] = and.out()[0];
    }
}
