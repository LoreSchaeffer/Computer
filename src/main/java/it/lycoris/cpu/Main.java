package it.lycoris.cpu;

import it.lycoris.cpu.hardware.MOS6502;
import it.lycoris.cpu.hardware.io.ComponentLibrary;
import it.lycoris.cpu.monitoring.SystemMonitor;
import it.lycoris.cpu.system.Memory;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Emulator MOS 6502 Hybrid...");

        ComponentLibrary lib = new ComponentLibrary();
        lib.loadDirectory(Paths.get("src/main/resources/hardware"));

        Memory ram = new Memory();

        byte[] program = {
                (byte) 0xA9, (byte) 0x42, // LDA #$42
                (byte) 0xAA,              // TAX
                (byte) 0x8D, (byte) 0x00, (byte) 0x02 // STA $0200
        };

        ram.loadProgram(0x8000, program);
        MOS6502 cpu = new MOS6502(lib, ram);
        cpu.reset();

        SystemMonitor.display(cpu, "Initial State");

        for (int i = 0; i < 3; i++) {
            cpu.step();
            SystemMonitor.display(cpu, "After step " + (i + 1));
        }

        System.out.println("\nCheck RAM at $0200: $" +
                Integer.toHexString(ram.read(0x0200)));
    }
}
