package it.multicoredev.computer.cpu.decoder;

import it.multicoredev.computer.util.listeners.ClockListener;

public class SequenceGenerator implements ClockListener {
    private final String[] states = new String[]{"1000", "0100", "0010", "0001"};
    private int state = 0;
    private byte ce;

    public void setCe(byte ce) {
        this.ce = ce;
    }

    public byte fetch() {
        if (state == 0) return 1;
        return 0;
    }

    public byte decode() {
        if (state == 1) return 1;
        return 0;
    }

    public byte execute() {
        if (state == 2) return 1;
        return 0;
    }

    public byte increment() {
        if (state == 3) return 1;
        return 0;
    }

    @Override
    public void clock(boolean clock) {
        if (ce == 1) {
            state++;

            if (state >= states.length) {
                state = 0;
            }
        }
    }
}
