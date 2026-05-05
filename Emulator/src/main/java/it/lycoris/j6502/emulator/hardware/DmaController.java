package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.core.SystemBus;
import it.lycoris.j6502.emulator.core.cpu.Cpu;
import it.lycoris.j6502.emulator.core.cpu.GateLevelCpu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Direct Memory Access (DMA) controller.
 * Responsible for rapidly copying 256 bytes of sprite data from main RAM directly
 * into the PPU's Object Attribute Memory (OAM), bypassing normal CPU execution.
 */
public class DmaController implements BusDevice {
    private static final Logger LOG = LoggerFactory.getLogger(DmaController.class);
    private static final int DMA_REGISTER_ADDRESS = 0x4014;

    private final int debugLevel;
    private final SystemBus systemBus;
    private final TileGraphicsPpu ppu;
    private Cpu cpu;

    /**
     * Initializes the DMA unit.
     *
     * @param systemBus  The system bus to read memory pages from.
     * @param cpu        The CPU to suspend during the transfer.
     * @param ppu        The target PPU containing the OAM.
     * @param debugLevel The verbosity level for debug logging (-1 = none, higher values = more verbose).
     */
    public DmaController(SystemBus systemBus, GateLevelCpu cpu, TileGraphicsPpu ppu, int debugLevel) {
        this.systemBus = systemBus;
        this.cpu = cpu;
        this.ppu = ppu;
        this.debugLevel = debugLevel;
    }

    @Override
    public boolean accepts(int address) {
        return address == DMA_REGISTER_ADDRESS;
    }

    @Override
    public int read(int address) {
        return 0x00;
    }

    @Override
    public void write(int address, int value) {
        if (address == DMA_REGISTER_ADDRESS) {
            // The value written determines the high byte (page) of RAM to copy.
            // E.g., writing $02 copies the page $0200 - $02FF.
            int pageAddress = (value & 0xFF) << 8;

            if (LOG.isDebugEnabled() && debugLevel > 0) LOG.debug("DMA Transfer initiated. Copying memory page ${} to PPU OAM.", String.format("%04X", pageAddress));

            for (int offset = 0; offset < 256; offset++) {
                int dataByte = this.systemBus.read(pageAddress + offset);
                this.ppu.writeOamDataDirectly(dataByte);
            }

            // A DMA transfer natively costs 512 clock cycles on the 6502
            // (1 read cycle + 1 write cycle per byte).
            this.cpu.suspendCycles(512);
        }
    }

    public DmaController setCpu(Cpu cpu) {
        this.cpu = cpu;
        return this;
    }
}