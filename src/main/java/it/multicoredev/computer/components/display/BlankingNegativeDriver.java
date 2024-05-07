package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Not;
import it.multicoredev.computer.components.gates.advanced.Or4;

public class BlankingNegativeDriver extends Component {
    private final Or4 or4 = new Or4();
    private final SegmentDisplayDriver driver = new SegmentDisplayDriver();

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

    private boolean[] data = new boolean[4];
    private boolean rippleNegativeIn = false;
    private boolean rippleBlankingIn = false;

    private boolean[] out = new boolean[7];
    private boolean outMinus = false;
    private boolean rippleNegativeOut = false;
    private boolean rippleBlankingOut = false;

    @Override
    protected void run() {
        or4.in(data);
        driver.in(data);

        not1.in(or4.out());

        and1.in(rippleBlankingIn, not1.out());

        not2.in(and1.out());

        and2.in(rippleNegativeIn, not2.out());

        and3.in(driver.out()[0], not2.out());
        and4.in(driver.out()[1], not2.out());
        and5.in(driver.out()[2], not2.out());
        and6.in(driver.out()[3], not2.out());
        and7.in(driver.out()[4], not2.out());
        and8.in(driver.out()[5], not2.out());
        and9.in(driver.out()[6], not2.out());

        not3.in(and2.out());

        and10.in(rippleNegativeIn, not3.out());

        out[0] = and3.out();
        out[1] = and4.out();
        out[2] = and5.out();
        out[3] = and6.out();
        out[4] = and7.out();
        out[5] = and8.out();
        out[6] = and9.out();

        outMinus = and2.out();

        rippleNegativeOut = and10.out();
        rippleBlankingOut = and1.out();
    }

    public BlankingNegativeDriver in(boolean[] data, boolean rippleNegativeIn, boolean rippleBlankingIn) {
        this.data = data;
        this.rippleNegativeIn = rippleNegativeIn;
        this.rippleBlankingIn = rippleBlankingIn;
        run();
        return this;
    }

    public BlankingNegativeDriver inData(boolean[] data) {
        this.data = data;
        run();
        return this;
    }

    public BlankingNegativeDriver inRippleNegativeIn(boolean rippleNegativeIn) {
        this.rippleNegativeIn = rippleNegativeIn;
        run();
        return this;
    }

    public BlankingNegativeDriver inRippleBlankingIn(boolean rippleBlankingIn) {
        this.rippleBlankingIn = rippleBlankingIn;
        run();
        return this;
    }

    public boolean[] out() {
        return out;
    }

    public boolean outMinus() {
        return outMinus;
    }

    public boolean rippleNegativeOut() {
        return rippleNegativeOut;
    }

    public boolean rippleBlankingOut() {
        return rippleBlankingOut;
    }
}
