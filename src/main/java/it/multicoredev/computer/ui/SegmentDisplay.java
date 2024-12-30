package it.multicoredev.computer.ui;

import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.State;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SegmentDisplay extends JPanel {
    private static final int SEGMENT_WIDTH = 70;
    private static final int SEGMENT_HEIGHT = 25;
    private static final int MINUS_WIDTH = 50;
    private static final int SEGMENT_DISTANCE = 2;

    private final DisplayPin[] pins = new DisplayPin[]{
            new DisplayPin("T", Direction.INPUT),
            new DisplayPin("TR", Direction.INPUT),
            new DisplayPin("BR", Direction.INPUT),
            new DisplayPin("B", Direction.INPUT),
            new DisplayPin("BL", Direction.INPUT),
            new DisplayPin("TL", Direction.INPUT),
            new DisplayPin("M", Direction.INPUT),
            new DisplayPin("MIN", Direction.INPUT)
    };

    public SegmentDisplay() {
        Dimension size = new Dimension(
                MINUS_WIDTH + SEGMENT_HEIGHT / 2 + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_HEIGHT / 2 + 20,
                SEGMENT_HEIGHT / 2 + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_WIDTH + SEGMENT_DISTANCE + SEGMENT_HEIGHT / 2 + 20
        );

        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setBackground(Colors.BLACK);

        for (DisplayPin pin : pins) {
            pin.setDisplay(this);
        }
    }

    public Pin[] segmentPins() {
        return Arrays.copyOfRange(pins, 0, 7);
    }

    public Pin minusPin() {
        return pins[7];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int xCenter = getWidth() / 2 + MINUS_WIDTH / 2;
        int yCenter = getHeight() / 2;
        int halfSegmentWidth = SEGMENT_WIDTH / 2;

        drawHSegment(g, getColor(7), xCenter - halfSegmentWidth - MINUS_WIDTH - SEGMENT_HEIGHT / 2 - SEGMENT_DISTANCE, yCenter, MINUS_WIDTH, SEGMENT_HEIGHT); // MINUS
        drawHSegment(g, getColor(0), xCenter - halfSegmentWidth, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT); // TOP
        drawVSegment(g, getColor(1), xCenter + halfSegmentWidth + SEGMENT_DISTANCE, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // TOP RIGHT
        drawVSegment(g, getColor(2), xCenter + halfSegmentWidth + SEGMENT_DISTANCE, yCenter + SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // BOTTOM RIGHT
        drawHSegment(g, getColor(3), xCenter - halfSegmentWidth, yCenter + SEGMENT_WIDTH + SEGMENT_DISTANCE * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT); // BOTTOM
        drawVSegment(g, getColor(4), xCenter - halfSegmentWidth - SEGMENT_DISTANCE, yCenter + SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // BOTTOM LEFT
        drawVSegment(g, getColor(5), xCenter - halfSegmentWidth - SEGMENT_DISTANCE, yCenter - SEGMENT_WIDTH - SEGMENT_DISTANCE, SEGMENT_HEIGHT, SEGMENT_WIDTH); // TOP LEFT
        drawHSegment(g, getColor(6), xCenter - halfSegmentWidth, yCenter, SEGMENT_WIDTH, SEGMENT_HEIGHT); // MIDDLE
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

    private Color getColor(int index) {
        return pins[index].state().equals(State.HIGH) ? Colors.RED : Colors.NOT_BLACK;
    }

    private static class DisplayPin extends Pin {
        private SegmentDisplay display;

        public DisplayPin(String name, Direction direction) {
            super(name, direction);
        }

        private void setDisplay(SegmentDisplay display) {
            if (this.display != null) throw new IllegalStateException("Display already set");
            this.display = display;
        }

        @Override
        public Pin state(State state) {
            this.state = state;
            display.repaint();
            return this;
        }
    }
}
