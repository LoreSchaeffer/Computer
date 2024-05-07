package it.multicoredev.computer.cpu.alu.adder;

public class HalfAdder {
    private boolean a;
    private boolean b;

    public void setA(byte a) {
        this.a = a == 1;
    }

    public void setB(byte b) {
        this.b = b == 1;
    }

    public byte[] getOut() {
        boolean sum = ((!a && b) || (a && !b));
        boolean cout = (a && b);

        return new byte[]{sum ? (byte) 1 : (byte) 0, cout ? (byte) 1 : (byte) 0};
    }
}
