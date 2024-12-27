package it.multicoredev.computer.v2.components.display;

import it.multicoredev.computer.v2.components.gates.And;
import it.multicoredev.computer.v2.components.gates.Not;
import it.multicoredev.computer.v2.components.gates.compound.MultiOr;

import java.util.Arrays;

public class BlankingNegativeDriver extends DisplayDriver {
    private final MultiOr mOr = new MultiOr(4);

    private final Not not1 = new Not();

    private final And and1 = new And();

    private final Not not2 = new Not();

    private final And and2 = new And();

    private final And and3 = new And();
    private final And and4 = new And();
    private final And and5 = new And();
    private final And and6 = new And();
    private final And and7 = new And();
    private final And and8 = new And();
    private final And and9 = new And();

    private final Not not3 = new Not();

    private final And and10 = new And();

    private boolean minus = false;

    public BlankingNegativeDriver() {
        super(6, 9);
    }

    @Override
    protected void update() {
        mOr.in(Arrays.copyOfRange(in, 0, 4));
        super.in(Arrays.copyOfRange(in, 0, 4));

        not1.in(mOr.out()[0]);

        and1.in(in[5], not1.out()[0]);

        not2.in(and1.out()[0]);

        and2.in(in[4], not2.out()[0]);

        and3.in(out()[0], not2.out()[0]);
        and4.in(out()[1], not2.out()[0]);
        and5.in(out()[2], not2.out()[0]);
        and6.in(out()[3], not2.out()[0]);
        and7.in(out()[4], not2.out()[0]);
        and8.in(out()[5], not2.out()[0]);
        and9.in(out()[6], not2.out()[0]);

        not3.in(and2.out()[0]);

        and10.in(in[4], not3.out()[0]);

        out[0] = and3.out()[0];
        out[1] = and4.out()[0];
        out[2] = and5.out()[0];
        out[3] = and6.out()[0];
        out[4] = and7.out()[0];
        out[5] = and8.out()[0];
        out[6] = and9.out()[0];
        out[7] = and10.out()[0];
        out[8] = and1.out()[0];
        minus = and2.out()[0];

        display.setSegments(Arrays.copyOfRange(out, 0, 8));
        display.setMinus(minus);
    }

    public boolean[] outData() {
        return Arrays.copyOfRange(out, 0, 8);
    }

    public boolean outMinus() {
        return minus;
    }

    public boolean outRippleNegative() {
        return out[7];
    }

    public boolean outRippleBlanking() {
        return out[8];
    }
}
