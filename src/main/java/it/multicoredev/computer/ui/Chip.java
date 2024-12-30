package it.multicoredev.computer.ui;

import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Component;
import it.multicoredev.computer.util.ConnectionList;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.Text;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class Chip extends JPanel {
    private static final int WIDTH = 80;
    private static final int MIN_HEIGHT = 30;
    private static final int PIN_SIZE = 18;
    private static final int MIN_PIN_SPACING = PIN_SIZE / 2;
    private static final int TEXT_PADDING = 16;
    private static final int LABEL_PADDING = 4;

    private final Component component;
    private final Pin[] inputPins;
    private final Pin[] outputPins;
    private final Color color;
    private boolean showLabels = false;
    private final Map<it.multicoredev.computer.elements.Pin, Label> labels = new HashMap<>();
    private final int inputLabelWidth;
    private final int outputLabelWidth;

    public Chip(ChipComponent component, Color color) {
        this.component = component;
        this.color = color;

        setLayout(null);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));

        generateLabels(component.inputs());
        generateLabels(component.outputs());

        inputPins = new Pin[component.inputs().length];
        outputPins = new Pin[component.outputs().length];

        int maxPins = Math.max(inputPins.length, outputPins.length);
        int longestInputLabel = labels.entrySet()
                .stream()
                .filter(e -> e.getKey().direction().equals(Direction.INPUT))
                .max(Comparator.comparingInt(e -> e.getValue().getPreferredSize().width))
                .map(e -> e.getValue().getPreferredSize().width)
                .orElse(0);
        int longestOutputLabel = labels.entrySet()
                .stream()
                .filter(e -> e.getKey().direction().equals(Direction.OUTPUT))
                .max(Comparator.comparingInt(e -> e.getValue().getPreferredSize().width))
                .map(e -> e.getValue().getPreferredSize().width)
                .orElse(0);
        inputLabelWidth = longestInputLabel > 0 ? longestInputLabel + LABEL_PADDING : 0;
        outputLabelWidth = longestOutputLabel > 0 ? longestOutputLabel + LABEL_PADDING : 0;

        setPreferredSize(new Dimension(
                WIDTH + PIN_SIZE + inputLabelWidth + outputLabelWidth,
                Math.max(
                        MIN_HEIGHT,
                        maxPins * PIN_SIZE + (maxPins + 2) * MIN_PIN_SPACING
                )
        ));

        int inSpacing = Math.max(MIN_PIN_SPACING, getPreferredSize().height / (inputPins.length + 1));
        for (int i = 0; i < inputPins.length; i++) {
            int y = inSpacing * (i + 1);

            Label label = labels.get(component.input(i));
            label.setBounds(0, y - (label.getPreferredSize().height / 2), label.getPreferredSize().width, label.getPreferredSize().height);
            label.setVisible(showLabels);
            add(label);

            Pin pin = new Pin(component.input(i), PIN_SIZE, label, this::handlePinEnter, this::handlePinExit);
            inputPins[i] = pin;

            pin.setBounds(inputLabelWidth, y - (PIN_SIZE / 2), PIN_SIZE, PIN_SIZE);
            add(pin);
        }

        int outSpacing = Math.max(MIN_PIN_SPACING, getPreferredSize().height / (outputPins.length + 1));
        for (int i = 0; i < component.outputs().length; i++) {
            int y = outSpacing * (i + 1);

            Label label = labels.get(component.output(i));
            label.setBounds(getPreferredSize().width - label.getPreferredSize().width, y - (label.getPreferredSize().height / 2), label.getPreferredSize().width, label.getPreferredSize().height);
            label.setVisible(showLabels);
            add(label);

            Pin pin = new Pin(component.output(i), PIN_SIZE, label, this::handlePinEnter, this::handlePinExit);
            outputPins[i] = pin;

            pin.setBounds(getPreferredSize().width - PIN_SIZE - outputLabelWidth, y - (PIN_SIZE / 2), PIN_SIZE, PIN_SIZE);
            add(pin);
        }
    }

    public Chip(ChipComponent component) {
        this(component, Colors.getRandom(component.getClass()));
    }

    public void showLabels(boolean showLabels) {
        this.showLabels = showLabels;
        labels.values().forEach(label -> label.setVisible(showLabels));
    }

    public List<ConnectionList> getConnections() {
        List<ConnectionList> connections = new ArrayList<>();

        for (Pin pin : outputPins) {
            connections.add(new ConnectionList(
                    pin,
                    pin.getPin().outboundConnections()
            ));
        }

        return connections;
    }

    private void generateLabels(it.multicoredev.computer.elements.Pin[] pins) {
        for (it.multicoredev.computer.elements.Pin pin : pins) {
            Label label = new Label(pin.name());
            labels.put(pin, label);
        }
    }

    private void handlePinEnter(it.multicoredev.computer.elements.Pin pin) {
        if (!showLabels) labels.get(pin).setVisible(true);
    }

    private void handlePinExit(it.multicoredev.computer.elements.Pin pin) {
        if (!showLabels) labels.get(pin).setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xCenter = getWidth() / 2;
        int yCenter = getHeight() / 2;
        int chipWidth = getWidth() - PIN_SIZE - inputLabelWidth - outputLabelWidth;

        g2.setColor(color);
        g2.fillRoundRect(PIN_SIZE / 2 + inputLabelWidth, 0, chipWidth, getHeight() - 1, 10, 10);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        List<String> lines = Text.wrap(component.name(), g2, chipWidth - PIN_SIZE);

        int largestWidth = 0;
        for (String line : lines) {
            int textWidth = g2.getFontMetrics().stringWidth(line);
            if (textWidth > largestWidth) largestWidth = textWidth;
        }

        if (largestWidth > chipWidth - PIN_SIZE) {
            setPreferredSize(new Dimension(largestWidth + TEXT_PADDING * 2 + PIN_SIZE + inputLabelWidth + outputLabelWidth, getHeight()));
            revalidate();
            repaint();

            for (Pin pin : outputPins) {
                pin.setBounds(getPreferredSize().width - PIN_SIZE - outputLabelWidth, pin.getY(), PIN_SIZE, PIN_SIZE);
            }
        }

        int textHeight = g2.getFontMetrics().getHeight();
        int startY = yCenter + g2.getFontMetrics().getAscent() / 2;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int textWidth = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, xCenter - textWidth / 2, startY + i * textHeight);
        }
    }

//    @Override
//    public boolean contains(int x, int y) {
//        return x >= PIN_SIZE / 2 + inputLabelWidth && x <= getWidth() - PIN_SIZE / 2 - outputLabelWidth && y >= 0 && y <= getHeight();
//    }
}
