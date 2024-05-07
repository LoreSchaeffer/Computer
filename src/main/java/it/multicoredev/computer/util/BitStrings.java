package it.multicoredev.computer.util;

public class BitStrings {

    private BitStrings() {
    }

    public static byte[] fromString(String str) {
        if (str == null) throw new IllegalArgumentException("String cannot be null");
        if (!str.matches("[01]+")) throw new IllegalArgumentException("String must contain only 0 and 1");

        byte[] bytes = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            bytes[i] = Byte.parseByte(String.valueOf(str.charAt(i)));
        }

        return bytes;
    }

    public static String toString(byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException("Bytes cannot be null");

        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            if (b != 0 && b != 1) throw new IllegalArgumentException("Byte must be 0 or 1");
            builder.append(b);
        }

        return builder.toString();
    }
}
