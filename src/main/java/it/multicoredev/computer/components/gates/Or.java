package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.ChipComponent;

public class Or extends ChipComponent {
    private final Not not1 = new Not();
    private final Not not2 = new Not();

    private final Nand nand = new Nand();

    public Or() {
        super(2, 1, "OR");
        run();
    }

    @Override
    protected void update() {
        not1.in(in[0]);
        not2.in(in[1]);

        nand.in(not1.out()[0], not2.out()[0]);
        out[0] = nand.out()[0];
    }
}
