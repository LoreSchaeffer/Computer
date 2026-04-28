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
        assertEquals(0, getBus("A"), "The accumulator should be initialized to 0.");
    }

    @Test
    void testLoadAndStoreValue() {
        setBus("DB", 0x55);
        setPin("OpOR", true);
        setPin("LoadA", true);
        update();

        pulseClock("Clk");
        assertEquals(0x55, getBus("A"), "The value 0x55 was not loaded correctly.");
    }

    @Test
    void testAdditionOverflowAndCarry() {
        setBus("DB", 0xFE);
        setPin("OpOR", true);
        setPin("LoadA", true);
        update();
        pulseClock("Clk");

        clearOperations();
        setPin("OpADD", true);
        setBus("DB", 0x03);
        setPin("CIn", false);
        setPin("LoadA", true);
        update();

        assertTrue(getPin("FlagC"), "The Carry Out flag should be active due to overflow.");
        assertFalse(getPin("FlagZ"), "The Zero flag should not be active.");

        pulseClock("Clk");

        assertEquals(0x01, getBus("A"), "Incorrect 8-bit addition result.");
    }

    @Test
    void testZeroFlagEdgeCase() {
        setBus("DB", 0xFF);
        setPin("OpOR", true);
        setPin("LoadA", true);
        update();
        pulseClock("Clk");

        clearOperations();
        setPin("OpADD", true);
        setBus("DB", 0x01);
        setPin("CIn", false);
        setPin("LoadA", true);
        update();

        assertTrue(getPin("FlagZ"), "The Zero flag should be active when the result is 0x00.");
        assertTrue(getPin("FlagC"), "The Carry flag should be active.");

        pulseClock("Clk");

        assertEquals(0x00, getBus("A"), "The result should have overflowed to 0x00.");
    }

    @Test
    void testNegativeFlag() {
        setBus("DB", 0x80);
        setPin("OpOR", true);
        setPin("LoadA", true);
        update();
        pulseClock("Clk");

        assertEquals(0x80, getBus("A"));
        assertTrue(getPin("FlagN"), "The Negative flag must be active if bit 7 is 1.");
    }
}