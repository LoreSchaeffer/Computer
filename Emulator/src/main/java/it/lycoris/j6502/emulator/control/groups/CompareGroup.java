package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers comparison instructions (CMP, CPX, CPY) into the execution environment.
 * Supports polymorphic execution across different CPU emulation strategies.
 */
public class CompareGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- CMP (Compare Accumulator) ---
        registry.put(0xC9, new OpcodeMetadata("CMP #", cpu -> this.cmp(cpu, this.getRegA(cpu), this.fetchImmediate(cpu))));
        registry.put(0xC5, new OpcodeMetadata("CMP $zp", cpu -> this.cmp(cpu, this.getRegA(cpu), this.fetchZeroPage(cpu))));
        registry.put(0xCD, new OpcodeMetadata("CMP $abs", cpu -> this.cmp(cpu, this.getRegA(cpu), this.fetchAbsolute(cpu))));

        // --- CPX (Compare X Register) ---
        registry.put(0xE0, new OpcodeMetadata("CPX #", cpu -> this.cmp(cpu, this.getRegX(cpu), this.fetchImmediate(cpu))));
        registry.put(0xE4, new OpcodeMetadata("CPX $zp", cpu -> this.cmp(cpu, this.getRegX(cpu), this.fetchZeroPage(cpu))));
        registry.put(0xEC, new OpcodeMetadata("CPX $abs", cpu -> this.cmp(cpu, this.getRegX(cpu), this.fetchAbsolute(cpu))));

        // --- CPY (Compare Y Register) ---
        registry.put(0xC0, new OpcodeMetadata("CPY #", cpu -> this.cmp(cpu, this.getRegY(cpu), this.fetchImmediate(cpu))));
        registry.put(0xC4, new OpcodeMetadata("CPY $zp", cpu -> this.cmp(cpu, this.getRegY(cpu), this.fetchZeroPage(cpu))));
        registry.put(0xCC, new OpcodeMetadata("CPY $abs", cpu -> this.cmp(cpu, this.getRegY(cpu), this.fetchAbsolute(cpu))));
    }

    // ========================================================================
    // VALUE FETCHING ABSTRACTIONS
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.fetchOperand();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextByte();
        throw new UnsupportedOperationException("Unsupported CPU architecture for immediate fetch.");
    }

    private int fetchZeroPage(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrZeroPage());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus(fastCpu.fetchNextByte());
        throw new UnsupportedOperationException("Unsupported CPU architecture for Zero Page fetch.");
    }

    private int fetchAbsolute(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readSystemBus(hardwareCpu.addrAbsolute());
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.readSystemBus(fastCpu.fetchNextAddress());
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute fetch.");
    }

    // ========================================================================
    // REGISTER ACCESS ABSTRACTIONS
    // ========================================================================

    private int getRegA(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.getAccumulator();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.getAccumulator();
        throw new UnsupportedOperationException("Unsupported CPU architecture for Accumulator access.");
    }

    private int getRegX(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readRegisterDirectly("X");
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.getRegisterX();
        throw new UnsupportedOperationException("Unsupported CPU architecture for X Register access.");
    }

    private int getRegY(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.readRegisterDirectly("Y");
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.getRegisterY();
        throw new UnsupportedOperationException("Unsupported CPU architecture for Y Register access.");
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void cmp(Cpu cpu, int reg, int val) {
        int maskedVal = val & 0xFF;
        boolean carryCondition = reg >= maskedVal;
        int difference = (reg - maskedVal) & 0xFF;

        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.forceFlag('C', carryCondition);
            hardwareCpu.updateZAndNFlags(difference);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setFlagC(carryCondition);
            fastCpu.updateZeroAndNegativeFlags(difference);
        }
    }
}