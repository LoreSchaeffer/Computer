package it.lycoris.j6502.emulator.monitoring;

import it.lycoris.j6502.emulator.hardware.MOS6502;

public class SystemMonitor {

    public static void display(MOS6502 cpu, String message) {
        System.out.println("\n--- " + message.toUpperCase() + " ---");
        System.out.print(cpu.snapshot());
    }
}
