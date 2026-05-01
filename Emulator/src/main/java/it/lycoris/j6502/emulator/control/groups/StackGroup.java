package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers stack manipulation instructions (PHA, PLA, PHP, PLP, TSX, TXS).
 * Supports polymorphic execution across different CPU emulation strategies.
 */
public class StackGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- Accumulator Stack Operations ---
        registry.put(0x48, new OpcodeMetadata("PHA", this::executePha));
        registry.put(0x68, new OpcodeMetadata("PLA", this::executePla));

        // --- Processor Status Stack Operations ---
        registry.put(0x08, new OpcodeMetadata("PHP", this::executePhp));
        registry.put(0x28, new OpcodeMetadata("PLP", this::executePlp));

        // --- Stack Pointer Transfers ---
        registry.put(0xBA, new OpcodeMetadata("TSX", this::executeTsx));
        registry.put(0x9A, new OpcodeMetadata("TXS", this::executeTxs));
    }

    // ========================================================================
    // STACK MANIPULATION ABSTRACTIONS
    // ========================================================================

    private void pushStack(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.pushStack(value);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int currentSp = fastCpu.readRegisterDirectly("SP");
            fastCpu.writeSystemBus(0x0100 | currentSp, value);
            fastCpu.setStackPointer((currentSp - 1) & 0xFF);
        }
    }

    private int pullStack(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.pullStack();
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int nextSp = (fastCpu.readRegisterDirectly("SP") + 1) & 0xFF;
            fastCpu.setStackPointer(nextSp);
            return fastCpu.readSystemBus(0x0100 | nextSp);
        }
        throw new UnsupportedOperationException("Unsupported CPU architecture for stack pull.");
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void executePha(Cpu cpu) {
        this.pushStack(cpu, cpu.getAccumulator());
    }

    private void executePla(Cpu cpu) {
        int value = this.pullStack(cpu);

        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.writeToBus(value, 0);
            hardwareCpu.loadAccumulatorDirect(0); // Bypass ALU and load
            hardwareCpu.updateZAndNFlags(value);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setAccumulator(value);
            fastCpu.updateZeroAndNegativeFlags(value);
        }
    }

    private void executePhp(Cpu cpu) {
        this.pushStack(cpu, cpu.getStatusRegister() | 0x10);
    }

    private void executePlp(Cpu cpu) {
        int status = this.pullStack(cpu);

        if (cpu instanceof GateLevelCpu hardwareCpu) {
            hardwareCpu.setStatusRegister(status);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setFlagC((status & 0x01) != 0);
            fastCpu.setFlagZ((status & 0x02) != 0);
            fastCpu.setFlagI((status & 0x04) != 0);
            fastCpu.setFlagD((status & 0x08) != 0);
            fastCpu.setFlagV((status & 0x40) != 0);
            fastCpu.setFlagN((status & 0x80) != 0);
        }
    }

    private void executeTsx(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            int sp = hardwareCpu.readRegisterDirectly("SP");
            hardwareCpu.writeToBus(sp, 0);
            hardwareCpu.pulseRegister("LoadX");
            hardwareCpu.updateZAndNFlags(sp);
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int sp = fastCpu.readRegisterDirectly("SP");
            fastCpu.setRegisterX(sp);
            fastCpu.updateZeroAndNegativeFlags(sp);
        }
    }

    private void executeTxs(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) {
            int x = hardwareCpu.readRegisterDirectly("X");
            hardwareCpu.writeToBus(x, 0);
            hardwareCpu.pulseRegister("LoadSP");
        } else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setStackPointer(fastCpu.getRegisterX());
        }
    }
}
