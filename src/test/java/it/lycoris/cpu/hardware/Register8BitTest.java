package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Register8BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("Register8Bit");
    }

    @Test
    void testInitialState() {
        update();
        assertEquals(0, getBus("Q"), "Register should initialize with a value of 0.");
    }

    @Test
    void testLatchStoresValue() {
        // Put 123 on the data bus
        setBus("D", 123);
        setPin("En", false);
        update();

        // The output must stay 0 because the clock (Enable) hasn't pulsed yet
        assertEquals(0, getBus("Q"), "Output should not change before the Enable signal is high.");

        // Pulse the clock to store the data
        pulseClock("En");

        assertEquals(123, getBus("Q"), "Register failed to store the input value.");
    }

    @Test
    void testMemoryIsRetained() {
        // 1. Store a recognizable value (0x55 / 85)
        setBus("D", 85);
        pulseClock("En");
        assertEquals(85, getBus("Q"));

        // 2. Drop the Enable signal and change the Input data
        setPin("En", false);
        setBus("D", 170); // 0xAA
        update();

        // 3. Verify the output has NOT changed to 170
        assertEquals(85, getBus("Q"), "Register did not retain its memory when Enable was low.");
    }
}