package it.multicoredev.computer.components.registers;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.TickingComponent;

public class Register4Bit extends ChipComponent implements TickingComponent {
    private final Register1Bit bit1 = new Register1Bit();
    private final Register1Bit bit2 = new Register1Bit();
    private final Register1Bit bit3 = new Register1Bit();
    private final Register1Bit bit4 = new Register1Bit();

    public Register4Bit() {
        super(6, 4, "Register 4B");
        run();
    }

    @Override
    protected void update() {
        bit1.in(in[0], in[4], in[5]);
        bit2.in(in[1], in[4], in[5]);
        bit3.in(in[2], in[4], in[5]);
        bit4.in(in[3], in[4], in[5]);

        out[0] = bit1.out()[0];
        out[1] = bit2.out()[0];
        out[2] = bit3.out()[0];
        out[3] = bit4.out()[0];
    }

    public Register4Bit inData(boolean[] data) {
        if (data.length != 4) throw new IllegalArgumentException("Data must be 4 bits long");

        in[0] = data[0];
        in[1] = data[1];
        in[2] = data[2];
        in[3] = data[3];
        run();

        return this;
    }

    public Register4Bit inEnable(boolean enable) {
        in[4] = enable;
        run();

        return this;
    }

    public Register4Bit clock(boolean clock) {
        in[5] = clock;
        run();

        return this;
    }
}
