package it.lycoris.j6502.emulator.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusRegisterBitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("StatusRegisterBit");
    }

    private void setMode(int mode) {
        setPin("Sel0", (mode & 1) != 0);
        setPin("Sel1", (mode & 2) != 0);
    }

    @Test
    void testLoadFromAlu() {
        setMode(1); // Mode 1: ALU
        setPin("AluIn", true);
        pulseClock("Clk");
        assertTrue(getPin("Q"), "Should load value from AluIn");
    }

    @Test
    void testHoldMode() {
        // First load a 1 manually
        setMode(3); // Mode 3: Manual
        setPin("ManIn", true);
        pulseClock("Clk");
        assertTrue(getPin("Q"));

        // Now switch to Hold (Mode 0) and change all inputs to false
        setMode(0);
        setPin("AluIn", false);
        setPin("BusIn", false);
        setPin("ManIn", false);
        pulseClock("Clk");

        assertTrue(getPin("Q"), "Should hold its previous value of true");
    }
}