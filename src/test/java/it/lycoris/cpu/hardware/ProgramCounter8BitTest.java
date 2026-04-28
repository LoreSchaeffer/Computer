package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProgramCounter8BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("ProgramCounter8Bit");
    }

    @Test
    void testInitialStateIsZero() {
        update();
        assertEquals(0, getBus("PCOut"), "The Program Counter should initialize at address 0x00.");
    }

    @Test
    void testSequentialIncrement() {
        // Enable the counting mode
        setPin("IncEnable", true);
        setPin("Load", false);

        // Tick 1
        pulseClock("Clk");
        assertEquals(1, getBus("PCOut"), "PC should increment to 1.");

        // Tick 2
        pulseClock("Clk");
        assertEquals(2, getBus("PCOut"), "PC should increment to 2.");

        // Tick 3
        pulseClock("Clk");
        assertEquals(3, getBus("PCOut"), "PC should increment to 3.");
    }

    @Test
    void testAbsoluteJumpLoad() {
        // We want to simulate a jump to address 0x80 (128)
        setBus("D", 128);

        // Enable Load mode, disable Increment
        setPin("Load", true);
        setPin("IncEnable", false);

        // The output shouldn't change before the clock
        update();
        assertEquals(0, getBus("PCOut"), "PC should not change before the clock pulse.");

        pulseClock("Clk");

        assertEquals(128, getBus("PCOut"), "PC failed to load the jump address 0x80.");
    }

    @Test
    void testHoldState() {
        // 1. Advance to address 5
        setPin("IncEnable", true);
        setPin("Load", false);
        for (int i = 0; i < 5; i++) {
            pulseClock("Clk");
        }
        assertEquals(5, getBus("PCOut"));

        // 2. Disable both Load and Increment (Hold Mode)
        setPin("IncEnable", false);
        setPin("Load", false);

        // 3. Pulse the clock. The PC should stay frozen at 5.
        pulseClock("Clk");
        pulseClock("Clk");

        assertEquals(5, getBus("PCOut"), "PC did not hold its value when both control pins were false.");
    }

    @Test
    void testWrapAroundZero() {
        // 1. Load the maximum 8-bit address (0xFF / 255)
        setBus("D", 255);
        setPin("Load", true);
        setPin("IncEnable", false);
        pulseClock("Clk");
        assertEquals(255, getBus("PCOut"));

        // 2. Increment
        setPin("Load", false);
        setPin("IncEnable", true);
        pulseClock("Clk");

        // 255 + 1 = 0 in 8-bit space
        assertEquals(0, getBus("PCOut"), "PC should wrap around from 255 to 0.");

        // Optional: If your 8-bit PC exposes a CarryOut for cascading to the High byte, test it here:
        // assertTrue(getPin("COut"), "Carry out should be active to cascade to the High Byte.");
    }
}