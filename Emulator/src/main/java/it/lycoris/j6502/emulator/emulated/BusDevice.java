package it.lycoris.j6502.emulator.emulated;

/**
 * Represents any hardware device that can be attached to the System Bus.
 * Devices must specify which addresses they handle and implement read/write logic.
 */
public interface BusDevice {
    /**
     * Determines if this device is responsible for the given memory address.
     *
     * @param address The 16-bit address queried on the bus.
     * @return boolean True if the device handles the address, false otherwise.
     */
    boolean accepts(int address);

    /**
     * Reads an 8-bit value from the device at the given address.
     *
     * @param address The 16-bit address on the bus.
     * @return int The 8-bit data value returned by the device.
     */
    int read(int address);

    /**
     * Writes an 8-bit value to the device at the given address.
     *
     * @param address The 16-bit address on the bus.
     * @param value   The 8-bit data value to write.
     */
    void write(int address, int value);
}
