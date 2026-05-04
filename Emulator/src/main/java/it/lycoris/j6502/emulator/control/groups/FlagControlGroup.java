package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;

import java.util.Map;

/**
 * Registers flag control instructions (CLC, CLD, CLI, CLV, SEC, SED, SEI).
 * Manipulates the Status Register hardware pins.
 */
public class FlagControlGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- Clear Flags ---
        registry.put(0x18, new OpcodeMetadata("CLC", cpu -> cpu.forceFlag('C', false)));
        registry.put(0xD8, new OpcodeMetadata("CLD", cpu -> cpu.forceFlag('D', false)));
        registry.put(0x58, new OpcodeMetadata("CLI", cpu -> cpu.forceFlag('I', false)));
        registry.put(0xB8, new OpcodeMetadata("CLV", cpu -> cpu.forceFlag('V', false)));

        // --- Set Flags ---
        registry.put(0x38, new OpcodeMetadata("SEC", cpu -> cpu.forceFlag('C', true)));
        registry.put(0xF8, new OpcodeMetadata("SED", cpu -> cpu.forceFlag('D', true)));
        registry.put(0x78, new OpcodeMetadata("SEI", cpu -> cpu.forceFlag('I', true)));
    }
}