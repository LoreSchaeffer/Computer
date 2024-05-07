package it.multicoredev.computer.cpu.alu;

import it.multicoredev.computer.cpu.alu.adder.Adder;
import it.multicoredev.computer.cpu.mux.MUX41;
import it.multicoredev.computer.util.BiVal;
import it.multicoredev.computer.util.bitwise.BitwiseAnd;
import it.multicoredev.computer.util.bitwise.BitwiseInverter;
import it.multicoredev.computer.util.bitwise.Replicate;
import it.multicoredev.computer.util.gates.Not;

public class ALU {
    public static final Not NOT = new Not();
    private Adder adder = new Adder(8);
    private BitwiseAnd and0 = new BitwiseAnd();
    private BitwiseAnd and1 = new BitwiseAnd();
    private BitwiseInverter inverter = new BitwiseInverter();
    private Replicate replicate = new Replicate(8);
    private MUX41 mux = new MUX41();

    public void setA(String a) {
        adder.setA(a);
        and1.setA(a);
        mux.setC(a);
    }

    public void setB(String b) {
        inverter.setA(b);
        and1.setB(b);
        mux.setD(b);
    }

    public void setS0(byte s) {
        mux.setSel0(s == 1);
    }

    public void setS1(byte s) {
        mux.setSel1(s == 1);
    }

    public void setS2(byte s) {
        adder.setC(s);
    }

    public void setS3(byte s) {
        inverter.setEn(s);
    }

    public void setS4(byte s) {
        replicate.setA(NOT.out(s));
    }

    public BiVal<String, Byte> getOut() {
        and0.setA(replicate.getOut());
        and0.setB(inverter.getOut());
        adder.setB(and0.getOut());
        BiVal<String, Byte> adderResult = adder.getOut();
        mux.setA(adderResult.getVal1());
        mux.setB(and1.getOut());

        return new BiVal<>(mux.getOut(), adderResult.getVal2());
    }

    /*
     *  S4  S3  S2   S1  S0  Z
     *  0   0   0   0   0   ADD (A+B)
     *  0   0   0   0   1   BITWISE AND (A&B)
     *  0   0   0   1   0   INPUT A
     *  0   0   0   1   1   INPUT B
     *  0   1   1   0   0   SUBTRACT (A-B)
     *  1   0   1   0   0   INCREMENT (A+1)
     *  1   0   0   0   0   INPUT A
     *  0   0   1   0   0   ADD (A+B)+1
     *  0   1   0   0   0   SUBTRACT (A-B)-1
     */
}
