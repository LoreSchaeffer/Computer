package it.lycoris.j6502.emulator.emulated;

import org.jetbrains.annotations.NotNull;

public record CpuState(
        int programCounter,
        int accumulator,
        int registerX,
        int registerY,
        int stackPointer,
        int statusRegister,
        long totalClockCycles
) {

    /**
     * Formats the CPU state into a highly readable, single-line trace string
     * suitable for emulator step-by-step debugging.
     *
     * @param currentOpcode   The 8-bit opcode currently being executed.
     * @param instructionName The mnemonic of the instruction.
     * @return A formatted trace string.
     */
    @NotNull
    public String toTraceString(int currentOpcode, String instructionName) {
        String binaryFlags = Integer.toBinaryString(this.statusRegister | 0x100).substring(1);

        return "[CYC %,10d] PC:$%04X | A:$%02X X:$%02X Y:$%02X SP:$%02X P:%s | Op:$%02X (%s)"
                .formatted(
                        this.totalClockCycles,
                        this.programCounter,
                        this.accumulator,
                        this.registerX,
                        this.registerY,
                        this.stackPointer,
                        binaryFlags,
                        currentOpcode,
                        instructionName
                );
    }
}
