package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.hardware.MOS6502;
import it.lycoris.j6502.emulator.ui.LycoWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Motherboard {
    private static final Logger LOG = LoggerFactory.getLogger(Motherboard.class);
    private final SystemBus bus;
    private final MOS6502 cpu;
    private final Ram mainRam;
    private final GraphicsPpu ppu;
    private final Apu apu;
    private final Keyboard keyboard;
    private final ConsoleTerminal terminal;
    private final Ram rom;

    /**
     * Initializes the motherboard, soldering all components to the system bus.
     */
    public Motherboard() {
        bus = new SystemBus();

        // Memory Map Definition
        this.mainRam = new Ram(0x0000, 0xEFFF);
        this.ppu = new GraphicsPpu(0x2000, 0x3FFF);
        this.keyboard = new Keyboard(0x4000);
        this.apu = new Apu(0x5000);
        this.terminal = new ConsoleTerminal(0xF000);
        this.rom = new Ram(0xF001, 0xFFFF);

        // Attach devices to the bus (priority order)
        this.bus.attachDevice(this.terminal);
        this.bus.attachDevice(this.keyboard);
        this.bus.attachDevice(this.ppu);
        this.bus.attachDevice(this.apu);
        this.bus.attachDevice(this.mainRam);
        this.bus.attachDevice(this.rom);

        // Connect the CPU to the bus
        this.cpu = new MOS6502(this.bus);

        LOG.info("Motherboard initialized successfully. Hardware mapped.");

        LycoWindow window = new LycoWindow(this.ppu, this.keyboard);
        window.setVisible(true);
    }

    /**
     * Loads the compiled binary into the main RAM and sets the Reset Vectors in ROM.
     *
     * @param startAddress The 16-bit address where execution should begin.
     * @param program      The binary machine code.
     */
    public void loadProgram(int startAddress, byte[] program) {
        this.mainRam.loadProgram(startAddress, program);

        // Configure Reset Vector at $FFFC-$FFFD in the ROM area
        this.rom.write(0xFFFC, startAddress & 0xFF);
        this.rom.write(0xFFFD, (startAddress >> 8) & 0xFF);
    }

    public MOS6502 cpu() {
        return this.cpu;
    }

    public SystemBus bus() {
        return this.bus;
    }

    public Keyboard keyboard() {
        return this.keyboard;
    }
}
