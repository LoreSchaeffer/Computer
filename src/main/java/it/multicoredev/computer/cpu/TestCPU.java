package it.multicoredev.computer.cpu;

import it.multicoredev.computer.cpu.alu.ALU;
import it.multicoredev.computer.cpu.decoder.SequenceGenerator;
import it.multicoredev.computer.util.components.Clock;
import it.multicoredev.computer.util.components.DFlipFlop;
import it.multicoredev.computer.util.listeners.ClockListener;

public class TestCPU implements ClockListener {
    //http://www.simplecpudesign.com/simple_cpu_v1/index.html
    private static Clock clock = new Clock(500);

    private static CPU cpu = new CPU();

    public static void main(String[] args) {
        registerListeners();
        cpu.getIR().addToRegister("0100000010101101");
        clock.start();
    }

    @Override
    public void clock(boolean clock) {
        System.out.println(clock ? "HIGH" : "LOW");
        System.out.println(cpu.getIR().readRegister(0));
    }

    private static void registerListeners() {
        clock.addListener(cpu);
        clock.addListener(new TestCPU());
    }
}
