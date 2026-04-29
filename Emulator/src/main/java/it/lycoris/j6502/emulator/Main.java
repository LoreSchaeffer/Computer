package it.lycoris.j6502.emulator;

import it.lycoris.j6502.emulator.hardware.MOS6502;
import it.lycoris.j6502.emulator.hardware.io.ComponentLibrary;
import it.lycoris.j6502.emulator.monitoring.SystemMonitor;
import it.lycoris.j6502.emulator.system.Memory;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Emulator MOS 6502 Hybrid...");

        ComponentLibrary lib = new ComponentLibrary();
        lib.loadFromResources("hardware");

        Memory ram = new Memory();

        byte[] program = {
                (byte) 0xA9,
                (byte) 0x50, // LDA #$50 (Carica 80 decimale)
                (byte) 0x69,
                (byte) 0x50  // ADC #$50 (Aggiunge 80 decimale)
        };

        ram.loadProgram(0x8000, program);
        MOS6502 cpu = new MOS6502(lib, ram);
        cpu.reset();

        System.out.println("\n--- RUNNING ALU MATH TEST ---");

        for (int i = 0; i < 2; i++) {
            cpu.step();
            SystemMonitor.display(cpu, "After Step " + (i + 1));
        }
    }
}
