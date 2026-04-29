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
                (byte) 0xA9, (byte) 0x05, // LDA #$05
                (byte) 0xAA,              // TAX
                // --- LOOP START ($8003) ---
                (byte) 0xCA,              // DEX       ($8003)
                (byte) 0xD0, (byte) -3    // BNE -3    ($8004, $8005) -> Salta a PC(8006) - 3 = 8003
        };

        ram.loadProgram(0x8000, program);
        MOS6502 cpu = new MOS6502(lib, ram);
        cpu.reset();

        SystemMonitor.display(cpu, "Initial State");

        for (int i = 0; i < 15; i++) {
            cpu.step();
            SystemMonitor.display(cpu, "After step " + (i + 1));

            if (cpu.snapshot().currentOpcode() == 0x00) {
                System.out.println("Execution terminated (Reached empty space in RAM).");
                break;
            }
        }

        System.out.println("\nCheck RAM at $0200: $" +
                Integer.toHexString(ram.read(0x0200)));
    }
}
