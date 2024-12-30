package it.multicoredev.computer;

import it.multicoredev.computer.components.v3.display.Dabble;
import it.multicoredev.computer.components.v3.display.DoubleDabble;
import it.multicoredev.computer.components.v3.display.SegDisplayDriver;
import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.ui.SegmentDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger("Computer");

    // TODO DLatch and DFlipFlop could not work as expected
    // TODO Send state when connecting pins

    public static void main(String[] args) {
        LOG.info("Starting Computer");
        LOG.debug("Debug mode enabled");


        JFrame frame = new JFrame("Computer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setMinimumSize(new Dimension(1280, 720));
        frame.getContentPane().setBackground(Colors.ALMOST_BLACK);

        SegDisplayDriver driver = new SegDisplayDriver();
        driver.showLabels(true);
        frame.add(driver.getChip());

        SegmentDisplay display = new SegmentDisplay();
        frame.add(display);

        driver.connect(display.segmentPins());

        frame.pack();
        frame.setVisible(true);
    }
}
