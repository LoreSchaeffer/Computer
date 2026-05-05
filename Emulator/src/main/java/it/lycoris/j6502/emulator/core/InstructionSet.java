package it.lycoris.j6502.emulator.core;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.emulator.instructions.microcode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry containing all supported instructions for the 6502 processor.
 */
public class InstructionSet {
    private static final Logger LOG = LoggerFactory.getLogger(InstructionSet.class);
    private final Map<Integer, OpcodeMetadata> registry = new HashMap<>();

    public InstructionSet() {
        load(new ArithmeticGroup());
        load(new BranchGroup());
        load(new CompareGroup());
        load(new FlagControlGroup());
        load(new JumpSystemGroup());
        load(new LoadStoreGroup());
        load(new LogicalGroup());
        load(new RegisterTransferGroup());
        load(new ShiftRotateGroup());
        load(new StackGroup());
    }

    private void load(InstructionGroup group) {
        group.install(this.registry);
    }

    /**
     * Retrieves the metadata and execution logic for a given opcode.
     *
     * @param opcode The 8-bit instruction opcode.
     * @return The associated OpcodeMetadata. Returns a safe fallback if unimplemented.
     */
    public OpcodeMetadata get(int opcode) {
        return this.registry.getOrDefault(
                opcode,
                new OpcodeMetadata("???", _ -> LOG.error("Execution halted. Unimplemented Opcode detected: ${}", String.format("%02X", opcode)))
        );
    }

    // TODO Add a print method to dump the instruction set in a human-readable format (in a table)
}
