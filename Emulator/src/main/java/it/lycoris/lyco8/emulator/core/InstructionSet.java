package it.lycoris.lyco8.emulator.core;

import it.lycoris.lyco8.emulator.instructions.InstructionGroup;
import it.lycoris.lyco8.emulator.instructions.OpcodeMetadata;
import it.lycoris.lyco8.emulator.instructions.microcode.*;
import it.lycoris.lyco8.emulator.instructions.microcode.*;
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

    /**
     * Prints a 16x16 grid representing the standard 6502 opcode matrix to the system console.
     * Rows represent the high nibble (0x0 to 0xF) and columns represent the low nibble (x0 to xF).
     */
    public void printOpcodeMatrix() {
        StringBuilder matrixBuilder = new StringBuilder();

        matrixBuilder.append("\n=== 6502 OPCODE MATRIX ===\n");
        matrixBuilder.append(String.format("%-4s", ""));

        for (int column = 0; column <= 0xF; column++) {
            matrixBuilder.append(String.format("| x%-10X", column));
        }
        matrixBuilder.append("|\n");

        for (int row = 0; row <= 0xF; row++) {
            matrixBuilder.append(String.format("%-4X", row));

            for (int column = 0; column <= 0xF; column++) {
                int opcode = (row << 4) | column;

                if (this.registry.containsKey(opcode)) {
                    OpcodeMetadata metadata = this.registry.get(opcode);
                    matrixBuilder.append(String.format("| %-11s", metadata.mnemonic()));
                } else {
                    matrixBuilder.append(String.format("| %-11s", ""));
                }
            }
            matrixBuilder.append("|\n");
        }

        matrixBuilder.append("==========================\n");
        LOG.info("{}", matrixBuilder);
    }
}
