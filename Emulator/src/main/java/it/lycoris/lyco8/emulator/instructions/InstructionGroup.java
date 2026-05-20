package it.lycoris.lyco8.emulator.instructions;

import java.util.Map;

/**
 * Represents a logical grouping of CPU instructions (e.g., Arithmetic, Branching).
 * Classes implementing this interface are responsible for registering their
 * specific opcodes into the main instruction registry.
 */
public interface InstructionGroup {

    /**
     * Installs the group's opcodes into the central registry.
     *
     * @param registry The map associating an 8-bit opcode to its metadata.
     */
    void install(Map<Integer, OpcodeMetadata> registry);
}
