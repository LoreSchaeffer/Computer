package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InternalBus8BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("InternalBus8Bit");
    }

    // Helper method to set the 3-bit selector easily
    private void setSelector(int selValue) {
        setPin("Sel0", (selValue & 1) != 0); // Bit 0
        setPin("Sel1", (selValue & 2) != 0); // Bit 1
        setPin("Sel2", (selValue & 4) != 0); // Bit 2
    }

    @Test
    void testRoutingSourceZero() {
        // Set a recognizable value on Source 0 (e.g. 0xAA / 170)
        setBus("S0B", 170); // This sets S0B0...S0B7

        // Select Source 0 (Sel = 000)
        setSelector(0);
        update();

        assertEquals(170, getBus("Out"), "The bus should output the value of Source 0 (170).");
    }

    @Test
    void testRoutingSourceSeven() {
        // Set a recognizable value on Source 7 (e.g. 0x55 / 85)
        setBus("S7B", 85); // This sets S7B0...S7B7

        // Select Source 7 (Sel = 111)
        setSelector(7);
        update();

        assertEquals(85, getBus("Out"), "The bus should output the value of Source 7 (85).");
    }

    @Test
    void testIsolationBetweenSources() {
        // Assign different values to all 8 sources at the same time
        // to ensure there are no logical short circuits
        for (int i = 0; i < 8; i++) {
            // Assign 10 to source 0, 20 to source 1, ..., 80 to source 7
            setBus("S" + i + "B", (i + 1) * 10);
        }

        // Now iterate through all selectors and verify the expected value is routed
        for (int i = 0; i < 8; i++) {
            setSelector(i);
            update();

            int expectedValue = (i + 1) * 10;
            assertEquals(expectedValue, getBus("Out"),
                    "Routing failed for Source " + i + ". Expected " + expectedValue);
        }
    }
}