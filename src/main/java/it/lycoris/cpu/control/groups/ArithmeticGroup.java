package it.lycoris.cpu.control.groups;

import it.lycoris.cpu.control.InstructionGroup;
import it.lycoris.cpu.control.OpcodeMetadata;

import java.util.Map;

public class ArithmeticGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // INX - Increment Index X
        registry.put(0xE8, new OpcodeMetadata("INX", cpu -> {
            cpu.indexOp("IncX");
            cpu.updateZAndNFlags(cpu.snapshot().x());
        }));

        // DEX - Decrement Index X
        registry.put(0xCA, new OpcodeMetadata("DEX", cpu -> {
            cpu.indexOp("DecX");
            cpu.updateZAndNFlags(cpu.snapshot().x());
        }));

        // INY - Increment Index Y (Ready for the future!)
        registry.put(0xC8, new OpcodeMetadata("INY", cpu -> {
            cpu.indexOp("IncY");
            cpu.updateZAndNFlags(cpu.snapshot().y());
        }));

        // DEY - Decrement Index Y (Ready for the future!)
        registry.put(0x88, new OpcodeMetadata("DEY", cpu -> {
            cpu.indexOp("DecY");
            cpu.updateZAndNFlags(cpu.snapshot().y());
        }));

        // ADC # - Add with Carry (Immediate)
        registry.put(0x69, new OpcodeMetadata("ADC #", cpu -> {
            int val = cpu.fetchOperand();
            cpu.executeALU("OpADD", val, cpu.isFlagSet('C'));
        }));
    }
}
