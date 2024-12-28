package it.multicoredev.computer.ui;

import javax.swing.*;
import java.awt.*;

// TODO
public class Connection extends JPanel {
    private Pin pin;
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public Connection(Pin pin, int x1, int y1, int x2, int y2) {
        this.pin = pin;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setPreferredSize(new Dimension(Math.abs(x2 - x1), Math.abs(y2 - y1)));
    }

    public Connection updatePosition(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        setPreferredSize(new Dimension(Math.abs(x2 - x1), Math.abs(y2 - y1)));
        repaint();
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(3));
        g2.setColor(pin.getPin().state().toColor());
        g2.drawLine(x1, y1, x2, y2);
    }
}
