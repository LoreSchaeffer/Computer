package it.multicoredev.computer;

import it.multicoredev.computer.components.display.DoubleDabbleDriver;
import it.multicoredev.computer.components.display.TwoCompDriver;
import it.multicoredev.computer.util.Utils;

import javax.swing.*;
import java.awt.*;

public class Main {

    //TODO Displays not showing correct negative values (part 5)

    public static void main(String[] args) {
        JFrame frame = new JFrame("Computer");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(600, 600);

        TwoCompDriver driver = new TwoCompDriver();
        driver.in(Utils.fromBin("00111111"), true, false);

        frame.add(driver.getDisplay1());
        frame.add(driver.getDisplay2());
        frame.add(driver.getDisplay3());
        frame.pack();
        frame.setVisible(true);
    }
}
