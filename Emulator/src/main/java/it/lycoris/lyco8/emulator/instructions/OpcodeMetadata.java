package it.lycoris.lyco8.emulator.instructions;

/**
 * Stores the mnemonic and the hardware execution logic for a specific opcode.
 *
 * @param mnemonic The human-readable assembly mnemonic (e.g., "LDA #").
 * @param logic    The microcode sequence to execute on the datapath.
 */
public record OpcodeMetadata(
        String mnemonic,
        Instruction logic
) {
}
