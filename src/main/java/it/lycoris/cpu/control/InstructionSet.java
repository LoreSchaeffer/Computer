package it.lycoris.cpu.control;

import java.util.HashMap;
import java.util.Map;

public class InstructionSet {
    private final Map<Integer, InstructionMetadata> opcodes = new HashMap<>();

    public InstructionSet() {
        // --- LOAD Group ---
        add(0xA9, "LDA #", cpu -> {
            int val = cpu.fetchOperand();
            cpu.writeToBus(val, 0); // Source 0 (DIn)
            cpu.pulseRegister("LoadA", "OpOR");
        });

        // --- TRANSFER Group ---
        add(0xAA, "TAX", cpu -> {
            cpu.setBusSelector(1); // Source 1 (Acc)
            cpu.pulseRegister("LoadX");
        });

        add(0x8A, "TXA", cpu -> {
            cpu.setBusSelector(2); // Source 2 (X)
            cpu.pulseRegister("LoadA", "OpOR");
        });

        // --- MEMORY Group ---
        add(0x8D, "STA $abs", cpu -> {
            int addr = cpu.fetchAddress();
            int val = cpu.getAccumulator();
            cpu.getMemory().write(addr, val);
        });
    }

    private void add(int op, String name, Instruction logic) {
        opcodes.put(op, new InstructionMetadata(name, logic));
    }

    public InstructionMetadata get(int opcode) {
        return opcodes.getOrDefault(opcode, new InstructionMetadata("???", cpu -> {
        }));
    }
}
