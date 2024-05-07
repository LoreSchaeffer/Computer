package it.multicoredev.computer.util.components;

import it.multicoredev.computer.util.listeners.ClockListener;

public class DFlipFlop implements ClockListener {
    private byte d;
    private byte q = 0;
    private byte ce;

    public void setD(byte d) {
        this.d = d;
    }

    public void setCe(byte ce) {
        this.ce = ce;
    }

    public void clr() {
        ce = 0;
        d = 0;
        q = 0;
    }

    public byte getOut() {
        return q;
    }

    @Override
    public void clock(boolean clock) {
        if(clock) {
            if(ce == 1) {
                q = d;
            }
        }
    }
}
