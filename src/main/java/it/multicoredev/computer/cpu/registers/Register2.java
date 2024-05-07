package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.components.DFlipFlop;
import it.multicoredev.computer.util.listeners.ClockListener;

public class Register2 implements ClockListener {
    private DFlipFlop ff0 = new DFlipFlop();
    private DFlipFlop ff1 = new DFlipFlop();

    public void setD0(byte d) {
        ff0.setD(d);
    }

    public void setD1(byte d) {
        ff1.setD(d);
    }

    public void setCe(byte ce) {
        ff0.setCe(ce);
        ff1.setCe(ce);
    }

    public void clr() {
        ff0.clr();
        ff1.clr();
    }

    public byte getOut0() {
        return ff0.getOut();
    }

    public byte getOut1() {
        return ff1.getOut();
    }

    @Override
    public void clock(boolean clock) {

    }
}
