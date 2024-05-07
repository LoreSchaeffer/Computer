package it.multicoredev.computer.cpu.alu;

import it.multicoredev.computer.cpu.alu.adder.Adder;
import it.multicoredev.computer.util.bitwise.BitwiseInverter;

public class Subber {
    private Adder adder;

    public Subber(int size) {
        adder = new Adder(size);
    }

    public void setA(String a) {
        adder.setA(a);
    }

    public void setB(String b) {
        BitwiseInverter inverter = new BitwiseInverter();
        inverter.setA(b);
        inverter.setEn((byte) 1);
        adder.setB(inverter.getOut());
    }

    public String getOut() {
        adder.setC((byte) 1);
        return adder.getOut().getVal1();
    }
}
