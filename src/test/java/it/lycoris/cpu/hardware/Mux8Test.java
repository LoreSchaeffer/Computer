package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Mux8Test extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("mux8");
    }

    @Test
    void testRouting() {
        // Set all inputs to false EXCEPT In5
        for (int i = 0; i < 8; i++) setPin("In" + i, false);
        setPin("In5", true);

        // Loop through all selectors
        for (int i = 0; i < 8; i++) {
            setPin("Sel0", (i & 1) != 0);
            setPin("Sel1", (i & 2) != 0);
            setPin("Sel2", (i & 4) != 0);
            update();

            if (i == 5) {
                assertTrue(getPin("Out"), "Mux8 should output true for In5 when selector is 5");
            } else {
                assertFalse(getPin("Out"), "Mux8 should output false for In" + i);
            }
        }
    }
}