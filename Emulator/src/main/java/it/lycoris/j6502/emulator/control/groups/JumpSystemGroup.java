package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;

import java.util.Map;

public class JumpSystemGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- JMP (Jump) ---
        registry.put(0x4C, new OpcodeMetadata("JMP $abs", cpu -> cpu.jump(cpu.addrAbsolute())));
        registry.put(0x6C, new OpcodeMetadata("JMP ($abs)", cpu -> cpu.jump(cpu.addrIndirect())));

        // --- JSR (Jump to Subroutine) ---
        registry.put(0x20, new OpcodeMetadata("JSR $abs", cpu -> {
            int targetAddr = cpu.addrAbsolute();
            // JSR pushes the return address minus one
            int returnAddr = cpu.getAddressBus() - 1;
            cpu.pushStack((returnAddr >> 8) & 0xFF); // Push High Byte
            cpu.pushStack(returnAddr & 0xFF);        // Push Low Byte
            cpu.jump(targetAddr);
        }));

        // --- RTS (Return from Subroutine) ---
        registry.put(0x60, new OpcodeMetadata("RTS", cpu -> {
            int lo = cpu.pullStack();
            int hi = cpu.pullStack();
            int returnAddr = ((hi << 8) | lo) + 1; // PC is restored + 1
            cpu.jump(returnAddr);
        }));

        // --- NOP (No Operation) ---
        registry.put(0xEA, new OpcodeMetadata("NOP", cpu -> {
            // Literally does nothing, just burns cycles.
        }));

        // --- BRK (Force Interrupt) ---
        registry.put(0x00, new OpcodeMetadata("BRK", cpu -> {
            int pc = cpu.getAddressBus() + 1; // Return address
            cpu.pushStack((pc >> 8) & 0xFF);
            cpu.pushStack(pc & 0xFF);
            cpu.pushStack(cpu.getStatusRegister() | 0x10); // Push Status with Break flag
            cpu.forceFlag('I', true); // Disable interrupts

            // Jump to the IRQ Vector stored at $FFFE-$FFFF
            int lo = cpu.readSystemBus(0xFFFE);
            int hi = cpu.readSystemBus(0xFFFF);
            cpu.jump((hi << 8) | lo);
        }));

        // --- RTI (Return from Interrupt) ---
        registry.put(0x40, new OpcodeMetadata("RTI", cpu -> {
            cpu.setStatusRegister(cpu.pullStack());
            int lo = cpu.pullStack();
            int hi = cpu.pullStack();
            cpu.jump((hi << 8) | lo);
        }));
    }
}
