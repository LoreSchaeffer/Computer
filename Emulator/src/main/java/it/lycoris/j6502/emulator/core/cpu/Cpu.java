package it.lycoris.j6502.emulator.core.cpu;

/**
 * Represents the contract for any 6502 Central Processing Unit implementation.
 * Allows the system to switch between Gate-Level (cycle-accurate) and
 * High-Level (instruction-accurate) emulation strategies seamlessly.
 */
public interface Cpu {

    void reset();

    void step();

    void triggerNmi();

    void triggerIrq();

    void suspendCycles(int cycles);

    long getTotalClockCycles();

    int getProgramCounter();

    CpuState snapshot();
}