package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusRegisterTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("StatusRegister");
    }

    private void setFlagMode(String flagPrefix, int mode) {
        setPin("Sel" + flagPrefix + "0", (mode & 1) != 0);
        setPin("Sel" + flagPrefix + "1", (mode & 2) != 0);
    }

    @Test
    void testBit5IsAlwaysHigh() {
        update();
        assertTrue(getPin("P5"), "Bit 5 (Reserved) MUST always be true (VCC hardwired).");
    }

    @Test
    void testManualSetAndClearCarry() {
        // Mode 3 is Manual Input
        setFlagMode("C", 3);

        // SEC (Set Carry) simulation
        setPin("ManC", true);
        pulseClock("Clk");
        assertTrue(getPin("P0"), "Carry should be manually set to 1.");

        // CLC (Clear Carry) simulation
        setPin("ManC", false);
        pulseClock("Clk");
        assertFalse(getPin("P0"), "Carry should be manually cleared to 0.");
    }

    @Test
    void testHoldStateIsMaintained() {
        // 1. Set the Zero flag manually
        setFlagMode("Z", 3);
        setPin("ManZ", true);
        pulseClock("Clk");
        assertTrue(getPin("P1"), "Zero flag should be initially set.");

        // 2. Switch to HOLD mode (0)
        setFlagMode("Z", 0);

        // 3. Try to corrupt it by changing all possible inputs
        setPin("AluZ", false);
        setPin("ManZ", false);
        setPin("Bus1", false);

        // Tick the clock multiple times
        pulseClock("Clk");
        pulseClock("Clk");

        // The latch must feed back into itself and maintain the 'true' state
        assertTrue(getPin("P1"), "Zero flag must maintain its state when in HOLD mode.");
    }

    @Test
    void testLoadFromALU() {
        // Mode 1 is ALU Input. Let's test the Negative (N) flag.
        setFlagMode("N", 1);

        setPin("AluN", true);
        pulseClock("Clk");
        assertTrue(getPin("P7"), "Negative flag should load 'true' from ALU input.");

        setPin("AluN", false);
        pulseClock("Clk");
        assertFalse(getPin("P7"), "Negative flag should load 'false' from ALU input.");
    }

    @Test
    void testLoadFromBusPLP() {
        // Test the PLP instruction logic (Pull Processor Status from Bus)
        // Let's push 0b11000011 (195) onto the Bus
        // Expected Flags: N=1, V=1, -, B=0, D=0, I=0, Z=1, C=1
        setBus("Bus", 195);

        // Set all configurable flags to Mode 2 (Bus Input)
        setFlagMode("C", 2);
        setFlagMode("Z", 2);
        setFlagMode("I", 2);
        setFlagMode("D", 2);
        setFlagMode("B", 2);
        setFlagMode("V", 2);
        setFlagMode("N", 2);

        pulseClock("Clk");

        assertTrue(getPin("P0"), "Carry (Bit 0) should be 1.");
        assertTrue(getPin("P1"), "Zero (Bit 1) should be 1.");
        assertFalse(getPin("P2"), "Interrupt (Bit 2) should be 0.");
        assertFalse(getPin("P3"), "Decimal (Bit 3) should be 0.");
        assertFalse(getPin("P4"), "Break (Bit 4) should be 0.");
        assertTrue(getPin("P5"), "Reserved (Bit 5) MUST be 1 regardless of the bus.");
        assertTrue(getPin("P6"), "Overflow (Bit 6) should be 1.");
        assertTrue(getPin("P7"), "Negative (Bit 7) should be 1.");
    }
}