package it.lycoris.lyco8.emulator.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory-mapped character output device.
 * Writing an ASCII byte to its mapped address prints the character to the standard output,
 * acting as a textual monitor or teletype (TTY).
 */
public class ConsoleTerminal implements BusDevice {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleTerminal.class);
    private final int mappedAddress;

    /**
     * Initializes the terminal device.
     *
     * @param mappedAddress The memory address where the CPU will write ASCII characters.
     */
    public ConsoleTerminal(int mappedAddress) {
        this.mappedAddress = mappedAddress;
        LOG.info("Console Terminal mapped at address ${}", String.format("%04X", mappedAddress));
    }

    @Override
    public boolean accepts(int address) {
        return address == this.mappedAddress;
    }

    @Override
    public int read(int address) {
        return 0x00;
    }

    @Override
    public void write(int address, int value) {
        if (address == this.mappedAddress) {
            char asciiCharacter = (char) (value & 0xFF);
            System.out.print(asciiCharacter);
            System.out.flush();
        }
    }
}
