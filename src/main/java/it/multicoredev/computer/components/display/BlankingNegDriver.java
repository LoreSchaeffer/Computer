package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Not;
import it.multicoredev.computer.components.gates.compound.MultiOr;
import it.multicoredev.computer.ui.SegmentDisplay;

import java.util.Arrays;

public class BlankingNegDriver extends ChipComponent {
    private final MultiOr mOr = new MultiOr(4);

    private final Not not1 = new Not();

    private final DisplayDriver driver = new DisplayDriver();
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

    public BlankingNegDriver() {
        super(
                6,
                10,
                "Blanking Neg 7 Segment Driver",
                new String[]{"A", "B", "C", "D", "Neg", "Blk"},
                new String[]{"A", "B", "C", "D", "E", "F", "G", "Neg", "Blk", "S"}
        );
        run();
    }

    @Override
    protected void update() {
        mOr.in(Arrays.copyOfRange(in, 0, 4));

        not1.in(mOr.out()[0]);

        driver.in(Arrays.copyOfRange(in, 0, 4));
        and1.in(in[5], not1.out()[0]);

        not2.in(and1.out()[0]);

        and2.in(in[4], not2.out()[0]);

        and3.in(driver.out()[0], not2.out()[0]);
        and4.in(driver.out()[1], not2.out()[0]);
        and5.in(driver.out()[2], not2.out()[0]);
        and6.in(driver.out()[3], not2.out()[0]);
        and7.in(driver.out()[4], not2.out()[0]);
        and8.in(driver.out()[5], not2.out()[0]);
        and9.in(driver.out()[6], not2.out()[0]);

        not3.in(and2.out()[0]);

        and10.in(in[4], not3.out()[0]);

        out[0] = and3.out()[0]; // A
        out[1] = and4.out()[0]; // B
        out[2] = and5.out()[0]; // C
        out[3] = and6.out()[0]; // D
        out[4] = and7.out()[0]; // E
        out[5] = and8.out()[0]; // F
        out[6] = and9.out()[0]; // G
        out[7] = and10.out()[0]; // Neg
        out[8] = and1.out()[0]; // Blk
        out[9] = and2.out()[0]; // S
    }

    public boolean[] outData() {
        return Arrays.copyOfRange(out, 0, 8);
    }

    public boolean outMinus() {
        return out[9];
    }

    public boolean outRippleNegative() {
        return out[7];
    }

    public boolean outRippleBlanking() {
        return out[8];
    }

    public void connect(SegmentDisplay display) {
        connect(display, (out) -> {
//            display.setSegments(Arrays.copyOfRange(out, 0, 7));
//            display.setMinus(out[9]);
        });
    }
}
