package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.core.Cpu;

/**
 * A Picture Processing Unit implementing a retro Tile-Based rendering architecture.
 * Features a 4BPP (16 colors) packed CHR-RAM, a NameTable for tile indices,
 * and hardware VBlank NMI generation synchronized with dynamic CPU clock speeds.
 */
public class TileGraphicsPpu implements BusDevice {
    public static final int SCREEN_WIDTH_PIXELS = 128;
    public static final int SCREEN_HEIGHT_PIXELS = 64;

    public static final int TILES_X = 16;
    public static final int TILES_Y = 8;
    public static final int TILE_SIZE_BYTES = 32;

    private final int startAddress;
    private final int endAddress;
    private final int[] vram;

    private final int cyclesPerFrame;
    private int cycleCounter;

    private static final int CHR_RAM_OFFSET = 0x0000;
    private static final int NAMETABLE_OFFSET = 0x1000;
    private static final int PPU_CTRL_REG = 0x1080;
    private static final int PPU_STATUS_REG = 0x1081;

    /**
     * Initializes the PPU with a dynamic frequency target to ensure 60Hz NMI triggers.
     *
     * @param startAddress      The starting memory-mapped address.
     * @param endAddress        The ending memory-mapped address.
     * @param targetFrequencyHz The CPU speed in Hertz to calculate accurate frame timings.
     */
    public TileGraphicsPpu(int startAddress, int endAddress, int targetFrequencyHz) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.cycleCounter = 0;

        int size = (endAddress - startAddress) + 1;
        this.vram = new int[size];

        int effectiveFrequency = (targetFrequencyHz > 0) ? targetFrequencyHz : 1_000_000;
        this.cyclesPerFrame = effectiveFrequency / 60;
    }

    @Override
    public boolean accepts(int address) {
        return address >= this.startAddress && address <= this.endAddress;
    }

    @Override
    public int read(int address) {
        int internalOffset = address - this.startAddress;

        if (internalOffset == PPU_STATUS_REG) {
            int status = this.vram[internalOffset];
            this.vram[internalOffset] = status & 0x7F;
            return status;
        }

        return this.vram[internalOffset];
    }

    @Override
    public void write(int address, int value) {
        int internalOffset = address - this.startAddress;
        this.vram[internalOffset] = value & 0xFF;
    }

    /**
     * Synchronizes the PPU with the CPU clock dynamically.
     *
     * @param cyclesElapsed The number of CPU clock cycles passed since the last evaluation.
     * @param cpu           The CPU instance to signal interrupts to.
     */
    public void tick(int cyclesElapsed, Cpu cpu) {
        this.cycleCounter += cyclesElapsed;

        if (this.cycleCounter >= this.cyclesPerFrame) {
            this.cycleCounter -= this.cyclesPerFrame;

            this.vram[PPU_STATUS_REG] |= 0x80;

            boolean nmiEnabled = (this.vram[PPU_CTRL_REG] & 0x80) != 0;
            if (nmiEnabled) cpu.triggerNmi();
        }
    }

    public void renderToBuffer(int[] displayPixels, int[] hexPalette) {
        for (int tileY = 0; tileY < TILES_Y; tileY++) {
            for (int tileX = 0; tileX < TILES_X; tileX++) {

                int nametableIndex = NAMETABLE_OFFSET + (tileY * TILES_X) + tileX;
                int tileId = this.vram[nametableIndex] & 0x7F;

                int tileBaseAddress = CHR_RAM_OFFSET + (tileId * TILE_SIZE_BYTES);

                for (int row = 0; row < 8; row++) {
                    for (int byteCol = 0; byteCol < 4; byteCol++) {

                        int byteData = this.vram[tileBaseAddress + (row * 4) + byteCol];

                        int leftPixelColorIndex = (byteData >> 4) & 0x0F;
                        int rightPixelColorIndex = byteData & 0x0F;

                        int absoluteScreenX = (tileX * 8) + (byteCol * 2);
                        int absoluteScreenY = (tileY * 8) + row;

                        int bufferIndex1 = (absoluteScreenY * SCREEN_WIDTH_PIXELS) + absoluteScreenX;
                        displayPixels[bufferIndex1] = hexPalette[leftPixelColorIndex];

                        int bufferIndex2 = bufferIndex1 + 1;
                        displayPixels[bufferIndex2] = hexPalette[rightPixelColorIndex];
                    }
                }
            }
        }
    }
}