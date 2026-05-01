package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers flag control instructions (CLC, CLD, CLI, CLV, SEC, SED, SEI).
 * Supports polymorphic execution across different CPU emulation strategies.
 */
public class FlagControlGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- Clear Flags ---
        registry.put(0x18, new OpcodeMetadata("CLC", cpu -> this.setFlag(cpu, 'C', false)));
        registry.put(0xD8, new OpcodeMetadata("CLD", cpu -> this.setFlag(cpu, 'D', false)));
        registry.put(0x58, new OpcodeMetadata("CLI", cpu -> this.setFlag(cpu, 'I', false)));
        registry.put(0xB8, new OpcodeMetadata("CLV", cpu -> this.setFlag(cpu, 'V', false)));

        // --- Set Flags ---
        registry.put(0x38, new OpcodeMetadata("SEC", cpu -> this.setFlag(cpu, 'C', true)));
        registry.put(0xF8, new OpcodeMetadata("SED", cpu -> this.setFlag(cpu, 'D', true)));
        registry.put(0x78, new OpcodeMetadata("SEI", cpu -> this.setFlag(cpu, 'I', true)));
    }

    // ========================================================================
    // POLYMORPHIC HELPER METHODS
    // ========================================================================

    /**
     * Polymorphically sets or clears a specific flag.
     *
     * @param cpu   The execution context (CPU).
     * @param flag  The character representing the flag (C, D, I, V).
     * @param state The boolean state to apply.
     */
    private void setFlag(Cpu cpu, char flag, boolean state) {
        if (cpu instanceof GateLevelCpu hardwareCpu) hardwareCpu.forceFlag(flag, state);
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            switch (flag) {
                case 'C' -> fastCpu.setFlagC(state);
                case 'D' -> fastCpu.setFlagD(state);
                case 'I' -> fastCpu.setFlagI(state);
                case 'V' -> fastCpu.setFlagV(state);
                default -> throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
    }
}