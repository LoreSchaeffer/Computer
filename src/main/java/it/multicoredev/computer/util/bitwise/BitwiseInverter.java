package it.multicoredev.computer.util.bitwise;

import it.multicoredev.computer.util.gates.Xor;

public class BitwiseInverter {
    private static final Xor XOR = new Xor();

    private String a;
    private boolean en;

    public void setA(String a) {
        this.a = a;
    }

    public void setEn(byte en) {
        this.en = en == 1;
    }

    public String getOut() {
        StringBuilder builder = new StringBuilder();
        for(char c : a.toCharArray()) {
            builder.append(XOR.out(Byte.parseByte(""+ c), (byte) (en ? 1 : 0)));
        }

        return builder.toString();
    }
}
