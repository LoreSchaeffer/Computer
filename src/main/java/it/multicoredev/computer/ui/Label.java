package it.multicoredev.computer.ui;

import javax.swing.*;
import java.awt.*;

public class Label extends JPanel {
    private static final Font FONT = new Font("Arial", Font.BOLD, 11);
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 0.6f);
    private static final int H_PADDING = 8;
    private static final int V_PADDING = 2;

    private final String text;

    public Label(String text) {
        this.text = text;

        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(FONT);
        int width = metrics.stringWidth(text) + H_PADDING * 2;
        int height = metrics.getHeight() + V_PADDING * 2;
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setFont(FONT);
        FontMetrics metrics = g.getFontMetrics(FONT);

        int width = metrics.stringWidth(text) + H_PADDING * 2;
        int height = metrics.getHeight() + V_PADDING * 2;

        g.setColor(BACKGROUND_COLOR);
        g.fillRoundRect(0, 0, width, height, 10, 10);

        g.setColor(Color.WHITE);
        g.drawString(text, H_PADDING, V_PADDING + metrics.getAscent());
    }
}
