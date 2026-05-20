package it.lycoris.lyco8.emulator.core;

public final class MemoryMap {

    private MemoryMap() {
        throw new UnsupportedOperationException("Utility class");
    }

    // --- MAIN SYSTEM MEMORY ---
    public static final int RAM_START       = 0x0000;
    public static final int RAM_SIZE        = 0x2000; // 8KB total (includes Zero Page and Stack)

    // --- PICTURE PROCESSING UNIT (PPU) ---
    public static final int PPU_START       = 0x3000;
    public static final int PPU_END         = 0x33FF; // Allocated block for video hardware
    public static final int PPU_NAMETABLE   = 0x3000; // Background text/tile grid
    public static final int PPU_CTRL        = 0x3300; // Control register
    public static final int PPU_STATUS      = 0x3301; // Status register
    public static final int PPU_OAM_ADDR    = 0x3302; // Sprite RAM address pointer
    public static final int PPU_OAM_DATA    = 0x3303; // Sprite RAM data port
    public static final int PPU_ADDR        = 0x3304; // VRAM address register (for custom tilesets)
    public static final int PPU_DATA        = 0x3305; // VRAM data port (for custom tilesets)

    // --- INPUT DEVICES & CONTROLLERS ---
    public static final int KEYBOARD_IN     = 0x4000; // Memory-mapped keyboard register
    public static final int JOYPAD_IN       = 0x4001; // Shift-register joypad port
    public static final int DMA_REGISTER    = 0x4014; // Direct Memory Access trigger register

    // --- VIRTUAL FILE SYSTEM (VFS HOST INTERFACE) ---
    public static final int VFS_COMMAND     = 0x4100; // 0x00=IDLE, 0x01=LOAD, 0x02=SAVE
    public static final int VFS_NAME_PTR_L  = 0x4101; // Filename string pointer low byte
    public static final int VFS_NAME_PTR_H  = 0x4102; // Filename string pointer high byte
    public static final int VFS_DATA_PTR_L  = 0x4103; // Target RAM start pointer low byte
    public static final int VFS_DATA_PTR_H  = 0x4104; // Target RAM start pointer high byte

    // --- AUDIO PROCESSING UNIT (APU) ---
    public static final int APU_START       = 0x5000;
    public static final int APU_SIZE        = 0x0020; // 32 sound registers (0x5000 - 0x501F)

    // --- SYSTEM ROM ---
    public static final int ROM_START       = 0x8000;
    public static final int ROM_SIZE        = 0x8000; // 32KB static ROM area
}
