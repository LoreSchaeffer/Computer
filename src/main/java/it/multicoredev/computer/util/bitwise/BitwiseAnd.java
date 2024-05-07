package it.multicoredev.computer.util.bitwise;

import it.multicoredev.computer.util.gates.And;

public class BitwiseAnd {
    private static final And AND = new And();
    private String a;
    private String b;

    public void setA(String a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getOut() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < a.length(); i++) {
            builder.append(AND.out(
                    Byte.parseByte("" + a.charAt(i)),
                    Byte.parseByte("" + b.charAt(i))));
        }

        return builder.toString();
    }
}
