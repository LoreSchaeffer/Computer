package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers inter-register transfer instructions (TAX, TXA, TAY, TYA).
 * Bypasses memory completely to move data internally within the CPU.
 */
public class RegisterTransferGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- TAX (Transfer Accumulator to X) ---
        registry.put(0xAA, new OpcodeMetadata("TAX", this::executeTax));

        // --- TXA (Transfer X to Accumulator) ---
        registry.put(0x8A, new OpcodeMetadata("TXA", this::executeTxa));

        // --- TAY (Transfer Accumulator to Y) ---
        registry.put(0xA8, new OpcodeMetadata("TAY", this::executeTay));

        // --- TYA (Transfer Y to Accumulator) ---
        registry.put(0x98, new OpcodeMetadata("TYA", this::executeTya));
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void executeTax(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.setBusSelector(1); // Source 1 (Accumulator)
            hardwareCpu.pulseRegister("LoadX");
            // Fixed Bug: TAX must update Zero and Negative flags
            hardwareCpu.updateZAndNFlags(hardwareCpu.readRegisterDirectly("X"));
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int value = fastCpu.getAccumulator();
            fastCpu.setRegisterX(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void executeTxa(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.loadAccumulatorDirect(2); // Source 2 (X Register)
            hardwareCpu.updateZAndNFlags(hardwareCpu.getAccumulator());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int value = fastCpu.getRegisterX();
            fastCpu.setAccumulator(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void executeTay(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.setBusSelector(1); // Source 1 (Accumulator)
            hardwareCpu.pulseRegister("LoadY");
            hardwareCpu.updateZAndNFlags(hardwareCpu.readRegisterDirectly("Y"));
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int value = fastCpu.getAccumulator();
            fastCpu.setRegisterY(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void executeTya(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.loadAccumulatorDirect(3); // Source 3 (Y Register)
            hardwareCpu.updateZAndNFlags(hardwareCpu.getAccumulator());
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int value = fastCpu.getRegisterY();
            fastCpu.setAccumulator(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }
}
