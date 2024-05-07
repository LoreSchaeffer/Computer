package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.gates.Xor;

public class AddSub8Bit extends Component {
    private final Adder8Bit adder = new Adder8Bit();
    private final Or or = new Or();
    private final Xor xor0 = new Xor();
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Xor xor4 = new Xor();
    private final Xor xor5 = new Xor();
    private final Xor xor6 = new Xor();
    private final Xor xor7 = new Xor();

    private boolean[] a = new boolean[8];
    private boolean[] b = new boolean[8];
    private boolean carryIn;
    private boolean sub;
    private boolean[] result = new boolean[8];
    private boolean carryOut;

    @Override
    protected void run() {
        xor0.in(b[0], sub);
        xor1.in(b[1], sub);
        xor2.in(b[2], sub);
        xor3.in(b[3], sub);
        xor4.in(b[4], sub);
        xor5.in(b[5], sub);
        xor6.in(b[6], sub);
        xor7.in(b[7], sub);

        or.in(carryIn, sub);

        adder.in(a, new boolean[]{xor0.out(), xor1.out(), xor2.out(), xor3.out(), xor4.out(), xor5.out(), xor6.out(), xor7.out()}, or.out());

        result = adder.sum();
        carryOut = adder.carry();
    }

    public AddSub8Bit in(boolean[] a, boolean[] b, boolean carryIn, boolean sub) {
        if (a.length != 8) throw new IllegalArgumentException("a must be 8 bits long");
        if (b.length != 8) throw new IllegalArgumentException("b must be 8 bits long");

        this.a = a;
        this.b = b;
        this.carryIn = carryIn;
        this.sub = sub;
        run();
        return this;
    }

    public AddSub8Bit inA(boolean[] a) {
        if (a.length != 8) throw new IllegalArgumentException("a must be 8 bits long");

        this.a = a;
        run();
        return this;
    }

    public AddSub8Bit inB(boolean[] b) {
        if (b.length != 8) throw new IllegalArgumentException("b must be 8 bits long");

        this.b = b;
        run();
        return this;
    }

    public AddSub8Bit inCarry(boolean carryIn) {
        this.carryIn = carryIn;
        run();
        return this;
    }

    public AddSub8Bit inSub(boolean sub) {
        this.sub = sub;
        run();
        return this;
    }

    public boolean[] result() {
        return result;
    }

    public boolean carryOut() {
        return carryOut;
    }
}
