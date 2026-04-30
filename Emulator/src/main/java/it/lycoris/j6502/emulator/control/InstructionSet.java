package it.lycoris.j6502.emulator.control;

import it.lycoris.j6502.emulator.control.groups.*;
import it.lycoris.j6502.emulator.control.groups.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
        group.install(registry);
    }

    public OpcodeMetadata get(int opcode) {
        return registry.getOrDefault(opcode, new OpcodeMetadata("???", cpu -> LOG.error("Execution halted. Unimplemented Opcode detected: ${}", String.format("%02X", opcode))));
    }
}
