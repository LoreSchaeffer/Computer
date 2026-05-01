package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.emulator.emulated.InstructionLevelCpu;
import it.lycoris.j6502.emulator.hardware.GateLevelCpu;

import java.util.Map;

/**
 * Registers system and control flow instructions (JMP, JSR, RTS, NOP, BRK, RTI).
 * Supports polymorphic execution across different CPU emulation strategies.
 */
public class JumpSystemGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- JMP (Jump) ---
        registry.put(0x4C, new OpcodeMetadata("JMP $abs", cpu -> this.performJump(cpu, this.resolveAbsolute(cpu))));
        registry.put(0x6C, new OpcodeMetadata("JMP ($abs)", cpu -> this.performJump(cpu, this.resolveIndirect(cpu))));

        // --- Subroutines and Interrupts ---
        registry.put(0x20, new OpcodeMetadata("JSR $abs", this::jsr));
        registry.put(0x60, new OpcodeMetadata("RTS", this::rts));
        registry.put(0x00, new OpcodeMetadata("BRK", this::brk));
        registry.put(0x40, new OpcodeMetadata("RTI", this::rti));

        // --- NOP (No Operation) ---
        registry.put(0xEA, new OpcodeMetadata("NOP", _ -> {
        }));
    }

    // ========================================================================
    // ADDRESS RESOLUTION ABSTRACTIONS
    // ========================================================================

    private int resolveAbsolute(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrAbsolute();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.fetchNextAddress();
        throw new UnsupportedOperationException("Unsupported CPU architecture for Absolute fetch.");
    }

    private int resolveIndirect(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.addrIndirect();
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int pointer = fastCpu.fetchNextAddress();
            int lowByte = fastCpu.readSystemBus(pointer);
            int highByte = fastCpu.readSystemBus((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));
            return (highByte << 8) | lowByte;
        }
        throw new UnsupportedOperationException("Unsupported CPU architecture for Indirect fetch.");
    }

    private int getCurrentPc(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.getAddressBus();
        else if (cpu instanceof InstructionLevelCpu fastCpu) return fastCpu.getProgramCounter();
        throw new UnsupportedOperationException("Unsupported CPU architecture for PC retrieval.");
    }

    // ========================================================================
    // EXECUTION HELPER ABSTRACTIONS
    // ========================================================================

    private void performJump(Cpu cpu, int address) {
        if (cpu instanceof GateLevelCpu hardwareCpu) hardwareCpu.jump(address);
        else if (cpu instanceof InstructionLevelCpu fastCpu) fastCpu.setProgramCounter(address);
    }

    private void pushStack(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) hardwareCpu.pushStack(value);
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int sp = fastCpu.readRegisterDirectly("SP");
            fastCpu.writeSystemBus(0x0100 | sp, value);
            fastCpu.setStackPointer((sp - 1) & 0xFF);
        }
    }

    private int pullStack(Cpu cpu) {
        if (cpu instanceof GateLevelCpu hardwareCpu) return hardwareCpu.pullStack();
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            int sp = (fastCpu.readRegisterDirectly("SP") + 1) & 0xFF;
            fastCpu.setStackPointer(sp);
            return fastCpu.readSystemBus(0x0100 | sp);
        }
        throw new UnsupportedOperationException("Unsupported CPU architecture for stack pull.");
    }

    private void setFlagI(Cpu cpu, boolean state) {
        if (cpu instanceof GateLevelCpu hardwareCpu) hardwareCpu.forceFlag('I', state);
        else if (cpu instanceof InstructionLevelCpu fastCpu) fastCpu.setFlagI(state);
    }

    private void setStatusRegister(Cpu cpu, int value) {
        if (cpu instanceof GateLevelCpu hardwareCpu) hardwareCpu.setStatusRegister(value);
        else if (cpu instanceof InstructionLevelCpu fastCpu) {
            fastCpu.setFlagC((value & 0x01) != 0);
            fastCpu.setFlagZ((value & 0x02) != 0);
            fastCpu.setFlagI((value & 0x04) != 0);
            fastCpu.setFlagD((value & 0x08) != 0);
            fastCpu.setFlagV((value & 0x40) != 0);
            fastCpu.setFlagN((value & 0x80) != 0);
        }
    }

    // ========================================================================
    // POLYMORPHIC EXECUTION LOGIC
    // ========================================================================

    private void jsr(Cpu cpu) {
        int targetAddr = this.resolveAbsolute(cpu);
        int returnAddr = this.getCurrentPc(cpu) - 1;

        this.pushStack(cpu, (returnAddr >> 8) & 0xFF);
        this.pushStack(cpu, returnAddr & 0xFF);
        this.performJump(cpu, targetAddr);
    }

    private void rts(Cpu cpu) {
        int lowByte = this.pullStack(cpu);
        int highByte = this.pullStack(cpu);
        int returnAddr = ((highByte << 8) | lowByte) + 1;
        this.performJump(cpu, returnAddr);
    }

    private void brk(Cpu cpu) {
        int returnPc = this.getCurrentPc(cpu) + 1;

        this.pushStack(cpu, (returnPc >> 8) & 0xFF);
        this.pushStack(cpu, returnPc & 0xFF);
        this.pushStack(cpu, cpu.getStatusRegister() | 0x10);
        this.setFlagI(cpu, true);

        int lowByte = cpu.readSystemBus(0xFFFE);
        int highByte = cpu.readSystemBus(0xFFFF);
        this.performJump(cpu, (highByte << 8) | lowByte);
    }

    private void rti(Cpu cpu) {
        this.setStatusRegister(cpu, this.pullStack(cpu));
        int lowByte = this.pullStack(cpu);
        int highByte = this.pullStack(cpu);
        this.performJump(cpu, (highByte << 8) | lowByte);
    }
}