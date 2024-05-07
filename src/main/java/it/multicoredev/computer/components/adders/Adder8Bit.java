package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.Component;

import java.util.Arrays;

public class Adder8Bit extends Component {
    private final Adder4Bit lowAdder = new Adder4Bit();
    private final Adder4Bit highAdder = new Adder4Bit();

    private boolean[] a = new boolean[8];
    private boolean[] b = new boolean[8];
    private boolean carryIn;
    private final boolean[] sum = new boolean[8];
    private boolean carryOut;

    @Override
    protected void run() {
        lowAdder.in(Arrays.copyOfRange(a, 0, 4), Arrays.copyOfRange(b, 0, 4), carryIn);
        highAdder.in(Arrays.copyOfRange(a, 4, 8), Arrays.copyOfRange(b, 4, 8), lowAdder.carry());

        sum[0] = lowAdder.sum()[0];
        sum[1] = lowAdder.sum()[1];
        sum[2] = lowAdder.sum()[2];
        sum[3] = lowAdder.sum()[3];
        sum[4] = highAdder.sum()[0];
        sum[5] = highAdder.sum()[1];
        sum[6] = highAdder.sum()[2];
        sum[7] = highAdder.sum()[3];
        carryOut = highAdder.carry();
    }

    public Adder8Bit in(boolean[] a, boolean[] b, boolean carryIn) {
        if (a.length != 8) throw new IllegalArgumentException("a must be 8 bits long");
        if (b.length != 8) throw new IllegalArgumentException("b must be 8 bits long");

        this.a = a;
        this.b = b;
        this.carryIn = carryIn;
        run();
        return this;
    }

    public Adder8Bit inA(boolean[] a) {
        if (a.length != 8) throw new IllegalArgumentException("a must be 8 bits long");

        this.a = a;
        run();
        return this;
    }

    public Adder8Bit inB(boolean[] b) {
        if (b.length != 8) throw new IllegalArgumentException("b must be 8 bits long");

        this.b = b;
        run();
        return this;
    }

    public Adder8Bit inCarry(boolean carryIn) {
        this.carryIn = carryIn;
        run();
        return this;
    }

    public boolean[] sum() {
        return sum;
    }

    public boolean carry() {
        return carryOut;
    }
}
