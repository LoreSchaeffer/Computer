package it.lycoris.j6502.emulator.hardware;

/**
 * Represents a generic hardware device that can be attached to the system bus.
 * Devices must specify which memory addresses they respond to.
 */
public interface BusDevice {

    /**
     * Determines if this device is mapped to the requested memory address.
     *
     * @param address The 16-bit physical memory address.
     * @return true if the device handles this address, false otherwise.
     */
    boolean accepts(int address);

    /**
     * Reads an 8-bit unsigned value from the specified memory address.
     *
     * @param address The 16-bit physical memory address.
     * @return The 8-bit unsigned data.
     */
    int read(int address);

    /**
     * Writes an 8-bit unsigned value to the specified memory address.
     *
     * @param address The 16-bit physical memory address.
     * @param value   The 8-bit unsigned data to write.
     */
    void write(int address, int value);
}