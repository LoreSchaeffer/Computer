package it.multicoredev.computer.components.registers;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.util.ClockListener;

public class Register4Bit extends Component implements ClockListener {
    private final Register1Bit bit0 = new Register1Bit();
    private final Register1Bit bit1 = new Register1Bit();
    private final Register1Bit bit2 = new Register1Bit();
    private final Register1Bit bit3 = new Register1Bit();

    private boolean[] data = new boolean[4];
    private boolean enable;
    private boolean clock;
    private final boolean[] out = new boolean[4];

    @Override
    protected void run() {
        bit0.in(data[0], enable);
        bit1.in(data[1], enable);
        bit2.in(data[2], enable);
        bit3.in(data[3], enable);

        bit0.tick(clock);
        bit1.tick(clock);
        bit2.tick(clock);
        bit3.tick(clock);

        out[0] = bit0.out();
        out[1] = bit1.out();
        out[2] = bit2.out();
        out[3] = bit3.out();
    }

    public Register4Bit in(boolean[] data, boolean enable) {
        if (data.length != 4) throw new IllegalArgumentException("Data must be 4 bits long");

        this.data = data;
        this.enable = enable;
        run();
        return this;
    }

    public Register4Bit inData(boolean[] data) {
        if (data.length != 4) throw new IllegalArgumentException("Data must be 4 bits long");

        this.data = data;
        run();
        return this;
    }

    public Register4Bit inEnable(boolean enable) {
        this.enable = enable;
        run();
        return this;
    }

    public boolean[] out() {
        return out;
    }

    @Override
    public void tick(boolean clock) {
        this.clock = clock;
        run();
    }
}
