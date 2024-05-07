package it.multicoredev.computer.components.display;

import javax.swing.*;
import java.awt.*;

public class SegmentDisplay extends JPanel {
    private static final int SEGMENT_WIDTH = 70;
    private static final int SEGMENT_HEIGHT = 25;
    private static final int MINUS_WIDTH = 50;
    private static final int SEGMENT_DISTANCE = 2;
    private static final Color BACKGROUND = Color.BLACK;
    private static final Color SEGMENT_ON = Color.RED;
    private static final Color SEGMENT_OFF = new Color(21, 21, 21);

    private boolean[] segments = new boolean[8];
    /*
     * 0 -> MINUS
     * 1 -> TOP
     * 2 -> TOP-RIGHT
     * 3 -> BOTTOM-RIGHT
     * 4 -> BOTTOM
     * 5 -> BOTTOM-LEFT
     * 6 -> TOP-LEFT
     * 7 -> MIDDLE
     */

    public SegmentDisplay() {
        Dimension size = new Dimension(
                MINUS_WIDTH + SEGMENT_HEIGHT / 2 + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_HEIGHT / 2 + 20,
                SEGMENT_HEIGHT / 2 + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_HEIGHT / 2 + 20
        );

        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setBackground(BACKGROUND);
    }

    public void setSegment(int index, boolean state) {
        if (index < 0 || index >= segments.length) throw new IllegalArgumentException("Index out of bounds");

        segments[index] = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int xCenter = getWidth() / 2 + MINUS_WIDTH / 2;
        int yCenter = getHeight() / 2;
        int halfSegmentWidth = SEGMENT_WIDTH / 2;

        drawHSegment(g, segments[0] ? SEGMENT_ON : SEGMENT_OFF, xCenter - halfSegmentWidth - MINUS_WIDTH - SEGMENT_HEIGHT / 2 - SEGMENT_DISTANCE, yCenter, MINUS_WIDTH, SEGMENT_HEIGHT); // MINUS
        drawHSegment(g, segments[1] ? SEGMENT_ON : SEGMENT_OFF, xCenter - halfSegmentWidth, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT); // TOP
        drawVSegment(g, segments[2] ? SEGMENT_ON : SEGMENT_OFF, xCenter + halfSegmentWidth + SEGMENT_DISTANCE, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // TOP RIGHT
        drawVSegment(g, segments[3] ? SEGMENT_ON : SEGMENT_OFF, xCenter + halfSegmentWidth + SEGMENT_DISTANCE, yCenter + SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // BOTTOM RIGHT
        drawHSegment(g, segments[4] ? SEGMENT_ON : SEGMENT_OFF, xCenter - halfSegmentWidth, yCenter + SEGMENT_WIDTH + SEGMENT_DISTANCE * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT); // BOTTOM
        drawVSegment(g, segments[5] ? SEGMENT_ON : SEGMENT_OFF, xCenter - halfSegmentWidth - SEGMENT_DISTANCE, yCenter + SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // BOTTOM LEFT
        drawVSegment(g, segments[6] ? SEGMENT_ON : SEGMENT_OFF, xCenter - halfSegmentWidth - SEGMENT_DISTANCE, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // TOP LEFT
        drawHSegment(g, segments[7] ? SEGMENT_ON : SEGMENT_OFF, xCenter - halfSegmentWidth, yCenter, SEGMENT_WIDTH, SEGMENT_HEIGHT); // MIDDLE
    }

    private void drawHSegment(Graphics g, Color color, int x, int y, int width, int height) {
        int[] xPoints = {x, x + (height / 2), x + width - (height / 2), x + width, x + width - (height / 2), x + (height / 2)};
        int[] yPoints = {y, y + (height / 2), y + (height / 2), y, y - (height / 2), y - (height / 2)};

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    private void drawVSegment(Graphics g, Color color, int x, int y, int width, int height) {
        int[] xPoints = {x, x + (width / 2), x + (width / 2), x, x - (width / 2), x - (width / 2)};
        int[] yPoints = {y, y + (width / 2), y + height - (width / 2), y + height, y + height - (width / 2), y + (width / 2)};

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, xPoints.length);
    }
}
