package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccumulatorPathTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("AccumulatorPath");
    }

    @Test
    void testInitialStateIsZero() {
        update();
        assertEquals(0, getBus("A"), "The accumulator should be initialized to 0.");
    }

    @Test
    void testLoadAndStoreValue() {
        // We load 0x55 (01010101 in binary) via an OR operation (A=0 | Bus=0x55)
        setBus("DB", 0x55);
        setPin("OpOR", true);
        setPin("LoadA", false);
        update();

        assertEquals(0, getBus("A"), "The output must not change before the clock pulse.");

        pulseClock("LoadA");
        assertEquals(0x55, getBus("A"), "The value 0x55 was not loaded correctly.");
    }

    @Test
    void testAdditionOverflowAndCarry() {
        // 1. Insert 254 (0xFE) into the register via OR
        setBus("DB", 0xFE);
        setPin("OpOR", true);
        pulseClock("LoadA");

        // 2. Add 3 (0x03)
        clearOperations();
        setPin("OpADD", true);
        setBus("DB", 0x03);
        setPin("CIn", false);
        pulseClock("LoadA");

        // 254 + 3 = 257. In 8-bit (modulo 256) it is 1.
        assertEquals(0x01, getBus("A"), "Incorrect 8-bit addition result.");
        assertTrue(getPin("FlagC"), "The Carry Out flag should be active due to overflow.");
        assertFalse(getPin("FlagZ"), "The Zero flag should not be active.");
    }

    @Test
    void testZeroFlagEdgeCase() {
        // 255 + 1 = 0 (Overflow leading to exact zero)
        setBus("DB", 0xFF);
        setPin("OpOR", true);
        pulseClock("LoadA");

        clearOperations();
        setPin("OpADD", true);
        setBus("DB", 0x01);
        setPin("CIn", false);
        pulseClock("LoadA");

        assertEquals(0x00, getBus("A"), "The result should have overflowed to 0x00.");
        assertTrue(getPin("FlagZ"), "The Zero flag should be active when the result is 0x00.");
        assertTrue(getPin("FlagC"), "The Carry flag should be active due to the overflow.");
    }

    @Test
    void testNegativeFlag() {
        // Load a value with the Most Significant Bit (MSB) set (e.g., 0x80 / 128)
        setBus("DB", 0x80);
        setPin("OpOR", true);
        pulseClock("LoadA");

        assertTrue(getPin("FlagN"), "The Negative flag must be active if bit 7 is 1.");
    }
}
