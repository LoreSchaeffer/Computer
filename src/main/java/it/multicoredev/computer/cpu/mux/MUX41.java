package it.multicoredev.computer.cpu.mux;

public class MUX41 {
    private MUX mux0 = new MUX(8);
    private MUX mux1 = new MUX(8);
    private MUX mux2 = new MUX(8);

    public void setA(String a) {
        mux0.setA(a);
    }

    public void setB(String b) {
        mux0.setB(b);
    }

    public void setC(String c) {
        mux1.setA(c);
    }

    public void setD(String d) {
        mux1.setB(d);
    }

    public void setSel0(boolean sel) {
        mux0.setSel(sel);
        mux1.setSel(sel);
    }

    public void setSel1(boolean sel) {
        mux2.setSel(sel);
    }

    public String getOut() {
        mux2.setA(mux0.getOut());
        mux2.setB(mux1.getOut());
        return mux2.getOut();
    }
}
