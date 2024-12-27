package it.multicoredev.computer;

import it.multicoredev.computer.components.Negate;
import it.multicoredev.computer.components.display.DabbleDriver;
import it.multicoredev.computer.components.display.TwoCompDriver;
import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.ui.SegmentDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger("Computer");

    public static void main(String[] args) {
        LOG.info("Starting Computer");
        LOG.debug("Debug mode enabled");

        JFrame frame = new JFrame("Computer");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(800, 800);
        frame.setMinimumSize(new Dimension(800, 800));
        frame.getContentPane().setBackground(Colors.ALMOST_BLACK);

        Negate negate = new Negate();
        negate.getChip().showLabels(true);
        frame.add(negate.getChip());

        DabbleDriver driver = new DabbleDriver();
        driver.getChip().showLabels(true);
        //frame.add(driver.getChip());

        TwoCompDriver driver2 = new TwoCompDriver();
        driver2.getChip().showLabels(true);
        frame.add(driver2.getChip());

        SegmentDisplay display1 = new SegmentDisplay();
        SegmentDisplay display2 = new SegmentDisplay();
        SegmentDisplay display3 = new SegmentDisplay();
        frame.add(display1);
        frame.add(display2);
        frame.add(display3);

        driver2.connect(display1, display2, display3);

        frame.pack();
        frame.setVisible(true);
    }
}
