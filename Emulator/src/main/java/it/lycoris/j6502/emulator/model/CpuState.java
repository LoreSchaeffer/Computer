package it.lycoris.j6502.emulator.model;

import org.jetbrains.annotations.NotNull;

public record CpuState(
        int pc,
        int accumulator,
        int x,
        int y,
        int stackPointer,
        int status,
        int currentOpcode,
        String instructionName
) {

    @Override
    @NotNull
    public String toString() {
        return """
                +---------------------------------------+
                | PC: $%04X  |  A: $%02X  |  X: $%02X  |
                | Y: $%02X   | SP: $%02X  |  P: %s  |
                | Instr: %-15s | Op: $%02X |
                +---------------------------------------+
                """.formatted(
                pc, accumulator, x, y, stackPointer,
                Integer.toBinaryString(status | 0x100).substring(1),
                instructionName, currentOpcode
        );
    }
}
