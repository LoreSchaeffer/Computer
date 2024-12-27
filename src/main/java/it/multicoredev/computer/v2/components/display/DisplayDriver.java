package it.multicoredev.computer.v2.components.display;

import it.multicoredev.computer.util.Utils;
import it.multicoredev.computer.v2.components.gates.*;

public class DisplayDriver extends DisplayComponent {
    private final Xor xor1 = new Xor();
    private final Not not = new Not();

    private final Nand nand1 = new Nand();

    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Or or1 = new Or();

    private final Or or2 = new Or();
    private final Or or3 = new Or();
    private final Or or4 = new Or();

    private final Nand nand2 = new Nand();
    private final Or or5 = new Or();
    private final And and1 = new And();
    private final And and2 = new And();
    private final And and3 = new And();
    private final And and4 = new And();

    public DisplayDriver() {
        super(4, 7);
    }

    protected DisplayDriver(int inSize, int outSize) {
        super(inSize, outSize);
    }

    @Override
    public DisplayDriver in(boolean... in) {
        super.in(Utils.flip(in));
        return this;
    }

    @Override
    protected void update() {
        xor1.in(in[3], in[1]);
        not.in(in[1]);

        nand1.in(in[2], not.out()[0]);

        xor2.in(nand1.out()[0], in[0]);
        xor3.in(in[2], xor1.out()[0]);
        or1.in(in[2], xor1.out()[0]);

        or2.in(xor1.out()[0], xor2.out()[0]);
        or3.in(in[2], not.out()[0]);
        or4.in(xor2.out()[0], xor3.out()[0]);

        nand2.in(xor2.out()[0], in[2]);
        or5.in(or3.out()[0], in[0]);
        and1.in(or2.out()[0], or4.out()[0]);
        and2.in(xor2.out()[0], nand1.out()[0]);
        and3.in(or3.out()[0], or4.out()[0]);
        and4.in(or4.out()[0], or1.out()[0]);

        out[0] = or2.out()[0];
        out[1] = nand2.out()[0];
        out[2] = or5.out()[0];
        out[3] = and1.out()[0];
        out[4] = and2.out()[0];
        out[5] = and3.out()[0];
        out[6] = and4.out()[0];

        display.setMinus(false);
        display.setSegments(out);
    }
}
