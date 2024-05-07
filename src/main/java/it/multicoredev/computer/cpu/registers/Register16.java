package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.listeners.ClockListener;

public class Register16 implements ClockListener {
    private Register8 reg0 = new Register8();
    private Register8 reg1 = new Register8();

    public void setD(String d) {
        reg0.setD(d.substring(0, 8));
        reg1.setD(d.substring(8));
    }

    public void setCe(byte ce) {
        reg0.setCe(ce);
        reg1.setCe(ce);
    }

    public void clr() {
        reg0.clr();
        reg1.clr();
    }

    public String getOut() {
        return reg0.getOut() + reg1.getOut();
    }

    @Override
    public void clock(boolean clock) {
        reg0.clock(clock);
        reg1.clock(clock);
    }
}
