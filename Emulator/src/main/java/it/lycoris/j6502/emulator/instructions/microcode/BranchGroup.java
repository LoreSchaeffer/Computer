package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;

import java.util.Map;

/**
 * Registers relative branching instructions (BCC, BCS, BEQ, BMI, BNE, BPL, BVC, BVS).
 * Relies entirely on the gate-level CPU wrapper.
 */
public class BranchGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x90, new OpcodeMetadata("BCC", cpu -> this.branch(cpu, !cpu.isFlagSet('C'))));
        registry.put(0xB0, new OpcodeMetadata("BCS", cpu -> this.branch(cpu, cpu.isFlagSet('C'))));
        registry.put(0xD0, new OpcodeMetadata("BNE", cpu -> this.branch(cpu, !cpu.isFlagSet('Z'))));
        registry.put(0xF0, new OpcodeMetadata("BEQ", cpu -> this.branch(cpu, cpu.isFlagSet('Z'))));
        registry.put(0x10, new OpcodeMetadata("BPL", cpu -> this.branch(cpu, !cpu.isFlagSet('N'))));
        registry.put(0x30, new OpcodeMetadata("BMI", cpu -> this.branch(cpu, cpu.isFlagSet('N'))));
        registry.put(0x50, new OpcodeMetadata("BVC", cpu -> this.branch(cpu, !cpu.isFlagSet('V'))));
        registry.put(0x70, new OpcodeMetadata("BVS", cpu -> this.branch(cpu, cpu.isFlagSet('V'))));
    }

    private void branch(Cpu cpu, boolean condition) {
        byte offset = (byte) cpu.fetchNextByte();
        if (condition) {
            int newProgramCounter = (cpu.getProgramCounter() + offset) & 0xFFFF;
            cpu.jump(newProgramCounter);
        }
    }
}