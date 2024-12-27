package it.multicoredev.computer.util;

public class BitStrings {

    private BitStrings() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean[] fromString(String str) {
        if (str == null) throw new IllegalArgumentException("String cannot be null");
        if (!str.matches("[01]+")) throw new IllegalArgumentException("String must contain only 0 and 1");

        boolean bits[] = new boolean[str.length()];
        for (int i = 0; i < str.length(); i++) {
            bits[i] = str.charAt(i) == '1';
        }

        return bits;
    }

    public static String toString(boolean[] bits) {
        if (bits == null) return null;

        StringBuilder builder = new StringBuilder();
        for (boolean bit : bits) {
            builder.append(bit ? '1' : '0');
        }

        return builder.toString();
    }
}
