package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.model.CpuState;

/**
 * Defines the strict architectural contract for a MOS 6502 Microprocessor.
 * Implementations of this interface can vary from highly accurate logic-gate simulations
 * to high-speed instruction-level software emulators.
 */
public interface Cpu {
    // ========================================================================
    // LIFECYCLE & EXECUTION
    // ========================================================================

    /**
     * Executes the hardware reset sequence, resetting registers and loading the Program Counter.
     */
    void reset();

    /**
     * Executes a single CPU cycle or instruction, depending on the implementation granularity.
     */
    void step();

    // ========================================================================
    // BUS & MEMORY ACCESS
    // ========================================================================

    /**
     * Retrieves the connected System Bus.
     *
     * @return The SystemBus instance attached to the CPU.
     */
    SystemBus getBus();

    /**
     * Reads a single byte from the external memory bus.
     *
     * @param address The 16-bit memory address.
     * @return The 8-bit value read.
     */
    int readSystemBus(int address);

    /**
     * Writes a single byte to the external memory bus.
     *
     * @param address The 16-bit memory address.
     * @param value   The 8-bit value to write.
     */
    void writeSystemBus(int address, int value);

    // ========================================================================
    // DIAGNOSTICS & STATE INSPECTION
    // ========================================================================

    /**
     * Retrieves the current state of the Accumulator register.
     *
     * @return The 8-bit Accumulator value.
     */
    int getAccumulator();

    /**
     * Retrieves the Processor Status Register (Flags).
     *
     * @return The 8-bit Status Register value.
     */
    int getStatusRegister();

    /**
     * Generates a read-only, point-in-time snapshot of the CPU's internal state.
     * Essential for UI rendering, debugging, and tracing.
     *
     * @return A CpuState record containing registers and flags.
     */
    CpuState snapshot();

    /**
     * Inspects a specific register directly without causing emulation side-effects.
     *
     * @param registerName The logical name of the register.
     * @return The 8-bit value of the register.
     */
    int readRegisterDirectly(String registerName);

    enum Type {
        HARDWARE_EMULATED,
        SOFTWARE_EMULATED
    }
}
