package it.lycoris.cpu.control;

import it.lycoris.cpu.control.groups.*;

import java.util.HashMap;
import java.util.Map;

public class InstructionSet {
    private final Map<Integer, OpcodeMetadata> registry = new HashMap<>();

    public InstructionSet() {
        load(new ArithmeticGroup());
        load(new BranchGroup());
        load(new LoadStoreGroup());
        load(new RegisterTransferGroup());
        load(new StackGroup());
    }

    private void load(InstructionGroup group) {
        group.install(registry);
    }

    public OpcodeMetadata get(int opcode) {
        return registry.getOrDefault(opcode, new OpcodeMetadata("???", cpu -> System.err.println("Unknown Opcode!")));
    }
}
