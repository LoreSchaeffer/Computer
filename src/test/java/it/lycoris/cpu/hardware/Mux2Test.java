package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Mux2Test extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("mux2");
    }

    @Test
    void testRouting() {
        // Set distinct inputs
        setPin("In0", false);
        setPin("In1", true);

        // Select In0
        setPin("Sel", false);
        update();
        assertFalse(getPin("Out"), "Mux2 should route In0 (false) when Sel is 0");

        // Select In1
        setPin("Sel", true);
        update();
        assertTrue(getPin("Out"), "Mux2 should route In1 (true) when Sel is 1");
    }
}