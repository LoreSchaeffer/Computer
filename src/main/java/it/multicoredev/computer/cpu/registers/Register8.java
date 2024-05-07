package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.listeners.ClockListener;

public class Register8 implements ClockListener {
    private Register4 reg0 = new Register4();
    private Register4 reg1 = new Register4();

    public void setD(String d) {
        char[] chars = d.toCharArray();
        reg0.setD0(Byte.parseByte("" + chars[0]));
        reg0.setD1(Byte.parseByte("" + chars[1]));
        reg0.setD2(Byte.parseByte("" + chars[2]));
        reg0.setD3(Byte.parseByte("" + chars[3]));
        reg1.setD0(Byte.parseByte("" + chars[4]));
        reg1.setD1(Byte.parseByte("" + chars[5]));
        reg1.setD2(Byte.parseByte("" + chars[6]));
        reg1.setD3(Byte.parseByte("" + chars[7]));
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
        StringBuilder builder = new StringBuilder();
        builder.append(reg0.getOut0());
        builder.append(reg0.getOut1());
        builder.append(reg0.getOut2());
        builder.append(reg0.getOut3());
        builder.append(reg1.getOut0());
        builder.append(reg1.getOut1());
        builder.append(reg1.getOut2());
        builder.append(reg1.getOut3());

        return builder.toString();
    }

    @Override
    public void clock(boolean clock) {
        reg0.clock(clock);
        reg1.clock(clock);
    }
}
