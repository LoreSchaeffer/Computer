package it.multicoredev.computer.cpu.decoder;

import it.multicoredev.computer.cpu.registers.Register2;
import it.multicoredev.computer.util.gates.And;
import it.multicoredev.computer.util.gates.Or;
import it.multicoredev.computer.util.listeners.ClockListener;

public class Decoder implements ClockListener {
    private SequenceGenerator sg = new SequenceGenerator();
    private Register2 rg2 = new Register2();
    private InstructionDecoder id = new InstructionDecoder();

    private String instruction;

    private byte add;
    private byte load;
    private byte output;
    private byte input;
    private byte jumpz;
    private byte jump;
    private byte jumpnz;
    private byte jumpc;
    private byte jumpnc;
    private byte sub;
    private byte bitand;

    public void decode(String instruction) {
        this.instruction = instruction;
    }

    @Override
    public void clock(boolean clock) {
        /*sg.clock(clock);
        rg2.clock(clock);

        if(clock) {
            id.decode(instruction);

            byte state = Or.getOut(sg.decode(), sg.execute());

            add = And.getOut(state, id.getAdd());
            load = And.getOut(state, id.getLoad());
            output = And.getOut(state, id.getOutput());
            input = And.getOut(state, id.getInput());
            jumpz = And.getOut(state, id.getJumpz());
            jump = And.getOut(state, id.getJump());
            jumpnz = And.getOut(state, id.getJumpnz());
            jumpc = And.getOut(state, id.getJumpc());
            jumpnc = And.getOut(state, id.getJumpnc());
            sub = And.getOut(state, id.getSub());
            bitand = And.getOut(state, id.getBitand());
        }*/
    }
}
