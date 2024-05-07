package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.components.DFlipFlop;
import it.multicoredev.computer.util.listeners.ClockListener;

public class Register4 implements ClockListener {
    private DFlipFlop ff0 = new DFlipFlop();
    private DFlipFlop ff1 = new DFlipFlop();
    private DFlipFlop ff2 = new DFlipFlop();
    private DFlipFlop ff3 = new DFlipFlop();

    public void setD0(byte d) {
        ff0.setD(d);
    }

    public void setD1(byte d) {
        ff1.setD(d);
    }

    public void setD2(byte d) {
        ff2.setD(d);
    }

    public void setD3(byte d) {
        ff3.setD(d);
    }

    public void setCe(byte ce) {
        ff0.setCe(ce);
        ff1.setCe(ce);
        ff2.setCe(ce);
        ff3.setCe(ce);
    }

    public void clr() {
        ff0.clr();
        ff1.clr();
        ff2.clr();
        ff3.clr();
    }

    public byte getOut0() {
        return ff0.getOut();
    }

    public byte getOut1() {
        return ff1.getOut();
    }

    public byte getOut2() {
        return ff2.getOut();
    }

    public byte getOut3() {
        return ff3.getOut();
    }

    @Override
    public void clock(boolean clock) {
        ff0.clock(clock);
        ff1.clock(clock);
        ff2.clock(clock);
        ff3.clock(clock);
    }
}
