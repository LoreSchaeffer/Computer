package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.ChipComponent;

import java.util.Arrays;

public class Adder8Bit extends ChipComponent {
    private final Adder4Bit lowAdder = new Adder4Bit();
    private final Adder4Bit highAdder = new Adder4Bit();

    public Adder8Bit() {
        super(
                17,
                9,
                "Adder 8B",
                new String[]{
                        "A0", "A1", "A2", "A3",
                        "A4", "A5", "A6", "A7",

                        "B0", "B1", "B2", "B3",
                        "B4", "B5", "B6", "B7",

                        "Cin"
                },
                new String[]{"S0", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "Cout"}
        );
        run();
    }

    @Override
    protected void update() {
        lowAdder.inA(Arrays.copyOfRange(in, 0, 4));
        lowAdder.inB(Arrays.copyOfRange(in, 8, 12));
        lowAdder.inCarry(in[16]);

        highAdder.inA(Arrays.copyOfRange(in, 4, 8));
        highAdder.inB(Arrays.copyOfRange(in, 12, 16));
        highAdder.inCarry(lowAdder.carry());

        System.arraycopy(lowAdder.sum(), 0, out, 0, 4);
        System.arraycopy(highAdder.sum(), 0, out, 4, 4);
        out[8] = highAdder.carry();
    }

    public Adder8Bit inA(boolean... a) {
        if (a.length != 8) throw new IllegalArgumentException("Input A must be 8 bits long");

        System.arraycopy(a, 0, in, 0, 8);
        run();

        return this;
    }

    public Adder8Bit inB(boolean... b) {
        if (b.length != 8) throw new IllegalArgumentException("Input B must be 8 bits long");

        System.arraycopy(b, 0, in, 8, 8);
        run();

        return this;
    }

    public Adder8Bit inCarry(boolean carry) {
        in[16] = carry;
        run();

        return this;
    }

    public boolean[] sum() {
        return Arrays.copyOfRange(out, 0, 8);
    }

    public boolean carry() {
        return out[8];
    }
}
