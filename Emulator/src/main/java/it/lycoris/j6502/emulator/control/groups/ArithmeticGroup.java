package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.hardware.MOS6502;

import java.util.Map;

public class ArithmeticGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ADC (Add with Carry) ---
        registry.put(0x69, new OpcodeMetadata("ADC #", cpu -> adc(cpu, cpu.fetchOperand())));
        registry.put(0x65, new OpcodeMetadata("ADC $zp", cpu -> adc(cpu, cpu.readSystemBus(cpu.addrZeroPage()))));
        registry.put(0x75, new OpcodeMetadata("ADC $zp,X", cpu -> adc(cpu, cpu.readSystemBus(cpu.addrZeroPageX()))));
        registry.put(0x6D, new OpcodeMetadata("ADC $abs", cpu -> adc(cpu, cpu.readSystemBus(cpu.addrAbsolute()))));
        registry.put(0x7D, new OpcodeMetadata("ADC $abs,X", cpu -> adc(cpu, cpu.readSystemBus(cpu.addrAbsoluteX()))));
        registry.put(0x79, new OpcodeMetadata("ADC $abs,Y", cpu -> adc(cpu, cpu.readSystemBus(cpu.addrAbsoluteY()))));

        // --- SBC (Subtract with Carry) ---
        registry.put(0xE9, new OpcodeMetadata("SBC #", cpu -> sbc(cpu, cpu.fetchOperand())));
        registry.put(0xE5, new OpcodeMetadata("SBC $zp", cpu -> sbc(cpu, cpu.readSystemBus(cpu.addrZeroPage()))));
        registry.put(0xF5, new OpcodeMetadata("SBC $zp,X", cpu -> sbc(cpu, cpu.readSystemBus(cpu.addrZeroPageX()))));
        registry.put(0xED, new OpcodeMetadata("SBC $abs", cpu -> sbc(cpu, cpu.readSystemBus(cpu.addrAbsolute()))));
        registry.put(0xFD, new OpcodeMetadata("SBC $abs,X", cpu -> sbc(cpu, cpu.readSystemBus(cpu.addrAbsoluteX()))));
        registry.put(0xF9, new OpcodeMetadata("SBC $abs,Y", cpu -> sbc(cpu, cpu.readSystemBus(cpu.addrAbsoluteY()))));

        // --- Increments/Decrements ---
        registry.put(0xE8, new OpcodeMetadata("INX", cpu -> {
            cpu.indexOp("IncX");
            cpu.updateZAndNFlags(cpu.snapshot().x());
        }));
        registry.put(0xCA, new OpcodeMetadata("DEX", cpu -> {
            cpu.indexOp("DecX");
            cpu.updateZAndNFlags(cpu.snapshot().x());
        }));
        registry.put(0xC8, new OpcodeMetadata("INY", cpu -> {
            cpu.indexOp("IncY");
            cpu.updateZAndNFlags(cpu.snapshot().y());
        }));
        registry.put(0x88, new OpcodeMetadata("DEY", cpu -> {
            cpu.indexOp("DecY");
            cpu.updateZAndNFlags(cpu.snapshot().y());
        }));

        // --- INC (Increment Memory) ---
        registry.put(0xE6, new OpcodeMetadata("INC $zp", cpu -> incMem(cpu, cpu.addrZeroPage())));
        registry.put(0xF6, new OpcodeMetadata("INC $zp,X", cpu -> incMem(cpu, cpu.addrZeroPageX())));
        registry.put(0xEE, new OpcodeMetadata("INC $abs", cpu -> incMem(cpu, cpu.addrAbsolute())));
        registry.put(0xFE, new OpcodeMetadata("INC $abs,X", cpu -> incMem(cpu, cpu.addrAbsoluteX())));

        // --- DEC (Decrement Memory) ---
        registry.put(0xC6, new OpcodeMetadata("DEC $zp", cpu -> decMem(cpu, cpu.addrZeroPage())));
        registry.put(0xD6, new OpcodeMetadata("DEC $zp,X", cpu -> decMem(cpu, cpu.addrZeroPageX())));
        registry.put(0xCE, new OpcodeMetadata("DEC $abs", cpu -> decMem(cpu, cpu.addrAbsolute())));
        registry.put(0xDE, new OpcodeMetadata("DEC $abs,X", cpu -> decMem(cpu, cpu.addrAbsoluteX())));
    }

    private void adc(MOS6502 cpu, int val) {
        cpu.executeALU("OpADD", val, cpu.isFlagSet('C'), true);
    }

    private void sbc(MOS6502 cpu, int val) {
        cpu.executeALU("OpADD", (~val) & 0xFF, cpu.isFlagSet('C'), true);
    }

    // New helper methods for memory increment/decrement
    private void incMem(MOS6502 cpu, int addr) {
        int val = (cpu.readSystemBus(addr) + 1) & 0xFF;
        cpu.writeSystemBus(addr, val);
        cpu.updateZAndNFlags(val);
    }

    private void decMem(MOS6502 cpu, int addr) {
        int val = (cpu.readSystemBus(addr) - 1) & 0xFF;
        cpu.writeSystemBus(addr, val);
        cpu.updateZAndNFlags(val);
    }
}