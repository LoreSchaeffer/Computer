package it.multicoredev.computer.util.bitwise;

public class Replicate {
    private final int size;
    private byte a;

    public Replicate(int size) {
        this.size = size;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public String getOut() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(a);
        }
        return builder.toString();
    }
}
