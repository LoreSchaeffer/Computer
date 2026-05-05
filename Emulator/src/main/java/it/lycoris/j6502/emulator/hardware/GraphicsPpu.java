package it.lycoris.j6502.emulator.hardware;

/**
 * A generic Picture Processing Unit providing basic memory-mapped frame buffer capabilities.
 */
public class GraphicsPpu implements BusDevice {
    // 128x54 = 8192 bytes
    public static final int SCREEN_WIDTH = 128;
    public static final int SCREEN_HEIGHT = 64;
    private final int startAddress;
    private final int endAddress;
    private final int[] vram;

    /**
     * Initializes the Video RAM chip.
     *
     * @param startAddress The inclusive starting 16-bit address (e.g., 0x2000).
     * @param endAddress   The inclusive ending 16-bit address (e.g., 0x3FFF).
     */
    public GraphicsPpu(int startAddress, int endAddress) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;

        int size = (endAddress - startAddress) + 1;
        this.vram = new int[size];
    }

    @Override
    public boolean accepts(int address) {
        return address >= this.startAddress && address <= this.endAddress;
    }

    @Override
    public int read(int address) {
        int physicalOffset = address - this.startAddress;
        return this.vram[physicalOffset];
    }

    @Override
    public void write(int address, int value) {
        int physicalOffset = address - this.startAddress;
        this.vram[physicalOffset] = value & 0xFF;
    }

    /**
     * Exposes the raw Video RAM to the host display rendering engine.
     *
     * @return The internal array representing the screen pixels.
     */
    public int[] getVram() {
        return this.vram;
    }
}
