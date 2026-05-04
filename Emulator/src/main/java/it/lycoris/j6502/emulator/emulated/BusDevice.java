package it.lycoris.j6502.emulator.emulated;

/**
 * Represents a hardware device connected to the system bus.
 * Any component that needs to be addressed by the CPU (RAM, ROM, I/O chips)
 * must implement this interface.
 */
public interface BusDevice {

    /**
     * Determines if this device is responsible for the given 16-bit address.
     *
     * @param address The 16-bit memory address to check.
     * @return true if the device is mapped to this address, false otherwise.
     */
    boolean accepts(int address);

    /**
     * Reads an 8-bit value from the specified address.
     *
     * @param address The 16-bit memory address to read from.
     * @return The 8-bit unsigned data (0x00 - 0xFF) stored at the address.
     */
    int read(int address);

    /**
     * Writes an 8-bit value to the specified address.
     *
     * @param address The 16-bit memory address to write to.
     * @param data    The 8-bit unsigned data (0x00 - 0xFF) to store.
     */
    void write(int address, int data);
}
