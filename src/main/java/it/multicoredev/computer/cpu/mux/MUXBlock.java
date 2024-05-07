package it.multicoredev.computer.cpu.mux;

public class MUXBlock {
    private boolean a;
    private boolean b;
    private boolean sel;

    public void setA(boolean a) {
        this.a = a;
    }

    public void setA(byte a) {
        this.a = a == 1;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public void setB(byte b) {
        this.b = b == 1;
    }

    public void setSel(boolean sel) {
        this.sel = sel;
    }

    public void setSel(byte sel) {
        this.sel = sel == 1;
    }

    public boolean getOut() {
        return ((a && !sel) || (sel && b));
    }
}
