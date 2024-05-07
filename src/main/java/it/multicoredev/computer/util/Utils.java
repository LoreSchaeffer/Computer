package it.multicoredev.computer.util;

public class Utils {

    private Utils() {
    }

    public static boolean[] flip(boolean[] data) {
        boolean[] flipped = new boolean[data.length];
        for (int i = 0; i < data.length; i++) {
            flipped[i] = data[data.length - i - 1];
        }

        return flipped;
    }
}
