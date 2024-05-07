package it.multicoredev.computer.util.gates;

public class Nand implements DualInputGate {
    public final And and = new And();
    public final Not not = new Not();

    @Override
    public byte out(final byte a, final byte b) {
        return not.out(and.out(a, b));
    }
}
