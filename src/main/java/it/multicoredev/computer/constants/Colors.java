package it.multicoredev.computer.constants;

import java.awt.*;
import java.util.Random;

public class Colors {
    public static final Color BLACK = new Color(0x000000);
    public static final Color NOT_BLACK = new Color(0x121212);
    public static final Color ALMOST_BLACK = new Color(0x212121);
    public static final Color WHITE = new Color(0xffffff);
    public static final Color RED = new Color(0xf44336);
    public static final Color GREEN = new Color(0x4caf50);
    public static final Color YELLOW = new Color(0xffeb3b);

    public static Color getRandom(Class<?> seed) {
        return Color.getHSBColor(new Random(seed.getName().hashCode()).nextFloat(), 0.3f, 1f);
    }
}
