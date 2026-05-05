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
    private final GraphicsPpu ppu;
    private final Apu apu;
    private final Keyboard keyboard;
    private final ConsoleTerminal terminal;
    private final Rom rom;
    private final InstructionSet instructionSet;

    /**
     * Initializes the motherboard, soldering all components to the system bus.
     */
    public Motherboard() {
        this.bus = new SystemBus();
        this.instructionSet = new InstructionSet();

        // --------------------------------------------------------------------
        // STRICT MEMORY MAP DEFINITION (No overlapping regions)
        // --------------------------------------------------------------------

        // 8KB RAM: 0x0000 to 0x1FFF (Covers Zero Page and Stack)
        this.ram = new Ram(0x0000, 0x2000);
        // PPU Registers: 0x2000 to 0x3FFF
        this.ppu = new GraphicsPpu(0x2000, 0x3FFF);
        // Keyboard I/O: 0x4000
        this.keyboard = new Keyboard(0x4000);
        // APU Registers: 0x5000 to 0x500F
        this.apu = new Apu(0x5000);
        // Terminal Output: 0xF000
        this.terminal = new ConsoleTerminal(0xF000);
        // 32KB ROM: 0x8000 to 0xFFFF
        // Note: Terminal at 0xF000 will intentionally shadow the ROM at that specific byte.
        this.rom = new Rom(0x8000, new int[0x8000]);

        // --------------------------------------------------------------------
        // BUS ATTACHMENT (Priority Order)
        // --------------------------------------------------------------------

        // Attach exact-address Memory-Mapped I/O devices first
        this.bus.attachDevice(this.terminal);
        this.bus.attachDevice(this.keyboard);
        this.bus.attachDevice(this.ppu);
        this.bus.attachDevice(this.apu);

        // Attach broad memory regions last
        this.bus.attachDevice(this.ram);
        this.bus.attachDevice(this.rom);

        // Connect the hardware-accurate CPU to the bus
        this.cpu = new Cpu(this.bus, this.instructionSet);

        LOG.info("Motherboard initialized successfully. Hardware mapped.");
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

    public GraphicsPpu ppu() {
        return this.ppu;
    }

    public Keyboard keyboard() {
        return this.keyboard;
    }

    public InstructionSet instructionSet() {
        return this.instructionSet;
    }
}