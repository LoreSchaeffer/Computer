package it.multicoredev.computer.ui;

import it.multicoredev.computer.util.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class Pin extends JPanel {
    private final it.multicoredev.computer.elements.Pin pin;
    private final Label label;
    private final int size;
    private final Consumer<it.multicoredev.computer.elements.Pin> onEnter;
    private final Consumer<it.multicoredev.computer.elements.Pin> onExit;

    public Pin(it.multicoredev.computer.elements.Pin pin, int size, Label label, Consumer<it.multicoredev.computer.elements.Pin> onEnter, Consumer<it.multicoredev.computer.elements.Pin> onExit) {
        this.pin = pin;
        this.size = size;
        this.label = label;
        this.onEnter = onEnter;
        this.onExit = onExit;

        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                handleMouseEnter();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                handleMouseExit();
            }
        });
    }

    private void handleMouseClick() {
        if (pin.direction().equals(Direction.OUTPUT)) return;
        pin.toggleState();
    }

    private void handleMouseEnter() {
        if (pin.direction().equals(Direction.INPUT)) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        onEnter.accept(pin);
    }

    private void handleMouseExit() {
        setCursor(Cursor.getDefaultCursor());
        onExit.accept(pin);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(pin.state().toColor());
        g2.fillOval(0, 0, size, size);
    }

    @Override
    public boolean contains(int x, int y) {
        int radius = size / 2;
        return Math.pow(x - radius, 2) + Math.pow(y - radius, 2) <= Math.pow(radius, 2);
    }

    public it.multicoredev.computer.elements.Pin getPin() {
        return pin;
    }
}
