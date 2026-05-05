package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers Load and Store instructions (LDA, LDX, LDY, STA, STX, STY).
 * Interacts with memory and routes data directly into or out of the hardware registers.
 */
public class LoadStoreGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- LDA (Load Accumulator) ---
        registry.put(0xA9, new OpcodeMetadata("LDA #", cpu -> this.loadA(cpu, this.fetchImmediate(cpu))));
        registry.put(0xA5, new OpcodeMetadata("LDA $zp", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveZeroPage(cpu)))));
        registry.put(0xB5, new OpcodeMetadata("LDA $zp,X", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveZeroPageX(cpu)))));
        registry.put(0xAD, new OpcodeMetadata("LDA $abs", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveAbsolute(cpu)))));
        registry.put(0xBD, new OpcodeMetadata("LDA $abs,X", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveAbsoluteX(cpu)))));
        registry.put(0xB9, new OpcodeMetadata("LDA $abs,Y", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveAbsoluteY(cpu)))));
        registry.put(0xA1, new OpcodeMetadata("LDA ($zp,X)", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveIndexedIndirectX(cpu)))));
        registry.put(0xB1, new OpcodeMetadata("LDA ($zp),Y", cpu -> this.loadA(cpu, cpu.readSystemBus(this.resolveIndirectIndexedY(cpu)))));

        // --- LDX (Load X Register) ---
        registry.put(0xA2, new OpcodeMetadata("LDX #", cpu -> this.loadX(cpu, this.fetchImmediate(cpu))));
        registry.put(0xA6, new OpcodeMetadata("LDX $zp", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveZeroPage(cpu)))));
        registry.put(0xB6, new OpcodeMetadata("LDX $zp,Y", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveZeroPageY(cpu)))));
        registry.put(0xAE, new OpcodeMetadata("LDX $abs", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveAbsolute(cpu)))));
        registry.put(0xBE, new OpcodeMetadata("LDX $abs,Y", cpu -> this.loadX(cpu, cpu.readSystemBus(this.resolveAbsoluteY(cpu)))));

        // --- LDY (Load Y Register) ---
        registry.put(0xA0, new OpcodeMetadata("LDY #", cpu -> this.loadY(cpu, this.fetchImmediate(cpu))));
        registry.put(0xA4, new OpcodeMetadata("LDY $zp", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveZeroPage(cpu)))));
        registry.put(0xB4, new OpcodeMetadata("LDY $zp,X", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveZeroPageX(cpu)))));
        registry.put(0xAC, new OpcodeMetadata("LDY $abs", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveAbsolute(cpu)))));
        registry.put(0xBC, new OpcodeMetadata("LDY $abs,X", cpu -> this.loadY(cpu, cpu.readSystemBus(this.resolveAbsoluteX(cpu)))));

        // --- STA (Store Accumulator) ---
        registry.put(0x85, new OpcodeMetadata("STA $zp", cpu -> cpu.writeSystemBus(this.resolveZeroPage(cpu), cpu.getAccumulator())));
        registry.put(0x95, new OpcodeMetadata("STA $zp,X", cpu -> cpu.writeSystemBus(this.resolveZeroPageX(cpu), cpu.getAccumulator())));
        registry.put(0x8D, new OpcodeMetadata("STA $abs", cpu -> cpu.writeSystemBus(this.resolveAbsolute(cpu), cpu.getAccumulator())));
        registry.put(0x9D, new OpcodeMetadata("STA $abs,X", cpu -> cpu.writeSystemBus(this.resolveAbsoluteX(cpu), cpu.getAccumulator())));
        registry.put(0x99, new OpcodeMetadata("STA $abs,Y", cpu -> cpu.writeSystemBus(this.resolveAbsoluteY(cpu), cpu.getAccumulator())));
        registry.put(0x81, new OpcodeMetadata("STA ($zp,X)", cpu -> cpu.writeSystemBus(this.resolveIndexedIndirectX(cpu), cpu.getAccumulator())));
        registry.put(0x91, new OpcodeMetadata("STA ($zp),Y", cpu -> cpu.writeSystemBus(this.resolveIndirectIndexedY(cpu), cpu.getAccumulator())));

        // --- STX & STY (Store X and Y Registers) ---
        registry.put(0x86, new OpcodeMetadata("STX $zp", cpu -> cpu.writeSystemBus(this.resolveZeroPage(cpu), cpu.getRegisterX())));
        registry.put(0x8E, new OpcodeMetadata("STX $abs", cpu -> cpu.writeSystemBus(this.resolveAbsolute(cpu), cpu.getRegisterX())));
        registry.put(0x84, new OpcodeMetadata("STY $zp", cpu -> cpu.writeSystemBus(this.resolveZeroPage(cpu), cpu.getRegisterY())));
        registry.put(0x8C, new OpcodeMetadata("STY $abs", cpu -> cpu.writeSystemBus(this.resolveAbsolute(cpu), cpu.getRegisterY())));
    }

    // ========================================================================
    // ADDRESSING MODE RESOLUTION
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int resolveZeroPage(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int resolveZeroPageX(Cpu cpu) {
        return (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
    }

    private int resolveZeroPageY(Cpu cpu) {
        return (cpu.fetchNextByte() + cpu.getRegisterY()) & 0xFF;
    }

    private int resolveAbsolute(Cpu cpu) {
        return cpu.fetchNextAddress();
    }

    private int resolveAbsoluteX(Cpu cpu) {
        return (cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF;
    }

    private int resolveAbsoluteY(Cpu cpu) {
        return (cpu.fetchNextAddress() + cpu.getRegisterY()) & 0xFFFF;
    }

    private int resolveIndexedIndirectX(Cpu cpu) {
        int zpAddress = (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        return (highByte << 8) | lowByte;
    }

    private int resolveIndirectIndexedY(Cpu cpu) {
        int zpAddress = cpu.fetchNextByte();
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        int baseAddress = (highByte << 8) | lowByte;
        return (baseAddress + cpu.getRegisterY()) & 0xFFFF;
    }

    // ========================================================================
    // HARDWARE EXECUTION LOGIC
    // ========================================================================

    private void loadA(Cpu cpu, int value) {
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

    private void loadX(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        cpu.assertDataBus(value);
        datapath.LoadX = true;

        cpu.pulseClock();

        datapath.LoadX = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(value);
    }

    private void loadY(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        cpu.assertDataBus(value);
        datapath.LoadY = true;

        cpu.pulseClock();

        datapath.LoadY = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(value);
    }
}