package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.Component;

// The First elements of the arrays are the least significant bits
// Last elements of the arrays are the most significant bits
public class Adder4Bit extends Component {
    private final Adder adder0 = new Adder();
    private final Adder adder1 = new Adder();
    private final Adder adder2 = new Adder();
    private final Adder adder3 = new Adder();

    private boolean[] a = new boolean[4];
    private boolean[] b = new boolean[4];
    private boolean carryIn;
    private final boolean[] sum = new boolean[4];
    private boolean carryOut;

    @Override
    protected void run() {
        adder0.in(a[0], b[0], carryIn);
        adder1.in(a[1], b[1], adder0.carry());
        adder2.in(a[2], b[2], adder1.carry());
        adder3.in(a[3], b[3], adder2.carry());

        sum[0] = adder0.sum();
        sum[1] = adder1.sum();
        sum[2] = adder2.sum();
        sum[3] = adder3.sum();
        carryOut = adder3.carry();
    }

    public boolean[] sum() {
        return sum;
    }

    public boolean carry() {
        return carryOut;
    }

    public Adder4Bit in(boolean[] a, boolean[] b, boolean carryIn) {
        if (a.length != 4) throw new IllegalArgumentException("a must be 4 bits long");
        if (b.length != 4) throw new IllegalArgumentException("b must be 4 bits long");

        this.a = a;
        this.b = b;
        this.carryIn = carryIn;
run();
        return this;
    }

    public Adder4Bit inA(boolean[] a) {
        if (a.length != 4) throw new IllegalArgumentException("a must be 4 bits long");

        this.a = a;
run();
        return this;
    }

    public Adder4Bit inB(boolean[] b) {
        if (b.length != 4) throw new IllegalArgumentException("b must be 4 bits long");

        this.b = b;
run();
        return this;
    }

    public Adder4Bit inCarry(boolean carryIn) {
        this.carryIn = carryIn;
run();
        return this;
    }
}
