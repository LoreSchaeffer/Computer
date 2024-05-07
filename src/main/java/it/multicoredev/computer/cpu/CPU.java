package it.multicoredev.computer.cpu;

import it.multicoredev.computer.cpu.alu.ALU;
import it.multicoredev.computer.cpu.decoder.Decoder;
import it.multicoredev.computer.cpu.mux.MUX41;
import it.multicoredev.computer.cpu.registers.InstructionRegister;
import it.multicoredev.computer.util.listeners.ClockListener;

public class CPU implements ClockListener {
    InstructionRegister ir = new InstructionRegister();
    private Decoder decoder = new Decoder();
    private ALU alu = new ALU();
    private MUX41 mux0 = new MUX41();
    private MUX41 mux1 = new MUX41();
    private MUX41 mux2 = new MUX41();

    private int pc = 0;

    @Override
    public void clock(boolean clock) {
        ir.clock(clock);

        decoder.decode(ir.readRegister(pc));

    }

    public InstructionRegister getIR() {
        return ir;
    }
}
