package it.multicoredev.computer.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Text {

    private Text() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> wrap(String text, Graphics2D g, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (g.getFontMetrics().stringWidth(testLine) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.isEmpty() ? word : " " + word);
            }
        }

        if (!currentLine.isEmpty()) lines.add(currentLine.toString());

        return lines;
    }
}
