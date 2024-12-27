package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Nor;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.gates.Xor;

public class Dabble extends ChipComponent {
    private final Xor xor1 = new Xor();
    private final Nor nor1 = new Nor();
    private final Xor xor2 = new Xor();

    private final Nor nor2 = new Nor();

    private final Nor nor3 = new Nor();
    private final Or or = new Or();

    private final Nor nor4 = new Nor();
    private final And and = new And();
    private final Xor xor3 = new Xor();

    public Dabble() {
        super(4, 4, "Dabble");
        run();
    }

    @Override
    protected void update() {
        xor1.in(in[0], in[3]);
        nor1.in(in[0], in[1]);
        xor2.in(in[0], in[2]);

        nor2.in(xor1.out()[0], xor2.out()[0]);

        nor3.in(nor2.out()[0], nor1.out()[0]);
        or.in(nor1.out()[0], xor1.out()[0]);

        nor4.in(or.out()[0], in[2]);
        and.in(or.out()[0], xor2.out()[0]);
        xor3.in(nor3.out()[0], in[3]);

        out[0] = nor3.out()[0];
        out[1] = nor4.out()[0];
        out[2] = and.out()[0];
        out[3] = xor3.out()[0];
    }
}
