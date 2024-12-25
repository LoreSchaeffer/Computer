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

    public static boolean[] fromBin(String bin) {
        boolean[] data = new boolean[bin.length()];
        for (int i = 0; i < bin.length(); i++) {
            data[i] = bin.charAt(i) == '1';
        }

        return data;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
