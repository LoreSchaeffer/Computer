package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.listeners.ClockListener;

import java.util.LinkedList;

public class InstructionRegister implements ClockListener {
    private LinkedList<Register16> registers = new LinkedList<>();

    public void addToRegister(String instruction) {
        Register16 register = new Register16();
        register.setD(instruction);
        register.setCe((byte) 1);
        registers.add(register);
    }

    public String readRegister(int i) {
        return registers.get(i).getOut();
    }

    @Override
    public void clock(boolean clock) {
        if (clock) {
            for (Register16 register : registers) {
                register.clock(true);
                register.setCe((byte) 0);
            }
        }
    }
}
