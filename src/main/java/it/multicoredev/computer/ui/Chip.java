package it.multicoredev.computer.ui;

import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.components.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Chip extends JPanel {
    private final Color color;
    private final String name;
    private boolean showLabels = false;
    private final Component component;
    private final String[] inLabels;
    private final String[] outLabels;

    public Chip(Color color, String name, Component component) {
        this.color = color;
        this.name = name;
        this.component = component;
        this.inLabels = new String[component.in().length];
        this.outLabels = new String[component.out().length];

        for (int i = 0; i < component.in().length; i++) {
            inLabels[i] = String.valueOf(i);
        }

        for (int i = 0; i < component.out().length; i++) {
            outLabels[i] = String.valueOf(i);
        }

        setPreferredSize(new Dimension(150, Math.max(inLabels.length, outLabels.length) * 40 + 60));
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    public void setLabels(String[] inLabels, String[] outLabels) {
        if (inLabels.length != this.inLabels.length) throw new IllegalArgumentException("Input labels must be " + this.inLabels.length + " long");
        if (outLabels.length != this.outLabels.length) throw new IllegalArgumentException("Output labels must be " + this.outLabels.length + " long");

        System.arraycopy(inLabels, 0, this.inLabels, 0, inLabels.length);
        System.arraycopy(outLabels, 0, this.outLabels, 0, outLabels.length);
    }

    public void setInLabels(String[] inLabels) {
        if (inLabels.length != this.inLabels.length) throw new IllegalArgumentException("Input labels must be " + this.inLabels.length + " long");

        System.arraycopy(inLabels, 0, this.inLabels, 0, inLabels.length);
    }

    public void setOutLabels(String[] outLabels) {
        if (outLabels.length != this.outLabels.length) throw new IllegalArgumentException("Output labels must be " + this.outLabels.length + " long");

        System.arraycopy(outLabels, 0, this.outLabels, 0, outLabels.length);
    }

    public void showLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }

    private void handleMouseClick(MouseEvent e) {
        int inSpacing = getHeight() / (component.in().length + 1);

        for (int i = 0; i < component.in().length; i++) {
            int y = inSpacing * (i + 1);

            Rectangle inputCircle = new Rectangle(0, y - 10, 20, 20);
            if (inputCircle.contains(e.getPoint())) {
                boolean[] in = component.in();
                in[i] = !in[i];
                component.in(in);

                repaint();
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xCenter = getWidth() / 2;
        int yCenter = getHeight() / 2;

        g2.setColor(Color.BLACK);
        g2.drawRect(10, 0, getWidth() - 20, getHeight() - 1);
        g2.setColor(color);
        g2.fillRect(11, 1, getWidth() - 21, getHeight() - 2);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        List<String> lines = splitTextIntoLines(name, g2, getWidth() - 58);

        int textHeight = g2.getFontMetrics().getHeight();
        int totalTextHeight = lines.size() * textHeight;
        int startY = yCenter - totalTextHeight / 2 + textHeight;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int textWidth = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, xCenter - textWidth / 2, startY + i * textHeight);
        }

        int inSpacing = getHeight() / (component.in().length + 1);
        for (int i = 0; i < component.in().length; i++) {
            int y = inSpacing * (i + 1);

            g2.setColor(Color.BLACK);
            g2.fillOval(0, y - 10, 20, 20);

            g2.setColor(component.in()[i] ? Colors.GREEN : Colors.RED);
            g2.fillOval(1, y - 9, 18, 18);

            if (showLabels) {
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.setColor(Color.BLACK);
                g2.drawString(inLabels[i], 22, y + 4);
            }
        }

        int outSpacing = getHeight() / (component.out().length + 1);
        for (int i = 0; i < component.out().length; i++) {
            int y = outSpacing * (i + 1);

            g2.setColor(Color.BLACK);
            g2.fillOval(getWidth() - 20, y - 10, 20, 20);

            g2.setColor(component.out()[i] ? Colors.GREEN : Colors.RED);
            g2.fillOval(getWidth() - 19, y - 9, 18, 18);

            if (showLabels) {
                String label = outLabels[i];

                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.setColor(Color.BLACK);

                int textWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, getWidth() - 22 - textWidth, y + 4);
            }
        }
    }

    private List<String> splitTextIntoLines(String text, Graphics2D g2, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (g2.getFontMetrics().stringWidth(testLine) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.isEmpty() ? word : " " + word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
