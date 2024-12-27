package it.multicoredev.computer.ui;

import it.multicoredev.computer.constants.Colors;

import javax.swing.*;
import java.awt.*;

public class SegmentDisplay extends JPanel {
    private static final int SEGMENT_WIDTH = 70;
    private static final int SEGMENT_HEIGHT = 25;
    private static final int MINUS_WIDTH = 50;
    private static final int SEGMENT_DISTANCE = 2;

    private boolean[] segments = new boolean[7];
    private boolean minus = false;

    /*
     * 0 -> TOP
     * 1 -> TOP-RIGHT
     * 2 -> BOTTOM-RIGHT
     * 3 -> BOTTOM
     * 4 -> BOTTOM-LEFT
     * 5 -> TOP-LEFT
     * 6 -> MIDDLE
     */

    public SegmentDisplay() {
        Dimension size = new Dimension(
                MINUS_WIDTH + SEGMENT_HEIGHT / 2 + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_HEIGHT / 2 + 20,
                SEGMENT_HEIGHT / 2 + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_HEIGHT / 2 + 20
        );

        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setBackground(Colors.BLACK);
    }

    public void setSegments(boolean... segments) {
        if (segments.length != 7) throw new IllegalArgumentException("Input must be 7 bits long");

        this.segments = segments;
        repaint();
    }

    public void setMinus(boolean minus) {
        this.minus = minus;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int xCenter = getWidth() / 2 + MINUS_WIDTH / 2;
        int yCenter = getHeight() / 2;
        int halfSegmentWidth = SEGMENT_WIDTH / 2;

        drawHSegment(g, minus ? Colors.RED : Colors.ALMOST_BLACK, xCenter - halfSegmentWidth - MINUS_WIDTH - SEGMENT_HEIGHT / 2 - SEGMENT_DISTANCE, yCenter, MINUS_WIDTH, SEGMENT_HEIGHT); // MINUS
        drawHSegment(g, segments[0] ? Colors.RED : Colors.ALMOST_BLACK, xCenter - halfSegmentWidth, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT); // TOP
        drawVSegment(g, segments[1] ? Colors.RED : Colors.ALMOST_BLACK, xCenter + halfSegmentWidth + SEGMENT_DISTANCE, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // TOP RIGHT
        drawVSegment(g, segments[2] ? Colors.RED : Colors.ALMOST_BLACK, xCenter + halfSegmentWidth + SEGMENT_DISTANCE, yCenter + SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // BOTTOM RIGHT
        drawHSegment(g, segments[3] ? Colors.RED : Colors.ALMOST_BLACK, xCenter - halfSegmentWidth, yCenter + SEGMENT_WIDTH + SEGMENT_DISTANCE * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT); // BOTTOM
        drawVSegment(g, segments[4] ? Colors.RED : Colors.ALMOST_BLACK, xCenter - halfSegmentWidth - SEGMENT_DISTANCE, yCenter + SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // BOTTOM LEFT
        drawVSegment(g, segments[5] ? Colors.RED : Colors.ALMOST_BLACK, xCenter - halfSegmentWidth - SEGMENT_DISTANCE, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // TOP LEFT
        drawHSegment(g, segments[6] ? Colors.RED : Colors.ALMOST_BLACK, xCenter - halfSegmentWidth, yCenter, SEGMENT_WIDTH, SEGMENT_HEIGHT); // MIDDLE
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
