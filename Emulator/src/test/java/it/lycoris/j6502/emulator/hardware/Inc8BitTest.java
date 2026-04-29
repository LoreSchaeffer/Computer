package it.lycoris.j6502.emulator.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Inc8BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("Inc8Bit");
    }

    @Test
    void testIncrementZero() {
        setBus("A", 0);
        update();

        // 0 + 1 = 1
        assertEquals(1, getBus("S"), "0 incremented should be 1.");
        assertFalse(getPin("COut"), "Carry Out should be false when incrementing 0.");
    }

    @Test
    void testIncrementStandardValue() {
        setBus("A", 127); // 0x7F
        update();

        // 127 + 1 = 128
        assertEquals(128, getBus("S"), "127 incremented should be 128 (0x80).");
        assertFalse(getPin("COut"), "Carry Out should be false.");
    }

    @Test
    void testIncrementWithWrapAround() {
        // Test the maximum 8-bit value
        setBus("A", 255); // 0xFF
        update();

        // 255 + 1 = 256 -> wraps to 0 in 8-bit, with Carry Out
        assertEquals(0, getBus("S"), "255 incremented should wrap around to 0.");
        assertTrue(getPin("COut"), "Carry Out MUST be active when wrapping past 255.");
    }
}