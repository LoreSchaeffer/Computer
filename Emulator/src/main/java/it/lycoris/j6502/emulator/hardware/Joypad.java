package it.lycoris.j6502.emulator.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Memory-mapped Joypad controller using an 8-bit shift register pattern.
 * Emulates standard retro console polling mechanics (e.g., Nintendo Entertainment System).
 */
public class Joypad implements BusDevice {
    private static final Logger LOG = LoggerFactory.getLogger(Joypad.class);

    // Standard buttons represented as bit masks
    public static final int BUTTON_A = 0b10000000;
    public static final int BUTTON_B = 0b01000000;
    public static final int BUTTON_SELECT = 0b00100000;
    public static final int BUTTON_START = 0b00010000;
    public static final int BUTTON_UP = 0b00001000;
    public static final int BUTTON_DOWN = 0b00000100;
    public static final int BUTTON_LEFT = 0b00000010;
    public static final int BUTTON_RIGHT = 0b00000001;

    private final int memoryAddress;

    private volatile int buttonState;     // The live state of the physical buttons
    private int shiftRegister;            // The latched state being read by the CPU
    private boolean isStrobeActive;       // When true, reads constantly poll the live state

    /**
     * Initializes the Joypad device.
     *
     * @param memoryAddress The memory-mapped I/O address (e.g., 0x4016).
     */
    public Joypad(int memoryAddress) {
        this.memoryAddress = memoryAddress;
        this.buttonState = 0x00;
        this.shiftRegister = 0x00;
        this.isStrobeActive = false;
    }

    @Override
    public boolean accepts(int address) {
        return address == this.memoryAddress;
    }

    @Override
    public int read(int address) {
        if (this.isStrobeActive) {
            // If strobe is active, continuously return the state of the first button (A)
            return (this.buttonState & BUTTON_A) != 0 ? 1 : 0;
        }

        // Return the Most Significant Bit and shift the register left
        int response = (this.shiftRegister & 0x80) != 0 ? 1 : 0;
        this.shiftRegister = (this.shiftRegister << 1) & 0xFF;

        return response;
    }

    @Override
    public void write(int address, int value) {
        boolean previousStrobeState = this.isStrobeActive;
        this.isStrobeActive = (value & 0x01) == 1;
        if (previousStrobeState && !this.isStrobeActive) this.shiftRegister = this.buttonState;
    }

    /**
     * External interface for the UI window to press a physical button.
     *
     * @param buttonMask The bitmask of the button being pressed.
     */
    public void pressButton(int buttonMask) {
        this.buttonState |= buttonMask;
    }

    /**
     * External interface for the UI window to release a physical button.
     *
     * @param buttonMask The bitmask of the button being released.
     */
    public void releaseButton(int buttonMask) {
        this.buttonState &= ~buttonMask;
    }
}