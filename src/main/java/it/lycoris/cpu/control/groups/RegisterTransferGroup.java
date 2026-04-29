package it.lycoris.cpu.control.groups;

import it.lycoris.cpu.control.InstructionGroup;
import it.lycoris.cpu.control.OpcodeMetadata;

import java.util.Map;

public class RegisterTransferGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // TAX - Transfer Accumulator to X
        registry.put(0xAA, new OpcodeMetadata("TAX", cpu -> {
            cpu.setBusSelector(1); // Source 1 (Accumulator)
            cpu.pulseRegister("LoadX");
        }));

        // TXA - Transfer X to Accumulator
        registry.put(0x8A, new OpcodeMetadata("TXA", cpu -> {
            cpu.loadAccumulatorDirect(2); // Source 2 (X Register), bypass ALU
            cpu.updateZAndNFlags(cpu.getAccumulator());
        }));

        // TAY - Transfer Accumulator to Y
        registry.put(0xA8, new OpcodeMetadata("TAY", cpu -> {
            cpu.setBusSelector(1); // Source 1 (Acc)
            cpu.pulseRegister("LoadY");
            cpu.updateZAndNFlags(cpu.snapshot().y());
        }));

        // TYA - Transfer Y to Accumulator
        registry.put(0x98, new OpcodeMetadata("TYA", cpu -> {
            cpu.loadAccumulatorDirect(3); // Source 3 (Y Register), bypass ALU
            cpu.updateZAndNFlags(cpu.getAccumulator());
        }));
    }
}
