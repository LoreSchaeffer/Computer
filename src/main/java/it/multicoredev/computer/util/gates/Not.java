package it.multicoredev.computer.util.gates;

public class Not implements SingleInputGate {

    @Override
    public byte out(final byte input) {
        return input == 1 ? (byte) 0 : (byte) 1;
    }
}
