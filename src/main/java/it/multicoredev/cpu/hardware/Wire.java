package it.multicoredev.cpu.hardware;

public class Wire {
    private boolean state;

    public Wire(boolean state) {
        this.state = state;
    }

    public Wire() {
        this.state = false;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
