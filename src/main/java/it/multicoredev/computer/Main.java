package it.multicoredev.computer;

import it.multicoredev.computer.components.display.SegmentDisplay;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Computer");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SegmentDisplay display = new SegmentDisplay();
        frame.setSize(600, 600);

        display.setSegment(1, true);
        display.setSegment(2, true);
        display.setSegment(3, true);

        frame.add(display);
        frame.pack();
        frame.setVisible(true);
    }
}
