package it.lycoris.lyco8.emulator.hardware;

import it.lycoris.lyco8.emulator.core.MemoryMap;
import it.lycoris.lyco8.emulator.core.SystemBus;
import it.lycoris.lyco8.emulator.core.cpu.Cpu;
import it.lycoris.lyco8.emulator.core.cpu.GateLevelCpu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Direct Memory Access (DMA) controller.
 * Responsible for rapidly copying 256 bytes of sprite data from main RAM directly
 * into the PPU's Object Attribute Memory (OAM), bypassing normal CPU execution.
 */
public class DmaController implements BusDevice {
    private static final Logger LOG = LoggerFactory.getLogger(DmaController.class);

    private final int debugLevel;
    private final SystemBus systemBus;
    private final Ppu ppu;
    private Cpu cpu;

    /**
     * Initializes the DMA unit.
     *
     * @param systemBus  The system bus to read memory pages from.
     * @param cpu        The CPU to suspend during the transfer.
     * @param ppu        The target PPU containing the OAM.
     * @param debugLevel The verbosity level for debug logging (-1 = none, higher values = more verbose).
     */
    public DmaController(SystemBus systemBus, GateLevelCpu cpu, Ppu ppu, int debugLevel) {
        this.systemBus = systemBus;
        this.cpu = cpu;
        this.ppu = ppu;
        this.debugLevel = debugLevel;
    }

    @Override
    public boolean accepts(int address) {
        return address == MemoryMap.DMA_REGISTER;
    }

    @Override
    public int read(int address) {
        return 0x00;
    }

    @Override
    public void write(int address, int value) {
        if (address == MemoryMap.DMA_REGISTER) {
            int pageAddress = (value & 0xFF) << 8;

            if (LOG.isDebugEnabled() && debugLevel > 0) LOG.debug("DMA Transfer initiated. Copying memory page ${} to PPU OAM.", String.format("%04X", pageAddress));

            for (int offset = 0; offset < 256; offset++) {
                int dataByte = this.systemBus.read(pageAddress + offset);
                this.ppu.writeOamDataDirectly(dataByte);
            }

            this.cpu.suspendCycles(513);
        }
    }
}