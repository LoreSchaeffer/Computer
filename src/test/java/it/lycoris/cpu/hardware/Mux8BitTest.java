package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Mux8BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("Mux8Bit");
    }

    @Test
    void testRouteBusA() {
        // 1. Set different values on the two buses to ensure they do not mix
        // A = 0xAA (170), B = 0x55 (85)
        setBus("A", 170);
        setBus("B", 85);

        // 2. Selector set to false (selects Bus A)
        setPin("Sel", false);
        update();

        // 3. Verify that the output is exactly Bus A
        assertEquals(170, getBus("Out"), "Mux8Bit should route Bus A (170) when Sel is false.");
    }

    @Test
    void testRouteBusB() {
        // 1. Reuse the same values as before
        setBus("A", 170);
        setBus("B", 85);

        // 2. Selector set to true (selects Bus B)
        setPin("Sel", true);
        update();

        // 3. Verify that the output is exactly Bus B
        assertEquals(85, getBus("Out"), "Mux8Bit should route Bus B (85) when Sel is true.");
    }

    @Test
    void testIsolation() {
        // Stress test: change A bits while routing B to ensure
        // there are no short circuits or signal leaks between the two buses.
        setBus("B", 255); // Expected output
        setPin("Sel", true); // Route B
        update();

        assertEquals(255, getBus("Out"));

        // Now dramatically change A
        setBus("A", 0);
        update();

        // The output must remain unchanged
        assertEquals(255, getBus("Out"), "Changing Bus A should not affect the output when Sel is true.");
    }
}