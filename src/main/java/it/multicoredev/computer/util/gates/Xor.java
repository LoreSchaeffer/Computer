package it.multicoredev.computer.util.gates;

public class Xor implements DualInputGate {

    @Override
    public byte out(byte a, byte b) {
        return (byte) (((a == 0) && (b == 1)) || ((a == 1) && (b == 0)) ? 1 : 0);
    }
}
