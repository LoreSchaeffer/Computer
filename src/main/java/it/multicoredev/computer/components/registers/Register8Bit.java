package it.multicoredev.computer.components.registers;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.TickingComponent;

public class Register8Bit extends ChipComponent implements TickingComponent {
    private final Register4Bit lowBites = new Register4Bit();
    private final Register4Bit highBites = new Register4Bit();

    public Register8Bit() {
        super(10, 8, "Register 8B");
        run();
    }

    @Override
    protected void update() {
        lowBites.in(in[0], in[1], in[2], in[3], in[8], in[9]);
        highBites.in(in[4], in[5], in[6], in[7], in[8], in[9]);

        System.arraycopy(lowBites.out(), 0, out, 0, 4);
        System.arraycopy(highBites.out(), 0, out, 4, 4);
    }

    public Register8Bit inData(boolean[] data) {
        if (data.length != 8) throw new IllegalArgumentException("Data must be 8 bits long");

        System.arraycopy(data, 0, in, 0, 8);
        run();

        return this;
    }

    public Register8Bit inEnable(boolean enable) {
        in[8] = enable;
        run();

        return this;
    }

    @Override
    public Register8Bit clock(boolean clock) {
        in[9] = clock;
        run();

        return this;
    }
}
