package it.multicoredev.computer.components;

import it.multicoredev.computer.components.adders.Adder8Bit;
import it.multicoredev.computer.components.gates.Xor;

public class Negate extends Component {
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Xor xor4 = new Xor();
    private final Xor xor5 = new Xor();
    private final Xor xor6 = new Xor();
    private final Xor xor7 = new Xor();
    private final Xor xor8 = new Xor();
    private final Adder8Bit adder = new Adder8Bit();

    private boolean enable = false;
    private boolean[] in = new boolean[8];
    private boolean[] out = new boolean[8];

    @Override
    protected void run() {
        xor1.in(in[0], enable);
        xor2.in(in[1], enable);
        xor3.in(in[2], enable);
        xor4.in(in[3], enable);
        xor5.in(in[4], enable);
        xor6.in(in[5], enable);
        xor7.in(in[6], enable);
        xor8.in(in[7], enable);

        adder.inA(new boolean[]{false, false, false, false, false, false, false, false});
        adder.inB(new boolean[]{xor1.out(), xor2.out(), xor3.out(), xor4.out(), xor5.out(), xor6.out(), xor7.out(), xor8.out()});
        adder.inCarry(enable);

        out = adder.sum();
    }

    public void in(boolean[] in, boolean enable) {
        this.in = in;
        this.enable = enable;
        run();
    }

    private void enable(boolean enable) {
        this.enable = enable;
        run();
    }

    public boolean[] out() {
        return out;
    }
}
