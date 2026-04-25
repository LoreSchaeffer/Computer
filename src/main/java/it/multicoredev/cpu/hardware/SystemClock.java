package it.multicoredev.cpu.hardware;

public final class SystemClock {
    private final Wire clockWire;
    private boolean state;

    public SystemClock(Wire clockWire) {
        this.clockWire = clockWire;
    }

    public void tick() {
        this.state = !this.state;
        this.clockWire.setState(this.state);
    }
}
