package it.multicoredev.computer.util.gates;

public class Nor implements DualInputGate {
    public final Or or = new Or();
    public final Not not = new Not();

    @Override
    public byte out(final byte a, final byte b) {
        return not.out(or.out(a, b));
    }
}
