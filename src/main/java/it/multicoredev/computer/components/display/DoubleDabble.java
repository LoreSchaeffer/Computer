package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.ChipComponent;

public class DoubleDabble extends ChipComponent {
    private final Dabble dabble1 = new Dabble();

    private final Dabble dabble2 = new Dabble();

    private final Dabble dabble3 = new Dabble();

    private final Dabble dabble4 = new Dabble();
    private final Dabble dabble5 = new Dabble();

    private final Dabble dabble6 = new Dabble();
    private final Dabble dabble7 = new Dabble();

    public DoubleDabble() {
        super(8, 10, "Double Dabble");
        run();
    }

    @Override
    protected void update() {
        dabble1.in(false, in[0], in[1], in[2]);

        dabble2.in(dabble1.out()[1], dabble1.out()[2], dabble1.out()[3], in[3]);

        dabble3.in(dabble2.out()[1], dabble2.out()[2], dabble2.out()[3], in[4]);

        dabble4.in(false, dabble1.out()[0], dabble2.out()[0], dabble3.out()[0]);
        dabble5.in(dabble3.out()[1], dabble3.out()[2], dabble3.out()[3], in[5]);

        dabble6.in(dabble4.out()[1], dabble4.out()[2], dabble4.out()[3], dabble5.out()[0]);
        dabble7.in(dabble5.out()[1], dabble5.out()[2], dabble5.out()[3], in[6]);

        out[0] = dabble4.out()[0];
        out[1] = dabble6.out()[0];
        out[2] = dabble6.out()[1];
        out[3] = dabble6.out()[2];
        out[4] = dabble6.out()[3];
        out[5] = dabble7.out()[0];
        out[6] = dabble7.out()[1];
        out[7] = dabble7.out()[2];
        out[8] = dabble7.out()[3];
        out[9] = in[7];
    }
}
