package it.lycoris.lyco8.emulator.hardware;

import it.lycoris.lyco8.emulator.core.MemoryMap;
import it.lycoris.lyco8.emulator.core.cpu.Cpu;

/**
 * A Picture Processing Unit implementing a retro Tile-Based rendering architecture.
 * Features a 4BPP (16 colors) packed CHR-RAM, a NameTable for tile indices,
 * and hardware VBlank NMI generation synchronized with dynamic CPU clock speeds.
 */
public class Ppu implements BusDevice {
    public static final int TILES_X = 32;
    public static final int TILES_Y = 24;
    public static final int TILE_SIZE_BYTES = 32;

    public static final int SCREEN_WIDTH_PIXELS = TILES_X * 8;
    public static final int SCREEN_HEIGHT_PIXELS = TILES_Y * 8;

    private static final int OAM_SIZE = 256;
    private static final int VRAM_SIZE = 0x2000; // 8KB Internal Video Ram
    private static final int CHR_RAM_OFFSET = 0x0000;

    private final int startAddress;
    private final int endAddress;

    private final int[] vram;
    private final int[] oam;

    private int oamAddressPointer;

    // The famous Address Latch (Flip-Flop) for 16-bit VRAM addressing via 8-bit data bus
    private int ppuAddressPointer;
    private boolean addressLatch; // false = expecting High Byte, true = expecting Low Byte

    private final int[] backBuffer;

    private Cpu cpu;
    private int cycleCounter = 0;
    private static final int CYCLES_PER_FRAME = 1_000_000 / 60; // 60Hz at 1MHz (16666)
    private boolean nmiEnabled = false;
    private boolean vblankActive = false;

    public static final int[] HARDWARE_PALETTE = new int[]{
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

    /**
     * Initializes the PPU with a dynamic frequency target to ensure 60Hz NMI triggers.
     *
     * @param startAddress The starting memory-mapped address.
     * @param endAddress   The ending memory-mapped address.
     */
    public Ppu(int startAddress, int endAddress) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;

        this.vram = new int[VRAM_SIZE];
        this.oam = new int[OAM_SIZE];
        this.backBuffer = new int[SCREEN_WIDTH_PIXELS * SCREEN_HEIGHT_PIXELS];

        this.oamAddressPointer = 0x00;
        this.ppuAddressPointer = 0x0000;
        this.addressLatch = false;
    }

    @Override
    public boolean accepts(int address) {
        return address >= this.startAddress && address <= this.endAddress;
    }

    @Override
    public int read(int address) {
        if (address >= MemoryMap.PPU_NAMETABLE && address < MemoryMap.PPU_NAMETABLE + 768) {
            return this.vram[address - MemoryMap.PPU_NAMETABLE];
        }

        if (address == MemoryMap.PPU_STATUS) {
            int status = 0x00;
            if (this.vblankActive) status |= 0x80;

            this.addressLatch = false;
            this.vblankActive = false;
            return status;
        }

        if (address == MemoryMap.PPU_DATA) {
            int data = this.vram[this.ppuAddressPointer];
            this.ppuAddressPointer = (this.ppuAddressPointer + 1) & 0x1FFF;
            return data;
        }

        return 0x00;
    }

    @Override
    public void write(int address, int value) {
        // Direct NameTable write mapping
        if (address >= MemoryMap.PPU_NAMETABLE && address < MemoryMap.PPU_NAMETABLE + 768) {
            this.vram[address - MemoryMap.PPU_NAMETABLE] = value & 0xFF;
            return;
        }

        switch (address) {
            case MemoryMap.PPU_CTRL:
                this.nmiEnabled = (value & 0x80) != 0;
                break;
            case MemoryMap.PPU_OAM_ADDR:
                this.oamAddressPointer = value & 0xFF;
                break;
            case MemoryMap.PPU_OAM_DATA:
                this.oam[this.oamAddressPointer] = value & 0xFF;
                this.oamAddressPointer = (this.oamAddressPointer + 1) & 0xFF;
                break;
            case MemoryMap.PPU_ADDR:
                if (!this.addressLatch) this.ppuAddressPointer = (this.ppuAddressPointer & 0x00FF) | ((value & 0xFF) << 8);
                else this.ppuAddressPointer = (this.ppuAddressPointer & 0xFF00) | (value & 0xFF);

                this.addressLatch = !this.addressLatch;
                break;
            case MemoryMap.PPU_DATA:
                this.vram[this.ppuAddressPointer] = value & 0xFF;
                this.ppuAddressPointer = (this.ppuAddressPointer + 1) & 0x1FFF;
                break;
        }
    }

    public void tick(int cycles) {
        this.cycleCounter += cycles;

        if (this.cycleCounter >= CYCLES_PER_FRAME) {
            this.cycleCounter -= CYCLES_PER_FRAME;

            this.vblankActive = true;
            this.renderFrame();

            if (this.nmiEnabled && this.cpu != null) this.cpu.triggerNmi();
        }
    }

    public void connectCpu(Cpu cpu) {
        this.cpu = cpu;
    }

    /**
     * Interface for DMA Controller to blast data directly into Object Attribute Memory.
     */
    public void writeOamDataDirectly(int value) {
        this.oam[this.oamAddressPointer] = value & 0xFF;
        this.oamAddressPointer = (this.oamAddressPointer + 1) & 0xFF;
    }

    /**
     * Backdoor method used exclusively by the host bootloader (CharacterRomLoader)
     * to flash default factory font data before CPU execution begins.
     */
    public void flashCharacterRom(int vramAddress, int data) {
        if (vramAddress >= 0 && vramAddress < VRAM_SIZE) {
            this.vram[vramAddress] = data & 0xFF;
        }
    }

    /**
     * Exposes the internal video buffer to the Java Display UI.
     */
    public int[] getPixels() {
        return this.backBuffer;
    }

    /**
     * Translates the logical VRAM (NameTable + CHR-RAM) and OAM into a flat array of ARGB pixels.
     * This simulates the physical video signal generation.
     */
    public void renderFrame() {
        // 1. Render Background (NameTable)
        for (int tileY = 0; tileY < TILES_Y; tileY++) {
            for (int tileX = 0; tileX < TILES_X; tileX++) {
                int nametableIndex = tileY * TILES_X + tileX;
                int tileId = this.vram[nametableIndex]; // The tile index stored in VRAM NameTable

                int screenX = tileX * 8;
                int screenY = tileY * 8;

                drawTileToBuffer(tileId, screenX, screenY, false);
            }
        }

        // 2. Render Sprites (OAM)
        // Sprite format: [Y pos, Tile ID, Attributes, X pos]
        for (int i = 0; i < OAM_SIZE; i += 4) {
            int yPos = this.oam[i];
            int tileId = this.oam[i + 1];
            // int attributes = this.oam[i + 2]; // Future: Flip H/V, Palette swap
            int xPos = this.oam[i + 3];

            // If Y is >= 0xEF, the sprite is usually considered hidden/off-screen
            if (yPos > 0 && yPos < 240) {
                drawTileToBuffer(tileId, xPos, yPos - 1, true); // NES standard offsets sprite Y by 1
            }
        }
    }

    /**
     * Decodes the 4BPP CHR-RAM data and writes it to the ARGB back buffer.
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

                writePixel(targetXLeft, targetY, leftPixelColorIndex, isSprite);
                writePixel(targetXRight, targetY, rightPixelColorIndex, isSprite);
            }
        }
    }

    private void writePixel(int x, int y, int colorIndex, boolean isSprite) {
        if (x >= 0 && x < SCREEN_WIDTH_PIXELS && y >= 0 && y < SCREEN_HEIGHT_PIXELS) {
            if (isSprite && colorIndex == 0) return;

            int argbColor = HARDWARE_PALETTE[colorIndex];
            this.backBuffer[y * SCREEN_WIDTH_PIXELS + x] = argbColor;
        }
    }
}