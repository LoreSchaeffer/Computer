package it.lycoris.lyco8.emulator.core;

import it.lycoris.lyco8.emulator.core.cpu.Cpu;
import it.lycoris.lyco8.emulator.core.cpu.GateLevelCpu;
import it.lycoris.lyco8.emulator.core.cpu.HighLevelCpu;
import it.lycoris.lyco8.emulator.hardware.*;
import it.lycoris.lyco8.emulator.hardware.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the main board of the emulator, connecting the CPU to memory and peripherals.
 * Refactored to wire components using the centralized MemoryMap blueprint.
 */
public class Motherboard {
    private static final Logger LOG = LoggerFactory.getLogger(Motherboard.class);

    private final int debugLevel;

    private final SystemBus bus = new SystemBus();
    private final InstructionSet instructionSet = new InstructionSet();

    private final Cpu cpu;
    private final Ram ram;
    private final Ppu ppu;
    private final Apu apu;
    private final Keyboard keyboard;
    private final Joypad joypad;
    private final Rom rom;
    private final DmaController dmaController;
    private final VirtualFileSystem vfs;

    /**
     * Initializes the motherboard and permanently solders components using the static MemoryMap layout.
     *
     * @param emulationMode     High-Level or Gate-Level selection.
     * @param debugLevel        Logging verbosity level.
     * @param firmware          The 32KB system binary injected via the host bootstrap loader.
     */
    public Motherboard(EmulationMode emulationMode, int debugLevel, int[] firmware) {
        this.debugLevel = debugLevel;

        // --------------------------------------------------------------------
        // COMPONENT INITIALIZATION & MEMORY MAP
        // --------------------------------------------------------------------

        this.ram = new Ram(MemoryMap.RAM_START, MemoryMap.RAM_SIZE);
        this.rom = new Rom(MemoryMap.ROM_START, firmware);

        this.ppu = new Ppu(MemoryMap.PPU_START, MemoryMap.PPU_END);
        this.apu = new Apu(MemoryMap.APU_START);
        this.keyboard = new Keyboard(MemoryMap.KEYBOARD_IN);
        this.joypad = new Joypad(MemoryMap.JOYPAD_IN);
        this.vfs = new VirtualFileSystem(this.ram, this.keyboard);

        // --------------------------------------------------------------------
        // CPU INITIALIZATION
        // --------------------------------------------------------------------

        if (emulationMode == EmulationMode.GATE_LEVEL) {
            GateLevelCpu gateCpu = new GateLevelCpu(bus, instructionSet);
            this.cpu = gateCpu;

            this.dmaController = new DmaController(bus, gateCpu, ppu, debugLevel);
            this.bus.registerDevice(this.dmaController);

            LOG.info("System booted using Gate-Level (Cycle-Accurate) engine.");
        } else {
            this.cpu = new HighLevelCpu(bus);
            this.dmaController = null;

            LOG.info("System booted using High-Level Emulation (HLE) engine.");
        }

        this.ppu.connectCpu(cpu);

        // --------------------------------------------------------------------
        // BUS ATTACHMENT
        // --------------------------------------------------------------------

        this.bus.registerDevice(this.ram);
        this.bus.registerDevice(this.ppu);
        this.bus.registerDevice(this.apu);
        this.bus.registerDevice(this.keyboard);
        this.bus.registerDevice(this.joypad);
        this.bus.registerDevice(this.rom);
        this.bus.registerDevice(this.vfs);

        CharRom charRom = new CharRom();
        charRom.injectFont("/fonts/default.png", this.ppu);

        LOG.info("Motherboard initialized successfully.");
    }

    /**
     * Loads the compiled binary into the correct memory component and sets the Reset Vectors in ROM.
     *
     * @param startAddress The 16-bit address where execution should begin.
     * @param program      The binary machine code.
     */
    public void loadProgram(int startAddress, byte[] program) {
        int[] unsignedProgram = new int[program.length];
        for (int i = 0; i < program.length; i++) {
            unsignedProgram[i] = program[i] & 0xFF;
        }

        if (startAddress >= 0x8000) {
            for (int i = 0; i < unsignedProgram.length; i++) {
                this.rom.flashData(startAddress + i, unsignedProgram[i]);
            }
            LOG.info("Flashed {} bytes into ROM starting at ${}", program.length, String.format("%04X", startAddress));
        } else {
            this.ram.loadProgram(startAddress, unsignedProgram);
            LOG.info("Loaded {} bytes into RAM starting at ${}", program.length, String.format("%04X", startAddress));
        }

        LOG.info("Hardware vectors and Interrupt Shield configured successfully.");
    }

    public Cpu cpu() {
        return this.cpu;
    }

    public SystemBus bus() {
        return this.bus;
    }

    public Ppu ppu() {
        return this.ppu;
    }

    public Keyboard keyboard() {
        return this.keyboard;
    }

    public Joypad joypad() {
        return this.joypad;
    }

    public int debugLevel() {
        return this.debugLevel;
    }

    public enum EmulationMode {
        GATE_LEVEL,
        HIGH_LEVEL
    }
}