package it.lycoris.j6502.emulator.emulated;

/**
 * A Memory-Mapped I/O device representing a basic text terminal.
 * Writing an ASCII value to its address prints the character to the standard output.
 */
public class ConsoleTerminal implements BusDevice {
    private final int mappedAddress;

    /**
     * Initializes the terminal bound to a specific single memory address.
     *
     * @param mappedAddress The 16-bit address used to trigger terminal output.
     */
    public ConsoleTerminal(int mappedAddress) {
        this.mappedAddress = mappedAddress;
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
        char character = (char) (value & 0xFF);
        System.out.print(character);
        System.out.flush();
    }
}
