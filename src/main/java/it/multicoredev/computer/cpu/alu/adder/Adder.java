package it.multicoredev.computer.cpu.alu.adder;

import it.multicoredev.computer.util.BiVal;

public class Adder {
    private FullAdder[] adders;

    public Adder(int size) {
        adders = new FullAdder[size];

        for (int i = 0; i < size; i++) {
            adders[i] = new FullAdder();
        }
    }

    public void setA(String a) {
        char[] chars = a.toCharArray();
        int len = chars.length - 1;
        for (int i = 0; i < adders.length; i++) {
            adders[i].setA(Byte.parseByte("" + chars[len - i]));
        }
    }

    public void setB(String b) {
        char[] chars = b.toCharArray();
        int len = chars.length - 1;
        for (int i = 0; i < adders.length; i++) {
            adders[i].setB(Byte.parseByte("" + chars[len - i]));
        }
    }

    public void setC(byte c) {
        adders[0].setC(c);
    }

    public BiVal<String, Byte> getOut() {
        StringBuilder builder = new StringBuilder();

        byte c = -1;

        for (int i = 0; i < adders.length; i++) {
            if (i != 0) adders[i].setC(c);
            byte[] result = adders[i].getOut();
            builder.append(result[0]);
            c = result[1];
        }

        StringBuilder sum = new StringBuilder();
        for(int i = builder.length() - 1; i >= 0; i--) {
            sum.append(builder.charAt(i));
        }

        return new BiVal<>(sum.toString(), c);
    }
}
