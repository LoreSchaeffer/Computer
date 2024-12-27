package it.multicoredev.computer;

import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.v2.components.display.DisplayDriver;
import it.multicoredev.computer.v2.components.registers.Register4Bit;
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

        Register4Bit register = new Register4Bit();
        frame.add(register.getChip());

        DisplayDriver driver = new DisplayDriver();
        frame.add(driver.getDisplay());

        register.connect(driver);

        frame.pack();
        frame.setVisible(true);
    }
}
