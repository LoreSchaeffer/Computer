package it.multicoredev.computer.util.gates;

public class And implements DualInputGate {

    @Override
    public byte out(final byte a, final byte b) {
        return (byte) (a == 1 && b == 1 ? 1 : 0);
    }
}
