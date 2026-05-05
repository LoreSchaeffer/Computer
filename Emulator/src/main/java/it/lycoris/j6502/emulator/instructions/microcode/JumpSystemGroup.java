package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers system and control flow instructions (JMP, JSR, RTS, NOP, BRK, RTI).
 * Drives the Program Counter and Stack Pointer hardware pins.
 */
public class JumpSystemGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x4C, new OpcodeMetadata("JMP $abs", cpu -> cpu.jump(this.resolveAbsolute(cpu))));
        registry.put(0x6C, new OpcodeMetadata("JMP ($abs)", cpu -> cpu.jump(this.resolveIndirect(cpu))));

        registry.put(0x20, new OpcodeMetadata("JSR $abs", this::jsr));
        registry.put(0x60, new OpcodeMetadata("RTS", this::rts));
        registry.put(0x00, new OpcodeMetadata("BRK", this::brk));
        registry.put(0x40, new OpcodeMetadata("RTI", this::rti));

        registry.put(0xEA, new OpcodeMetadata("NOP", _ -> {
            // NOP simply consumes a cycle
        }));
    }

    // ========================================================================
    // ADDRESS RESOLUTION ABSTRACTIONS
    // ========================================================================

    private int resolveAbsolute(Cpu cpu) {
        return cpu.fetchNextAddress();
    }

    private int resolveIndirect(Cpu cpu) {
        int pointer = cpu.fetchNextAddress();
        int lowByte = cpu.readSystemBus(pointer);
        // Page boundary wrap-around bug in 6502 hardware
        int highByte = cpu.readSystemBus((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));
        return (highByte << 8) | lowByte;
    }

    // ========================================================================
    // STACK AND STATUS HELPERS
    // ========================================================================

    private void pushStack(Cpu cpu, int value) {
        int sp = cpu.getStackPointer();
        cpu.writeSystemBus(0x0100 | sp, value);

        // Assert hardware pin to decrement SP
        MOS6502 datapath = cpu.getDatapath();
        datapath.DecSP = true;
        cpu.pulseClock();
        datapath.DecSP = false;
        datapath.evaluateCombinational();
    }

    private int pullStack(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();

        // Assert hardware pin to increment SP first (Empty stack grows downwards)
        datapath.IncSP = true;
        cpu.pulseClock();
        datapath.IncSP = false;
        datapath.evaluateCombinational();

        int sp = cpu.getStackPointer();
        return cpu.readSystemBus(0x0100 | sp);
    }

    private int packStatusRegister(Cpu cpu) {
        int status = 0x20; // Bit 5 is always 1
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

    private void jsr(Cpu cpu) {
        int targetAddr = this.resolveAbsolute(cpu);
        int returnAddr = cpu.readAddressOutPins() - 1; // PC is already at next instruction

        this.pushStack(cpu, (returnAddr >> 8) & 0xFF);
        this.pushStack(cpu, returnAddr & 0xFF);
        cpu.jump(targetAddr);
    }

    private void rts(Cpu cpu) {
        int lowByte = this.pullStack(cpu);
        int highByte = this.pullStack(cpu);
        int returnAddr = ((highByte << 8) | lowByte) + 1;
        cpu.jump(returnAddr);
    }

    private void brk(Cpu cpu) {
        int returnPc = cpu.readAddressOutPins() + 1; // BRK skips the byte after opcode

        this.pushStack(cpu, (returnPc >> 8) & 0xFF);
        this.pushStack(cpu, returnPc & 0xFF);
        this.pushStack(cpu, this.packStatusRegister(cpu) | 0x10); // Push with B-flag set

        cpu.forceFlag('I', true);

        int lowByte = cpu.readSystemBus(0xFFFE);
        int highByte = cpu.readSystemBus(0xFFFF);
        cpu.jump((highByte << 8) | lowByte);
    }

    private void rti(Cpu cpu) {
        this.unpackStatusRegister(cpu, this.pullStack(cpu));
        int lowByte = this.pullStack(cpu);
        int highByte = this.pullStack(cpu);
        cpu.jump((highByte << 8) | lowByte);
    }
}