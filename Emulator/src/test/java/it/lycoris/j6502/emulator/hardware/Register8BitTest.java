package it.lycoris.j6502.emulator.hardware;

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

        // The output must stay 0 because we haven't pulsed the clock yet
        assertEquals(0, getBus("Q"), "Output should not change before the Enable signal is high.");

        // NUOVA LOGICA: Abilitiamo la scrittura e diamo il colpo di clock!
        setPin("En", true);
        pulseClock("Clk");
        setPin("En", false); // Chiudiamo il lucchetto

        assertEquals(123, getBus("Q"), "Register failed to store the input value.");
    }

    @Test
    void testMemoryIsRetained() {
        // 1. Store a recognizable value (0x55 / 85)
        setBus("D", 85);
        setPin("En", true);
        pulseClock("Clk");
        setPin("En", false); // Lucchetto chiuso

        assertEquals(85, getBus("Q"));

        // 2. Cambiamo i dati in ingresso, ma NON abilitiamo il registro (En = false)
        setBus("D", 170);

        // Diamo un colpo di clock. Il registro dovrebbe ignorarlo grazie alla porta AND!
        pulseClock("Clk");

        assertEquals(85, getBus("Q"), "Register did not retain its memory when En was false.");
    }
}