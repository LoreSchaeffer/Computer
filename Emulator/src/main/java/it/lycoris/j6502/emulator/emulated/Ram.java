package it.lycoris.j6502.emulator.emulated;

/**
 * Represents the Random Access Memory (RAM) of the system.
 * Uses a primitive array for maximum cache locality and JIT optimization.
 */
public class Ram implements BusDevice {
    private final int[] memoryArray;
    private final int startAddress;
    private final int endAddress;

    /**
     * Initializes a RAM module for a specific address range.
     *
     * @param startAddress The inclusive starting 16-bit address (e.g., 0x0000).
     * @param sizeInBytes  The total capacity of this RAM chip in bytes.
     */
    public Ram(int startAddress, int sizeInBytes) {
        if (sizeInBytes <= 0) throw new IllegalArgumentException("RAM size must be strictly positive.");

        this.startAddress = startAddress;
        this.endAddress = startAddress + sizeInBytes - 1;
        this.memoryArray = new int[sizeInBytes];
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
        int physicalOffset = address - this.startAddress;
        this.memoryArray[physicalOffset] = data & 0xFF;
    }

    /**
     * Utility method to inject a binary payload directly into the RAM module.
     * This bypasses standard bus propagation and is intended for initialization,
     * testing, or DMA (Direct Memory Access) loading.
     *
     * @param startAddress The starting memory address where the payload should be placed.
     * @param program      The array of unsigned 8-bit integers representing the machine code.
     */
    public void loadProgram(int startAddress, int[] program) {
        for (int index = 0; index < program.length; index++) {
            int currentAddress = startAddress + index;
            if (this.accepts(currentAddress)) this.write(currentAddress, program[index]);
            else throw new IndexOutOfBoundsException(String.format("Cannot load program at address 0x%04X: out of RAM bounds.", currentAddress));
        }
    }
}