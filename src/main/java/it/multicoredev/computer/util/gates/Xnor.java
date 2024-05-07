package it.multicoredev.computer.util.gates;

public class Xnor implements DualInputGate {
    public final Xor xor = new Xor();
    public final Not not = new Not();

    @Override
    public byte out(byte a, byte b) {
        return not.out(xor.out(a, b));
    }
}
