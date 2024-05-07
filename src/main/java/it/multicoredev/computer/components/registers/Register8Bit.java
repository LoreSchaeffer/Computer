package it.multicoredev.computer.components.registers;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.util.ClockListener;

public class Register8Bit extends Component implements ClockListener {
    private final Register4Bit lowBites = new Register4Bit();
    private final Register4Bit highBites = new Register4Bit();

    private boolean[] data = new boolean[8];
    private boolean enable;
    private boolean clock;
    private final boolean[] out = new boolean[8];

    @Override
    protected void run() {
        lowBites.in(new boolean[]{data[0], data[1], data[2], data[3]}, enable);
        highBites.in(new boolean[]{data[4], data[5], data[6], data[7]}, enable);

        lowBites.tick(clock);
        highBites.tick(clock);

        boolean[] low = lowBites.out();
        boolean[] high = highBites.out();

        out[0] = low[0];
        out[1] = low[1];
        out[2] = low[2];
        out[3] = low[3];
        out[4] = high[0];
        out[5] = high[1];
        out[6] = high[2];
        out[7] = high[3];
    }

    public Register8Bit in(boolean[] data, boolean enable) {
        if (data.length != 8) throw new IllegalArgumentException("Data must be 8 bits long");

        this.data = data;
        this.enable = enable;
        run();
        return this;
    }

    public Register8Bit inData(boolean[] data) {
        if (data.length != 8) throw new IllegalArgumentException("Data must be 8 bits long");

        this.data = data;
        run();
        return this;
    }

    public Register8Bit inEnable(boolean enable) {
        this.enable = enable;
        run();
        return this;
    }

    @Override
    public void tick(boolean clock) {
        this.clock = clock;
        run();
    }
}
