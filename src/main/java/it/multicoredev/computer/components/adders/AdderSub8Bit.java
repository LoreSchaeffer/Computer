package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.gates.Xor;

import java.util.Arrays;

public class AdderSub8Bit extends ChipComponent {
    private final Adder8Bit adder = new Adder8Bit();
    private final Or or = new Or();
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Xor xor4 = new Xor();
    private final Xor xor5 = new Xor();
    private final Xor xor6 = new Xor();
    private final Xor xor7 = new Xor();
    private final Xor xor8 = new Xor();

    public AdderSub8Bit() {
        super(
                18,
                9,
                "AdderSub8Bit",
                new String[]{
                        "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
                        "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7",
                        "Carry In", "Sub"
                },
                new String[]{
                        "Sum0", "Sum1", "Sum2", "Sum3", "Sum4", "Sum5", "Sum6", "Sum7",
                        "Carry Out"
                }
        );
        run();
    }

    @Override
    protected void update() {
        xor1.in(in[8], in[17]);
        xor2.in(in[9], in[17]);
        xor3.in(in[10], in[17]);
        xor4.in(in[11], in[17]);
        xor5.in(in[12], in[17]);
        xor6.in(in[13], in[17]);
        xor7.in(in[14], in[17]);
        xor8.in(in[15], in[17]);

        or.in(in[16], in[17]);

        adder.inA(Arrays.copyOfRange(in, 0, 8));
        adder.inB(xor1.out()[0], xor2.out()[0], xor3.out()[0], xor4.out()[0], xor5.out()[0], xor6.out()[0], xor7.out()[0], xor8.out()[0]);
        adder.inCarry(or.out()[0]);

        System.arraycopy(adder.sum(), 0, out, 0, 8);
        out[8] = adder.carry();
    }

    public AdderSub8Bit inA(boolean... a) {
        if (a.length != 8) throw new IllegalArgumentException("a must be 8 bits long");

        System.arraycopy(a, 0, in, 0, 8);
        run();

        return this;
    }

    public AdderSub8Bit inB(boolean... b) {
        if (b.length != 8) throw new IllegalArgumentException("b must be 8 bits long");

        System.arraycopy(b, 0, in, 8, 8);
        run();

        return this;
    }

    public AdderSub8Bit inCarry(boolean inCarry) {
        in[16] = inCarry;
        run();

        return this;
    }

    public AdderSub8Bit inSub(boolean sub) {
        in[17] = sub;
        run();

        return this;
    }

    public boolean[] result() {
        return Arrays.copyOfRange(out, 0, 8);
    }

    public boolean carryOut() {
        return out[8];
    }
}
