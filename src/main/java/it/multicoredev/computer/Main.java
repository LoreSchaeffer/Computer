package it.multicoredev.computer;

import it.multicoredev.computer.components.v3.gates.multi.MultiAnd;
import it.multicoredev.computer.components.v3.gates.multi.MultiOr;
import it.multicoredev.computer.components.v3.latches.DFlipFlop;
import it.multicoredev.computer.components.v3.latches.DLatch;
import it.multicoredev.computer.constants.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger("Computer");

    // TODO DLatch and DFlipFlop could not work as expected

    public static void main(String[] args) {
        LOG.info("Starting Computer");
        LOG.debug("Debug mode enabled");


        JFrame frame = new JFrame("Computer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setMinimumSize(new Dimension(1280, 720));
        frame.getContentPane().setBackground(Colors.ALMOST_BLACK);

        DFlipFlop flipFlop = new DFlipFlop();
        frame.add(flipFlop.getChip());

        frame.pack();
        frame.setVisible(true);
    }
}
