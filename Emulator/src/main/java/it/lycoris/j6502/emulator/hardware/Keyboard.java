package it.lycoris.j6502.emulator.hardware;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Memory-Mapped I/O device representing a basic keyboard input controller.
 * Reading from its mapped address returns the ASCII value of the last pressed key
 * and subsequently clears the hardware buffer.
 */
public class Keyboard implements BusDevice {
    private final int mappedAddress;
    // Ensures thread-safety as the GUI event-dispatch thread writes here while the CPU thread reads
    private final AtomicInteger lastKeyPressed;

    /**
     * Initializes the keyboard device bound to a specific memory address.
     *
     * @param mappedAddress The 16-bit address used to poll keyboard input.
     */
    public Keyboard(int mappedAddress) {
        this.mappedAddress = mappedAddress;
        this.lastKeyPressed = new AtomicInteger(0x00);
    }

    @Override
    public boolean accepts(int address) {
        return address == this.mappedAddress;
    }

    @Override
    public int read(int address) {
        return this.lastKeyPressed.getAndSet(0x00);
    }

    @Override
    public void write(int address, int value) {
    }

    /**
     * Simulates a hardware interrupt or external asynchronous event
     * that registers a key press into the device buffer.
     *
     * @param keyCode The ASCII code of the pressed key.
     */
    public void pressKey(int keyCode) {
        this.lastKeyPressed.set(keyCode & 0xFF);
    }
}
