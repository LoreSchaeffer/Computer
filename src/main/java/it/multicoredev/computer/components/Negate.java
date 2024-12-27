package it.multicoredev.computer.components;

import it.multicoredev.computer.components.adders.Adder8Bit;
import it.multicoredev.computer.components.gates.Xor;
import it.multicoredev.computer.util.Utils;

public class Negate extends ChipComponent {
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Xor xor4 = new Xor();
    private final Xor xor5 = new Xor();
    private final Xor xor6 = new Xor();
    private final Xor xor7 = new Xor();
    private final Xor xor8 = new Xor();

    private final Adder8Bit adder = new Adder8Bit();

    public Negate() {
        super(
                9,
                8,
                "Negate",
                new String[]{"A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "En"},
                new String[]{"S0", "S1", "S2", "S3", "S4", "S5", "S6", "S7"}
        );
        run();
    }

    @Override
    protected void update() {
        xor1.in(in[0], in[8]);
        xor2.in(in[1], in[8]);
        xor3.in(in[2], in[8]);
        xor4.in(in[3], in[8]);
        xor5.in(in[4], in[8]);
        xor6.in(in[5], in[8]);
        xor7.in(in[6], in[8]);
        xor8.in(in[7], in[8]);

        adder.inA(xor8.out()[0], xor7.out()[0], xor6.out()[0], xor5.out()[0], xor4.out()[0], xor3.out()[0], xor2.out()[0], xor1.out()[0]);
        adder.inCarry(in[8]);

        System.arraycopy(Utils.flip(adder.sum()), 0, out, 0, 8);
    }
}
