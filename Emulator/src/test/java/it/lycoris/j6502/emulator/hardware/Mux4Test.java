package it.lycoris.j6502.emulator.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Mux4Test extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("Mux4");
    }

    @Test
    void testRouting() {
        // Inputs: 0010 (In0=0, In1=1, In2=0, In3=0)
        setPin("In0", false);
        setPin("In1", true);
        setPin("In2", false);
        setPin("In3", false);

        // Test Sel = 0 (00) -> Expect false
        setPin("Sel0", false);
        setPin("Sel1", false);
        update();
        assertFalse(getPin("Out"));

        // Test Sel = 1 (01 or 10 depending on your MSB/LSB wiring. Assuming Sel0 is LSB)
        setPin("Sel0", true);
        setPin("Sel1", false);
        update();
        assertTrue(getPin("Out"), "Mux4 should route In1 when Sel is 1");

        // Test Sel = 2 (10) -> Expect false
        setPin("Sel0", false);
        setPin("Sel1", true);
        update();
        assertFalse(getPin("Out"));
    }
}