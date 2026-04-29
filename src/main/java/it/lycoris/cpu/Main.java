package it.lycoris.cpu;

import it.lycoris.cpu.hardware.MOS6502;
import it.lycoris.cpu.hardware.io.ComponentLibrary;
import it.lycoris.cpu.system.Memory;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Emulator MOS 6502 Hybrid...");

        ComponentLibrary lib = new ComponentLibrary();
        lib.loadDirectory(Paths.get("src/main/resources/hardware"));

        Memory ram = new Memory();

        byte[] rom = {
                (byte) 0xA9, // LDA
                (byte) 0x42, // VALUE (66 dec | 42 hex)
        };

        ram.loadProgram(0x8000, rom);

        MOS6502 cpu = new MOS6502(lib, ram);
        cpu.reset();

        System.out.println("\n--- INITIAL STATE ---");
        cpu.printState();

        System.out.println("\n--- RUNNING 1° CYCLE (LDA #$42) ---");
        cpu.step();

        System.out.println("\n--- FINAL STATE ---");
        cpu.printState();
    }
}
