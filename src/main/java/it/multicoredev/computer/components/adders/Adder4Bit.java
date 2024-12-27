package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.ChipComponent;

public class Adder4Bit extends ChipComponent {
    private final Adder adder1 = new Adder();
    private final Adder adder2 = new Adder();
    private final Adder adder3 = new Adder();
    private final Adder adder4 = new Adder();

    public Adder4Bit() {
        super(
                9,
                5,
                "Adder 4B",
                new String[]{
                        "A0", "A1", "A2", "A3",
                        "B0", "B1", "B2", "B3",
                        "Cin"
                },
                new String[]{"S0", "S1", "S2", "S3", "Cout"}
        );
        run();
    }

    @Override
    protected void update() {
        adder1.in(in[0], in[4], in[8]);
        adder2.in(in[1], in[5], adder1.carry());
        adder3.in(in[2], in[6], adder2.carry());
        adder4.in(in[3], in[7], adder3.carry());

        out[0] = adder1.sum();
        out[1] = adder2.sum();
        out[2] = adder3.sum();
        out[3] = adder4.sum();
        out[4] = adder4.carry();
    }

    public Adder4Bit inA(boolean... a) {
        if (a.length != 4) throw new IllegalArgumentException("Input length must be 4");

        System.arraycopy(a, 0, in, 0, 4);
        run();

        return this;
    }

    public Adder4Bit inB(boolean... b) {
        if (b.length != 4) throw new IllegalArgumentException("Input length must be 4");

        System.arraycopy(b, 0, in, 4, 4);
        run();

        return this;
    }

    public Adder4Bit inCarry(boolean carry) {
        in[8] = carry;
        run();

        return this;
    }

    public boolean[] sum() {
        return new boolean[]{out[0], out[1], out[2], out[3]};
    }

    public boolean carry() {
        return out[4];
    }
}
