package it.lycoris.cpu.control;

public record OpcodeMetadata(
        String mnemonic,
        Instruction logic
) {
}
