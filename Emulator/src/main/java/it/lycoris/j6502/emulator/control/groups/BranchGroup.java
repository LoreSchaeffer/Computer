package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers relative branching instructions (BCC, BCS, BEQ, BMI, BNE, BPL, BVC, BVS).
 * Supports polymorphic execution across different CPU emulation strategies.
 */
public class BranchGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- Branch on Carry ---
        registry.put(0x90, new OpcodeMetadata("BCC", cpu -> this.branchIf(cpu, !this.readFlag(cpu, 'C'))));
        registry.put(0xB0, new OpcodeMetadata("BCS", cpu -> this.branchIf(cpu, this.readFlag(cpu, 'C'))));

        // --- Branch on Zero ---
        registry.put(0xF0, new OpcodeMetadata("BEQ", cpu -> this.branchIf(cpu, this.readFlag(cpu, 'Z'))));
        registry.put(0xD0, new OpcodeMetadata("BNE", cpu -> this.branchIf(cpu, !this.readFlag(cpu, 'Z'))));

        // --- Branch on Negative ---
        registry.put(0x30, new OpcodeMetadata("BMI", cpu -> this.branchIf(cpu, this.readFlag(cpu, 'N'))));
        registry.put(0x10, new OpcodeMetadata("BPL", cpu -> this.branchIf(cpu, !this.readFlag(cpu, 'N'))));

        // --- Branch on Overflow ---
        registry.put(0x50, new OpcodeMetadata("BVC", cpu -> this.branchIf(cpu, !this.readFlag(cpu, 'V'))));
        registry.put(0x70, new OpcodeMetadata("BVS", cpu -> this.branchIf(cpu, this.readFlag(cpu, 'V'))));
    }

    // ========================================================================
    // ABSTRACTIONS & HELPER METHODS
    // ========================================================================

    private boolean readFlag(Cpu cpu, char flag) {
        int status = cpu.getStatusRegister();
        return switch (flag) {
            case 'C' -> (status & 0x01) != 0;
            case 'Z' -> (status & 0x02) != 0;
            case 'V' -> (status & 0x40) != 0;
            case 'N' -> (status & 0x80) != 0;
            default -> false;
        };
    }

    private int fetchOffset(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.fetchOperand();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextByte();
        throw new UnsupportedOperationException("Unsupported CPU architecture for relative fetch.");
    }

    private void performBranch(Cpu cpu, int offset) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            int targetPc = (hardwareCpu.readRegisterDirectly("PC") + offset) & 0xFFFF;
            hardwareCpu.jump(targetPc);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int targetPc = (fastCpu.getProgramCounter() + offset) & 0xFFFF;
            fastCpu.setProgramCounter(targetPc);
        }
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void branchIf(Cpu cpu, boolean condition) {
        int rawOffset = this.fetchOffset(cpu);

        if (condition) {
            int offset = (rawOffset >= 0x80) ? rawOffset - 256 : rawOffset;
            this.performBranch(cpu, offset);
        }
    }
}
