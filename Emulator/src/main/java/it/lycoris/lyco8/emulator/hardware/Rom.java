package it.lycoris.lyco8.emulator.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Read-Only Memory (ROM) chip on the system bus.
 * Any write attempts to this mapped address space will be ignored,
 * preserving the integrity of the loaded firmware or cartridge data.
 */
public class Rom implements BusDevice {
    private static final Logger LOG = LoggerFactory.getLogger(Rom.class);
    private final int[] memoryArray;
    private final int startAddress;
    private final int endAddress;

    /**
     * Initializes the ROM chip and permanently flashes the provided data into it.
     *
     * @param startAddress The base 16-bit address where the ROM is mapped.
     * @param firmware     The read-only data to be flashed onto the chip.
     */
    public Rom(int startAddress, int[] firmware) {
        if (firmware == null || firmware.length == 0) throw new IllegalArgumentException("ROM firmware cannot be null or empty.");

        this.startAddress = startAddress;
        this.endAddress = startAddress + firmware.length - 1;
        this.memoryArray = new int[firmware.length];

        System.arraycopy(firmware, 0, this.memoryArray, 0, firmware.length);
    }

    @Override
    public boolean accepts(int address) {
        return address >= this.startAddress && address <= this.endAddress;
    }

    @Override
    public int read(int address) {
        int physicalOffset = address - this.startAddress;
        return this.memoryArray[physicalOffset];
    }

    @Override
    public void write(int address, int data) {
        LOG.debug("Attempted to write data 0x{} to Read-Only Memory at address 0x{}. Write ignored.",
                String.format("%02X", data),
                String.format("%04X", address));
    }

    /**
     * Special initialization method for the emulator to bypass the Read-Only restriction
     * during the bootstrap/flashing phase. Simulates burning data into the ROM chip.
     *
     * @param address The 16-bit physical memory address.
     * @param data    The 8-bit unsigned data to write.
     */
    public void flashData(int address, int data) {
        int physicalOffset = address - this.startAddress;
        if (physicalOffset >= 0 && physicalOffset < this.memoryArray.length) {
            this.memoryArray[physicalOffset] = data & 0xFF;
        }
    }
}