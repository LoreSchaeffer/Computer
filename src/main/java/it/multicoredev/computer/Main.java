package it.multicoredev.computer;

import it.multicoredev.computer.components.display.SegmentDisplay;
import it.multicoredev.computer.components.display.SegmentDisplayDriver;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Computer");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SegmentDisplay display = new SegmentDisplay();
        frame.setSize(600, 600);

        SegmentDisplayDriver driver = new SegmentDisplayDriver();
        driver.in(new boolean[]{false, true, false, true});

        boolean[] data = new boolean[8];
        data[0] = false;
        for (int i = 0; i < driver.out().length; i++) {
            data[i + 1] = driver.out()[i];
        }

        display.setSegments(data);

        frame.add(display);
        frame.pack();
        frame.setVisible(true);
    }
}
