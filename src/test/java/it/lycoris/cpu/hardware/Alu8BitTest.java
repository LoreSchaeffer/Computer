package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Alu8BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("ALU8Bit");
    }

    @Test
    void testSimpleAddition() {
        clearOperations();
        setPin("OpADD", true);
        setPin("CIn", false);

        // 15 + 25 = 40
        setBus("A", 15);
        setBus("B", 25);
        update();

        assertEquals(40, getBus("Res"), "15 + 25 should yield 40.");
        assertFalse(getPin("COut"), "Carry Out should be false.");
        assertFalse(getPin("FlagZ"), "Zero flag should be false.");
        assertFalse(getPin("FlagN"), "Negative flag should be false.");
        assertFalse(getPin("FlagV"), "Overflow flag should be false.");
    }

    @Test
    void testAdditionWithRippleCarry() {
        clearOperations();
        setPin("OpADD", true);
        setPin("CIn", false);

        // 255 + 1 = 256 -> 0 (in 8-bit) with Carry Out
        setBus("A", 255); // 0xFF
        setBus("B", 1);   // 0x01
        update();

        assertEquals(0, getBus("Res"), "255 + 1 should wrap around to 0.");
        assertTrue(getPin("COut"), "Carry Out should be active due to unsigned overflow.");
        assertTrue(getPin("FlagZ"), "Zero flag should be active since the 8-bit result is 0.");
        assertFalse(getPin("FlagN"), "Negative flag should be false.");
        assertFalse(getPin("FlagV"), "Overflow flag should be false (unsigned carry != signed overflow).");
    }

    @Test
    void testAdditionWithCarryIn() {
        clearOperations();
        setPin("OpADD", true);

        // 10 + 10 + 1 (CIn) = 21
        setPin("CIn", true);
        setBus("A", 10);
        setBus("B", 10);
        update();

        assertEquals(21, getBus("Res"), "10 + 10 + CarryIn(1) should be 21.");
    }

    @Test
    void testSignedOverflowFlag() {
        clearOperations();
        setPin("OpADD", true);
        setPin("CIn", false);

        // Positive + Positive = Negative (Overflow)
        // 127 (0x7F) + 2 (0x02) = 129 (0x81), which is -127 in 8-bit signed.
        setBus("A", 127);
        setBus("B", 2);
        update();

        assertEquals(129, getBus("Res"), "127 + 2 should be 129 (0x81).");
        assertTrue(getPin("FlagV"), "Overflow flag MUST be active (Positive + Positive = Negative).");
        assertTrue(getPin("FlagN"), "Negative flag should be active because MSB is 1.");

        // Negative + Negative = Positive (Overflow)
        // 128 (0x80) + 255 (0xFF, or -1) = 127 (0x7F)
        setBus("A", 128);
        setBus("B", 255);
        update();

        assertEquals(127, getBus("Res"), "128 + 255 (8-bit) should yield 127 (0x7F).");
        assertTrue(getPin("FlagV"), "Overflow flag MUST be active (Negative + Negative = Positive).");
        assertFalse(getPin("FlagN"), "Negative flag should be false because MSB is 0.");
        assertTrue(getPin("COut"), "Unsigned carry out should also be active here.");
    }

    @Test
    void testLogicalAnd() {
        clearOperations();
        setPin("OpAND", true);
        setPin("CIn", false);

        // 0b10101010 (170) AND 0b11110000 (240) = 0b10100000 (160)
        setBus("A", 170);
        setBus("B", 240);
        update();

        assertEquals(160, getBus("Res"), "Logical AND failed.");
        assertTrue(getPin("FlagN"), "Negative flag should be active (Bit 7 is 1).");
    }

    @Test
    void testLogicalOr() {
        clearOperations();
        setPin("OpOR", true);
        setPin("CIn", false);

        // 0b00001111 (15) OR 0b11110000 (240) = 0b11111111 (255)
        setBus("A", 15);
        setBus("B", 240);
        update();

        assertEquals(255, getBus("Res"), "Logical OR failed.");
        assertTrue(getPin("FlagN"), "Negative flag should be active.");
    }

    @Test
    void testLogicalXor() {
        // Nota: Assicurati che il pin di controllo sia OpXOR o OpXor come salvato nel tuo JSON
        clearOperations();
        setPin("OpXOR", true);
        setPin("CIn", false);

        // 0b11111111 (255) XOR 0b10101010 (170) = 0b01010101 (85)
        setBus("A", 255);
        setBus("B", 170);
        update();

        assertEquals(85, getBus("Res"), "Logical XOR failed.");
        assertFalse(getPin("FlagZ"), "Zero flag should be false.");
        assertFalse(getPin("FlagN"), "Negative flag should be false.");
    }
}
