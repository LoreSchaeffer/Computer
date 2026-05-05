package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.emulator.core.Cpu;

import java.util.Map;

/**
 * Registers relative branching instructions (BCC, BCS, BEQ, BMI, BNE, BPL, BVC, BVS).
 * Relies entirely on the gate-level CPU wrapper.
 */
public class BranchGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- Branch on Carry ---
        registry.put(0x90, new OpcodeMetadata("BCC", cpu -> this.branchIf(cpu, !cpu.isFlagSet('C'))));
        registry.put(0xB0, new OpcodeMetadata("BCS", cpu -> this.branchIf(cpu, cpu.isFlagSet('C'))));

        // --- Branch on Zero ---
        registry.put(0xF0, new OpcodeMetadata("BEQ", cpu -> this.branchIf(cpu, cpu.isFlagSet('Z'))));
        registry.put(0xD0, new OpcodeMetadata("BNE", cpu -> this.branchIf(cpu, !cpu.isFlagSet('Z'))));

        // --- Branch on Negative ---
        registry.put(0x30, new OpcodeMetadata("BMI", cpu -> this.branchIf(cpu, cpu.isFlagSet('N'))));
        registry.put(0x10, new OpcodeMetadata("BPL", cpu -> this.branchIf(cpu, !cpu.isFlagSet('N'))));

        // --- Branch on Overflow ---
        registry.put(0x50, new OpcodeMetadata("BVC", cpu -> this.branchIf(cpu, !cpu.isFlagSet('V'))));
        registry.put(0x70, new OpcodeMetadata("BVS", cpu -> this.branchIf(cpu, cpu.isFlagSet('V'))));
    }

    /**
     * Performs the relative branch operation if the condition is met.
     *
     * @param cpu       The execution context.
     * @param condition The evaluated flag condition.
     */
    private void branchIf(Cpu cpu, boolean condition) {
        // Fetch the relative offset (it must be fetched regardless of the condition to advance the PC)
        int rawOffset = cpu.fetchNextByte();

        if (condition) {
            // Convert 8-bit unsigned to signed offset (-128 to 127)
            int offset = (rawOffset >= 0x80) ? rawOffset - 256 : rawOffset;

            // Read the current PC from the hardware pins
            int currentPc = cpu.readAddressOutPins();

            // Calculate the target address and route it back to the hardware
            int targetPc = (currentPc + offset) & 0xFFFF;
            cpu.jump(targetPc);
        }
    }
}