package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;

import java.util.Map;

/**
 * Registers system and control flow instructions (JMP, JSR, RTS, NOP, BRK, RTI).
 * Drives the Program Counter and Stack Pointer hardware pins.
 */
public class JumpSystemGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x4C, new OpcodeMetadata("JMP $abs", cpu -> cpu.jump(cpu.fetchNextAddress())));

        registry.put(0x6C, new OpcodeMetadata("JMP ($abs)", cpu -> {
            int targetAddress = cpu.fetchNextAddress();
            int lowByte = cpu.readSystemBus(targetAddress);
            // 6502 Hardware Bug: If the indirect address falls on a page boundary (xxFF),
            // it fetches the high byte from the start of the same page (xx00) instead of the next page.
            int highAddress = (targetAddress & 0xFF00) | ((targetAddress + 1) & 0x00FF);
            int highByte = cpu.readSystemBus(highAddress);
            cpu.jump((highByte << 8) | lowByte);
        }));

        registry.put(0x20, new OpcodeMetadata("JSR $abs", cpu -> {
            int targetAddress = cpu.fetchNextAddress();
            int returnAddress = (cpu.getProgramCounter() - 1) & 0xFFFF;
            cpu.pushStack((returnAddress >> 8) & 0xFF);
            cpu.pushStack(returnAddress & 0xFF);
            cpu.jump(targetAddress);
        }));

        registry.put(0x60, new OpcodeMetadata("RTS", cpu -> {
            int lowByte = cpu.pullStack();
            int highByte = cpu.pullStack();
            int returnAddress = ((highByte << 8) | lowByte) + 1;
            cpu.jump(returnAddress);
        }));

        registry.put(0x00, new OpcodeMetadata("BRK", cpu -> {
            cpu.fetchNextByte();
            int programCounter = cpu.getProgramCounter();
            cpu.pushStack((programCounter >> 8) & 0xFF);
            cpu.pushStack(programCounter & 0xFF);
            cpu.pushStack(cpu.getStatusRegister() | 0x30);
            cpu.forceFlag('I', true);
            int lowByte = cpu.readSystemBus(0xFFFE);
            int highByte = cpu.readSystemBus(0xFFFF);
            cpu.jump((highByte << 8) | lowByte);
        }));

        registry.put(0x40, new OpcodeMetadata("RTI", cpu -> {
            cpu.setStatusRegister((cpu.pullStack() & 0xEF) | 0x20);
            int lowByte = cpu.pullStack();
            int highByte = cpu.pullStack();
            cpu.jump((highByte << 8) | lowByte);
        }));

        registry.put(0xEA, new OpcodeMetadata("NOP", _ -> { /* NoOp */ }));
    }
}