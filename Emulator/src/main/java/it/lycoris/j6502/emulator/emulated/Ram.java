package it.lycoris.j6502.emulator.emulated;

/**
 * Represents the Random Access Memory (RAM) of the console.
 */
public class Ram implements BusDevice {
    private final int[] memoryArray;
    private final int startAddress;
    private final int endAddress;

    /**
     * Initializes a RAM module for a specific address range.
     *
     * @param startAddress The inclusive starting 16-bit address.
     * @param endAddress   The inclusive ending 16-bit address.
     */
    public Ram(int startAddress, int endAddress) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        int size = (endAddress - startAddress) + 1;
        this.memoryArray = new int[size];
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
    public void write(int address, int value) {
        int physicalOffset = address - this.startAddress;
        this.memoryArray[physicalOffset] = value & 0xFF;
    }

    /**
     * Utility method to inject a binary program directly into RAM.
     *
     * @param address The starting address for the payload.
     * @param program The byte array containing the compiled instructions.
     */
    public void loadProgram(int address, byte[] program) {
        for (int i = 0; i < program.length; i++) {
            this.write(address + i, program[i]);
        }
    }
}
