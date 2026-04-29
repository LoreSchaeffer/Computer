package it.lycoris.cpu.control.groups;

import it.lycoris.cpu.control.InstructionGroup;
import it.lycoris.cpu.control.OpcodeMetadata;

import java.util.Map;

public class StackGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // PHA - Push Accumulator onto Stack
        registry.put(0x48, new OpcodeMetadata("PHA", cpu -> {
            // 1. Read current Stack Pointer
            int sp = cpu.snapshot().stackPointer();

            // 2. Calculate hardware stack address (Page 1: $0100 to $01FF)
            int stackAddress = 0x0100 | sp;

            // 3. Write Accumulator to memory
            cpu.getMemory().write(stackAddress, cpu.getAccumulator());

            // 4. Hardware decrement of SP (Stack grows downwards!)
            cpu.indexOp("DecSP");
        }));

        // PLA - Pull Accumulator from Stack
        registry.put(0x68, new OpcodeMetadata("PLA", cpu -> {
            cpu.indexOp("IncSP");
            int stackAddress = 0x0100 | cpu.snapshot().stackPointer();
            int val = cpu.getMemory().read(stackAddress);

            cpu.writeToBus(val, 0); // Source 0 (Data In)
            cpu.loadAccumulatorDirect(0); // Bypass ALU and load
            cpu.updateZAndNFlags(val);
        }));
    }
}
