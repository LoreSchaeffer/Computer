package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.hardware.MOS6502;

import java.util.Map;

public class BranchGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x90, new OpcodeMetadata("BCC", cpu -> branchIf(cpu, !cpu.isFlagSet('C'))));
        registry.put(0xB0, new OpcodeMetadata("BCS", cpu -> branchIf(cpu, cpu.isFlagSet('C'))));
        registry.put(0xF0, new OpcodeMetadata("BEQ", cpu -> branchIf(cpu, cpu.isFlagSet('Z'))));
        registry.put(0x30, new OpcodeMetadata("BMI", cpu -> branchIf(cpu, cpu.isFlagSet('N'))));
        registry.put(0xD0, new OpcodeMetadata("BNE", cpu -> branchIf(cpu, !cpu.isFlagSet('Z'))));
        registry.put(0x10, new OpcodeMetadata("BPL", cpu -> branchIf(cpu, !cpu.isFlagSet('N'))));
        registry.put(0x50, new OpcodeMetadata("BVC", cpu -> branchIf(cpu, !cpu.isFlagSet('V'))));
        registry.put(0x70, new OpcodeMetadata("BVS", cpu -> branchIf(cpu, cpu.isFlagSet('V'))));
    }

    private void branchIf(MOS6502 cpu, boolean condition) {
        int rawOffset = cpu.fetchOperand();

        if (condition) {
            int offset = (rawOffset >= 0x80) ? rawOffset - 256 : rawOffset;
            int targetPc = (cpu.snapshot().pc() + offset) & 0xFFFF;

            cpu.jump(targetPc);
        }
    }
}
