package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullAdderTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("full_adder");
    }

    @Test
    void testAdditionWithoutCarryIn() {
        setPin("CIn", false);

        // 0 + 0 = 0 (COut = 0)
        setPin("A", false);
        setPin("B", false);
        update();
        assertFalse(getPin("Sum"));
        assertFalse(getPin("COut"));

        // 1 + 0 = 1 (COut = 0)
        setPin("A", true);
        setPin("B", false);
        update();
        assertTrue(getPin("Sum"));
        assertFalse(getPin("COut"));

        // 1 + 1 = 0 (COut = 1)
        setPin("A", true);
        setPin("B", true);
        update();
        assertFalse(getPin("Sum"));
        assertTrue(getPin("COut"));
    }

    @Test
    void testAdditionWithCarryIn() {
        setPin("CIn", true);

        // 0 + 0 + 1 = 1 (COut = 0)
        setPin("A", false);
        setPin("B", false);
        update();
        assertTrue(getPin("Sum"));
        assertFalse(getPin("COut"));

        // 1 + 0 + 1 = 0 (COut = 1)
        setPin("A", true);
        setPin("B", false);
        update();
        assertFalse(getPin("Sum"));
        assertTrue(getPin("COut"));

        // 1 + 1 + 1 = 1 (COut = 1)
        setPin("A", true);
        setPin("B", true);
        update();
        assertTrue(getPin("Sum"));
        assertTrue(getPin("COut"));
    }
}