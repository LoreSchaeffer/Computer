package it.lycoris.cpu.system;

public class Memory {
    private final byte[] data = new byte[65536]; // 64KB

    public int read(int address) {
        return data[address & 0xFFFF] & 0xFF; // Convert byte signed to int 0-255
    }

    public void write(int address, int value) {
        data[address & 0xFFFF] = (byte) (value & 0xFF);
    }

    public void loadProgram(int startAddress, byte[] program) {
        System.arraycopy(program, 0, data, startAddress, program.length);
    }
}
