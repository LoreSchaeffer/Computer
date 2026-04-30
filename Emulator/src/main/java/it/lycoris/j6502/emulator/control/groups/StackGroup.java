package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;

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
            cpu.getBus().write(stackAddress, cpu.getAccumulator());

            // 4. Hardware decrement of SP (Stack grows downwards!)
            cpu.indexOp("DecSP");
        }));

        // PLA - Pull Accumulator from Stack
        registry.put(0x68, new OpcodeMetadata("PLA", cpu -> {
            cpu.indexOp("IncSP");
            int stackAddress = 0x0100 | cpu.snapshot().stackPointer();
            int val = cpu.getBus().read(stackAddress);

            cpu.writeToBus(val, 0); // Source 0 (Data In)
            cpu.loadAccumulatorDirect(0); // Bypass ALU and load
            cpu.updateZAndNFlags(val);
        }));

        // TSX - Transfer Stack Pointer to X
        registry.put(0xBA, new OpcodeMetadata("TSX", cpu -> {
            int sp = cpu.snapshot().stackPointer();
            cpu.writeToBus(sp, 0); // Put SP on the bus
            cpu.pulseRegister("LoadX");
            cpu.updateZAndNFlags(sp);
        }));

        // TXS - Transfer X to Stack Pointer (Flags are NOT updated)
        registry.put(0x9A, new OpcodeMetadata("TXS", cpu -> {
            int x = cpu.snapshot().x();
            cpu.writeToBus(x, 0);
            cpu.pulseRegister("LoadSP");
        }));

        // PHP - Push Processor Status
        registry.put(0x08, new OpcodeMetadata("PHP", cpu -> {
            // PHP pushes with the Break flag (bit 4) set to 1
            cpu.pushStack(cpu.getStatusRegister() | 0x10);
        }));

        // PLP - Pull Processor Status
        registry.put(0x28, new OpcodeMetadata("PLP", cpu -> {
            int status = cpu.pullStack();
            cpu.setStatusRegister(status);
        }));
    }
}
