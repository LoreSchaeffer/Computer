package it.lycoris.j6502.emulator.control;

public record OpcodeMetadata(
        String mnemonic,
        Instruction logic
) {
}
