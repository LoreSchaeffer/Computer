package it.lycoris.j6502.emulator.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProgramCounter16BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("ProgramCounter16Bit");
    }

    private void setBus16(String prefix, int value) {
        for (int i = 0; i < 16; i++) {
            setPin(prefix + i, ((value >> i) & 1) == 1);
        }
    }

    private int getBus16(String prefix) {
        int result = 0;
        for (int i = 0; i < 16; i++) {
            if (getPin(prefix + i)) {
                result |= (1 << i);
            }
        }
        return result;
    }

    @Test
    void testInitialStateIsZero() {
        update();
        assertEquals(0, getBus16("PCOut"), "The 16-bit Program Counter should initialize at address 0x0000.");
    }

    @Test
    void testSequentialIncrement() {
        setPin("IncEnable", true);
        setPin("Load", false);

        pulseClock("Clk");
        assertEquals(1, getBus16("PCOut"), "PC should increment to 1.");

        pulseClock("Clk");
        assertEquals(2, getBus16("PCOut"), "PC should increment to 2.");
    }

    @Test
    void testAbsoluteJumpLoad16Bit() {
        int targetAddress = 0xABCD;
        setBus16("D", targetAddress);

        setPin("Load", true);
        setPin("IncEnable", false);

        update();
        assertEquals(0, getBus16("PCOut"), "PC should not change before the clock pulse.");

        pulseClock("Clk");

        assertEquals(targetAddress, getBus16("PCOut"), "PC failed to load the 16-bit jump address 0xABCD.");
    }

    @Test
    void testHighByteIncrement() {
        setBus16("D", 255);
        setPin("Load", true);
        setPin("IncEnable", false);
        pulseClock("Clk");
        assertEquals(255, getBus16("PCOut"));

        setPin("Load", false);
        setPin("IncEnable", true);
        pulseClock("Clk");

        assertEquals(256, getBus16("PCOut"), "High Byte failed to increment when Low Byte wrapped around.");

        pulseClock("Clk");
        assertEquals(257, getBus16("PCOut"), "PC should correctly increment to 257 (0x0101).");
    }

    @Test
    void testHoldState() {
        setBus16("D", 0x1234);
        setPin("Load", true);
        pulseClock("Clk");
        assertEquals(0x1234, getBus16("PCOut"));

        setPin("Load", false);
        setPin("IncEnable", false);

        pulseClock("Clk");
        pulseClock("Clk");

        assertEquals(0x1234, getBus16("PCOut"), "PC did not hold its 16-bit value.");
    }

    @Test
    void testWrapAroundZero16Bit() {
        setBus16("D", 0xFFFF);
        setPin("Load", true);
        setPin("IncEnable", false);
        pulseClock("Clk");
        assertEquals(0xFFFF, getBus16("PCOut"));

        setPin("Load", false);
        setPin("IncEnable", true);
        pulseClock("Clk");

        assertEquals(0, getBus16("PCOut"), "PC should wrap around from 0xFFFF to 0x0000.");
    }
}