package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.core.cpu.Cpu;

/**
 * A Picture Processing Unit implementing a retro Tile-Based rendering architecture.
 * Features a 4BPP (16 colors) packed CHR-RAM, a NameTable for tile indices,
 * and hardware VBlank NMI generation synchronized with dynamic CPU clock speeds.
 */
public class TileGraphicsPpu implements BusDevice {
    public static final int TILES_X = 32;
    public static final int TILES_Y = 24;
    public static final int TILE_SIZE_BYTES = 32;

    public static final int SCREEN_WIDTH_PIXELS = TILES_X * 8;
    public static final int SCREEN_HEIGHT_PIXELS = TILES_Y * 8;

    private static final int OAM_SIZE = 256;
    private static final int MAX_SPRITES = 64;

    private final int startAddress;
    private final int endAddress;

    private final int[] vram;
    private final int[] oam;
    private int oamAddressPointer;

    private final int cyclesPerFrame;
    private int cycleCounter;

    private final int[] backBuffer;
    public static final int[] hardwarePalette = new int[]{
            0xFF000000, // 0: BLACK
            0xFFFFFFFF, // 1: WHITE
            0xFFFF0000, // 2: RED
            0xFF00FFFF, // 3: CYAN
            0xFFFF00FF, // 4: MAGENTA
            0xFF00FF00, // 5: GREEN
            0xFF0000FF, // 6: BLUE
            0xFFFFFF00, // 7: YELLOW
            0xFFFFA500, // 8: ORANGE
            0xFF8B4513, // 9: BROWN
            0xFFFFC0CB, // A: PINK
            0xFF404040, // B: DARK_GRAY
            0xFF808080, // C: GRAY
            0xFFD3D3D3, // D: LIGHT_GRAY
            0xFFADD8E6, // E: LIGHT_BLUE
            0xFF90EE90  // F: LIGHT_GREEN
    };

    private static final int CHR_RAM_OFFSET = 0x0000;
    private static final int NAMETABLE_OFFSET = 0x1000;
    private static final int PPU_CTRL_REG = 0x1300;
    private static final int PPU_STATUS_REG = 0x1301;
    private static final int OAM_ADDR_REG = 0x1302;
    private static final int OAM_DATA_REG = 0x1303;

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

        int size = (endAddress - startAddress) + 1;
        this.vram = new int[size];
        this.oam = new int[OAM_SIZE];
        this.oamAddressPointer = 0;

        int effectiveFrequency = (targetFrequencyHz > 0) ? targetFrequencyHz : 1_000_000;
        this.cyclesPerFrame = effectiveFrequency / 60;
        this.cycleCounter = 0;

        this.backBuffer = new int[SCREEN_WIDTH_PIXELS * SCREEN_HEIGHT_PIXELS];
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
            this.vram[internalOffset] = status & 0x7F; // Clear VBlank flag
            return status;
        }

        if (internalOffset == OAM_DATA_REG) return this.oam[this.oamAddressPointer];
        return this.vram[internalOffset];
    }

    @Override
    public void write(int address, int value) {
        int internalOffset = address - this.startAddress;

        if (internalOffset == OAM_ADDR_REG) {
            this.oamAddressPointer = value & 0xFF;
            return;
        }

        if (internalOffset == OAM_DATA_REG) {
            this.oam[this.oamAddressPointer] = value & 0xFF;
            this.oamAddressPointer = (this.oamAddressPointer + 1) & 0xFF; // Auto-increment
            return;
        }

        this.vram[internalOffset] = value & 0xFF;
    }

    /**
     * Direct interface for the DMA Controller to rapidly inject byte streams into the OAM.
     *
     * @param data The 8-bit value to write directly into OAM.
     */
    public void writeOamDataDirectly(int data) {
        this.oam[this.oamAddressPointer] = data & 0xFF;
        this.oamAddressPointer = (this.oamAddressPointer + 1) & 0xFF;
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

            // 1. Hardware Double Buffering: Snapshot the exact VRAM state BEFORE the CPU mutates it
            this.renderToBackBuffer();

            // 2. Enter VBlank state (Set bit 7 of Status Register)
            this.vram[PPU_STATUS_REG] |= 0x80;

            // 3. Trigger CPU NMI if enabled
            boolean nmiEnabled = (this.vram[PPU_CTRL_REG] & 0x80) != 0;
            if (nmiEnabled) {
                cpu.triggerNmi();
            }
        }
    }

    /**
     * Called by the Swing UI Thread.
     * Safely copies the static completed frame to the display, preventing Screen Tearing.
     *
     * @param displayPixels The target ARGB array provided by the UI.
     */
    public void copyFrameTo(int[] displayPixels) {
        System.arraycopy(this.backBuffer, 0, displayPixels, 0, this.backBuffer.length);
    }

    /**
     * Internal rendering pipeline. Writes pixels exclusively to the safe back-buffer.
     */
    private void renderToBackBuffer() {
        // Draw Background
        for (int tileY = 0; tileY < TILES_Y; tileY++) {
            for (int tileX = 0; tileX < TILES_X; tileX++) {
                int nametableIndex = NAMETABLE_OFFSET + (tileY * TILES_X) + tileX;
                int tileId = this.vram[nametableIndex] & 0x7F;
                this.drawTileToBuffer(tileId, tileX * 8, tileY * 8, false);
            }
        }

        // Draw Sprites
        for (int spriteIndex = 0; spriteIndex < MAX_SPRITES; spriteIndex++) {
            int oamBaseIndex = spriteIndex * 4;
            int spriteY = this.oam[oamBaseIndex];
            int tileId = this.oam[oamBaseIndex + 1];
            int spriteX = this.oam[oamBaseIndex + 3];

            if (spriteY < SCREEN_HEIGHT_PIXELS) {
                this.drawTileToBuffer(tileId, spriteX, spriteY, true);
            }
        }
    }

    /**
     * Decodes a 4BPP Packed tile and applies it to the back-buffer.
     *
     * @param tileId   The ID of the tile in CHR-RAM.
     * @param screenX  The absolute X coordinate on the screen.
     * @param screenY  The absolute Y coordinate on the screen.
     * @param isSprite If true, color index 0 is treated as transparent.
     */
    private void drawTileToBuffer(int tileId, int screenX, int screenY, boolean isSprite) {
        int tileBaseAddress = CHR_RAM_OFFSET + (tileId * TILE_SIZE_BYTES);

        for (int row = 0; row < 8; row++) {
            for (int byteCol = 0; byteCol < 4; byteCol++) {
                int byteData = this.vram[tileBaseAddress + (row * 4) + byteCol];

                int leftPixelColorIndex = (byteData >> 4) & 0x0F;
                int rightPixelColorIndex = byteData & 0x0F;

                int targetXLeft = screenX + (byteCol * 2);
                int targetXRight = targetXLeft + 1;
                int targetY = screenY + row;

                this.writePixel(targetXLeft, targetY, leftPixelColorIndex, isSprite);
                this.writePixel(targetXRight, targetY, rightPixelColorIndex, isSprite);
            }
        }
    }

    private void writePixel(int x, int y, int colorIndex, boolean isSprite) {
        if (x >= 0 && x < SCREEN_WIDTH_PIXELS && y >= 0 && y < SCREEN_HEIGHT_PIXELS) {
            if (!isSprite || colorIndex != 0) {
                int bufferIndex = (y * SCREEN_WIDTH_PIXELS) + x;
                this.backBuffer[bufferIndex] = this.hardwarePalette[colorIndex];
            }
        }
    }
}