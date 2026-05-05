package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers stack manipulation instructions (PHA, PLA, PHP, PLP, TSX, TXS).
 * Utilizes the internal hardware Stack Pointer (SP) increment/decrement pins.
 */
public class StackGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x48, new OpcodeMetadata("PHA", this::executePha));
        registry.put(0x68, new OpcodeMetadata("PLA", this::executePla));

        registry.put(0x08, new OpcodeMetadata("PHP", this::executePhp));
        registry.put(0x28, new OpcodeMetadata("PLP", this::executePlp));

        registry.put(0xBA, new OpcodeMetadata("TSX", this::executeTsx));
        registry.put(0x9A, new OpcodeMetadata("TXS", this::executeTxs));
    }

    // ========================================================================
    // STACK MANIPULATION ABSTRACTIONS
    // ========================================================================

    private void pushStack(Cpu cpu, int value) {
        int sp = cpu.getStackPointer();
        cpu.writeSystemBus(0x0100 | sp, value);

        MOS6502 datapath = cpu.getDatapath();
        datapath.DecSP = true;
        cpu.pulseClock();
        datapath.DecSP = false;
        datapath.evaluateCombinational();
    }

    private int pullStack(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();
        datapath.IncSP = true;
        cpu.pulseClock();
        datapath.IncSP = false;
        datapath.evaluateCombinational();

        int sp = cpu.getStackPointer();
        return cpu.readSystemBus(0x0100 | sp);
    }

    private int packStatusRegister(Cpu cpu) {
        int status = 0x20;
        if (cpu.isFlagSet('C')) status |= 0x01;
        if (cpu.isFlagSet('Z')) status |= 0x02;
        if (cpu.isFlagSet('I')) status |= 0x04;
        if (cpu.isFlagSet('D')) status |= 0x08;
        if (cpu.isFlagSet('V')) status |= 0x40;
        if (cpu.isFlagSet('N')) status |= 0x80;
        return status;
    }

    private void unpackStatusRegister(Cpu cpu, int status) {
        cpu.forceFlag('C', (status & 0x01) != 0);
        cpu.forceFlag('Z', (status & 0x02) != 0);
        cpu.forceFlag('I', (status & 0x04) != 0);
        cpu.forceFlag('D', (status & 0x08) != 0);
        cpu.forceFlag('V', (status & 0x40) != 0);
        cpu.forceFlag('N', (status & 0x80) != 0);
    }

    // ========================================================================
    // HARDWARE EXECUTION LOGIC
    // ========================================================================

    private void executePha(Cpu cpu) {
        this.pushStack(cpu, cpu.getAccumulator());
    }

    private void executePla(Cpu cpu) {
        int value = this.pullStack(cpu);

        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(value);
        datapath.BypassALU = true;
        datapath.LoadA = true;

        cpu.pulseClock();

        datapath.BypassALU = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(value);
    }

    private void executePhp(Cpu cpu) {
        this.pushStack(cpu, this.packStatusRegister(cpu) | 0x10); // Push with B flag set
    }

    private void executePlp(Cpu cpu) {
        int status = this.pullStack(cpu);
        this.unpackStatusRegister(cpu, status);
    }

    private void executeTsx(Cpu cpu) {
        int sp = cpu.getStackPointer();

        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(sp);
        datapath.LoadX = true;

        cpu.pulseClock();

        datapath.LoadX = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(sp);
    }

    private void executeTxs(Cpu cpu) {
        int x = cpu.getRegisterX();

        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(x);
        datapath.LoadSP = true;

        cpu.pulseClock();

        datapath.LoadSP = false;
        datapath.evaluateCombinational();
    }
}