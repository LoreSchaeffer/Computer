package it.lycoris.cpu.control.groups;

import it.lycoris.cpu.control.InstructionGroup;
import it.lycoris.cpu.control.OpcodeMetadata;

import java.util.Map;

public class LoadStoreGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // LDA # - Load Accumulator (Immediate)
        registry.put(0xA9, new OpcodeMetadata("LDA #", cpu -> {
            int val = cpu.fetchOperand();
            cpu.writeToBus(val, 0); // Source 0 (Data In)
            cpu.loadAccumulatorDirect(0); // Bypass ALU and load
            cpu.updateZAndNFlags(cpu.getAccumulator());
        }));

        // STA $abs - Store Accumulator (Absolute)
        registry.put(0x8D, new OpcodeMetadata("STA $abs", cpu -> {
            int addr = cpu.fetchAddress();
            int val = cpu.getAccumulator();
            cpu.getMemory().write(addr, val);
        }));

        // LDA $abs,X - Load Accumulator (Absolute Indexed with X)
        registry.put(0xBD, new OpcodeMetadata("LDA $abs,X", cpu -> {
            int baseAddress = cpu.fetchAddress();
            int xValue = cpu.snapshot().x();
            int effectiveAddress = (baseAddress + xValue) & 0xFFFF;

            int val = cpu.getMemory().read(effectiveAddress);
            cpu.writeToBus(val, 0); // Source 0 (Data In)
            cpu.loadAccumulatorDirect(0); // Bypass ALU and load
            cpu.updateZAndNFlags(cpu.getAccumulator());
        }));

        // STA $abs,X - Store Accumulator (Absolute Indexed with X)
        registry.put(0x9D, new OpcodeMetadata("STA $abs,X", cpu -> {
            int baseAddress = cpu.fetchAddress();
            int xValue = cpu.snapshot().x();
            int effectiveAddress = (baseAddress + xValue) & 0xFFFF;

            int val = cpu.getAccumulator();
            cpu.getMemory().write(effectiveAddress, val);
        }));
    }
}
