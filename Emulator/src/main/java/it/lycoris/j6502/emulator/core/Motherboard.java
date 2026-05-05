package it.lycoris.j6502.emulator.core;

import it.lycoris.j6502.emulator.hardware.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the main board of the emulator, connecting the CPU to memory and peripherals.
 */
public class Motherboard {
    private static final Logger LOG = LoggerFactory.getLogger(Motherboard.class);

    private final SystemBus bus;
    private final Cpu cpu;
    private final Ram ram;
    private final TileGraphicsPpu ppu;
    private final Apu apu;
    private final Keyboard keyboard;
    private final Joypad joypad;
    private final ConsoleTerminal terminal;
    private final Rom rom;
    private final InstructionSet instructionSet;

    /**
     * Initializes the motherboard, soldering all components to the system bus.
     */
    public Motherboard(int targetFrequencyHz) {
        this.bus = new SystemBus();
        this.instructionSet = new InstructionSet();

        // --------------------------------------------------------------------
        // COMPONENT INITIALIZATION & MEMORY MAP
        // --------------------------------------------------------------------

        this.ram = new Ram(0x0000, 0x2000);
        this.ppu = new TileGraphicsPpu(0x2000, 0x3FFF, targetFrequencyHz);
        this.keyboard = new Keyboard(0x4000);
        this.apu = new Apu(0x5000);
        this.terminal = new ConsoleTerminal(0xF000);
        this.rom = new Rom(0x8000, new int[0x8000]);
        this.joypad = new Joypad(0x4016);

        DmaController dmaController = new DmaController(this.bus, null, this.ppu);

        // --------------------------------------------------------------------
        // BUS ATTACHMENT (Priority Order: Specific I/O first, Broad Memory last)
        // --------------------------------------------------------------------

        this.bus.attachDevice(this.terminal);
        this.bus.attachDevice(this.keyboard);
        this.bus.attachDevice(this.joypad);
        this.bus.attachDevice(dmaController);
        this.bus.attachDevice(this.ppu);
        this.bus.attachDevice(this.apu);

        this.bus.attachDevice(this.ram);
        this.bus.attachDevice(this.rom);

        // --------------------------------------------------------------------
        // CPU INITIALIZATION
        // --------------------------------------------------------------------

        this.cpu = new Cpu(this.bus, this.instructionSet);

        dmaController.setCpu(this.cpu);

        LOG.info("Motherboard initialized successfully. DMA, Joypad, and legacy Keyboard are fully mapped.");
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

        // Route the payload to RAM or ROM depending on the start address
        if (startAddress >= 0x8000) {
            for (int i = 0; i < unsignedProgram.length; i++) {
                this.rom.flashData(startAddress + i, unsignedProgram[i]);
            }
            LOG.info("Flashed {} bytes into ROM starting at ${}", program.length, String.format("%04X", startAddress));
        } else {
            this.ram.loadProgram(startAddress, unsignedProgram);
            LOG.info("Loaded {} bytes into RAM starting at ${}", program.length, String.format("%04X", startAddress));
        }

        // Force the Reset Vector into the ROM using the hardware flash backdoor
        this.rom.flashData(0xFFFC, startAddress & 0xFF);
        this.rom.flashData(0xFFFD, (startAddress >> 8) & 0xFF);
    }

    public Cpu cpu() {
        return this.cpu;
    }

    public SystemBus bus() {
        return this.bus;
    }

    public TileGraphicsPpu ppu() {
        return this.ppu;
    }

    public Keyboard keyboard() {
        return this.keyboard;
    }

    public Joypad joypad() {
        return this.joypad;
    }

    public InstructionSet instructionSet() {
        return this.instructionSet;
    }
}