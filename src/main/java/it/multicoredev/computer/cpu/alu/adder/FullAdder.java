package it.multicoredev.computer.cpu.alu.adder;

public class FullAdder {
    private HalfAdder adder0 = new HalfAdder();
    private HalfAdder adder1 = new HalfAdder();

    public void setA(byte a) {
        adder0.setA(a);
    }

    public void setB(byte b) {
        adder0.setB(b);
    }

    public void setC(byte c) {
        adder1.setB(c);
    }

    public byte[] getOut() {
        byte[] res1 = adder0.getOut();
        adder1.setA(res1[0]);
        byte[] res2 = adder1.getOut();
        return new byte[]{res2[0], (byte) (((res1[1] == 1) || (res2[1] == 1)) ? 1 : 0)};
    }
}
