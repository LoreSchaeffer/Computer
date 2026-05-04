package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers arithmetic instructions (ADC, SBC, INC, DEC, INX, DEX, INY, DEY).
 * Executes operations exclusively by manipulating the hardware datapath pins.
 */
public class ArithmeticGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0x69, new OpcodeMetadata("ADC #", cpu -> this.adc(cpu, this.fetchImmediate(cpu))));
        registry.put(0x65, new OpcodeMetadata("ADC $zp", cpu -> this.adc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x75, new OpcodeMetadata("ADC $zp,X", cpu -> this.adc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0x6D, new OpcodeMetadata("ADC $abs", cpu -> this.adc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0x7D, new OpcodeMetadata("ADC $abs,X", cpu -> this.adc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0x79, new OpcodeMetadata("ADC $abs,Y", cpu -> this.adc(cpu, this.fetchAbsoluteY(cpu))));

        registry.put(0xE9, new OpcodeMetadata("SBC #", cpu -> this.sbc(cpu, this.fetchImmediate(cpu))));
        registry.put(0xE5, new OpcodeMetadata("SBC $zp", cpu -> this.sbc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0xF5, new OpcodeMetadata("SBC $zp,X", cpu -> this.sbc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0xED, new OpcodeMetadata("SBC $abs", cpu -> this.sbc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0xFD, new OpcodeMetadata("SBC $abs,X", cpu -> this.sbc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0xF9, new OpcodeMetadata("SBC $abs,Y", cpu -> this.sbc(cpu, this.fetchAbsoluteY(cpu))));

        registry.put(0xE8, new OpcodeMetadata("INX", this::executeInx));
        registry.put(0xCA, new OpcodeMetadata("DEX", this::executeDex));
        registry.put(0xC8, new OpcodeMetadata("INY", this::executeIny));
        registry.put(0x88, new OpcodeMetadata("DEY", this::executeDey));

        registry.put(0xE6, new OpcodeMetadata("INC $zp", cpu -> this.incMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xF6, new OpcodeMetadata("INC $zp,X", cpu -> this.incMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xEE, new OpcodeMetadata("INC $abs", cpu -> this.incMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xFE, new OpcodeMetadata("INC $abs,X", cpu -> this.incMem(cpu, this.resolveAbsoluteX(cpu))));

        registry.put(0xC6, new OpcodeMetadata("DEC $zp", cpu -> this.decMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xD6, new OpcodeMetadata("DEC $zp,X", cpu -> this.decMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xCE, new OpcodeMetadata("DEC $abs", cpu -> this.decMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xDE, new OpcodeMetadata("DEC $abs,X", cpu -> this.decMem(cpu, this.resolveAbsoluteX(cpu))));
    }

    private int fetchImmediate(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int fetchZeroPage(Cpu cpu) {
        int address = cpu.fetchNextByte();
        return cpu.readSystemBus(address);
    }

    private int fetchZeroPageX(Cpu cpu) {
        int address = (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
        return cpu.readSystemBus(address);
    }

    private int fetchAbsolute(Cpu cpu) {
        int address = cpu.fetchNextAddress();
        return cpu.readSystemBus(address);
    }

    private int fetchAbsoluteX(Cpu cpu) {
        int address = (cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF;
        return cpu.readSystemBus(address);
    }

    private int fetchAbsoluteY(Cpu cpu) {
        int address = (cpu.fetchNextAddress() + cpu.getRegisterY()) & 0xFFFF;
        return cpu.readSystemBus(address);
    }

    private int resolveZeroPage(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int resolveZeroPageX(Cpu cpu) {
        return (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
    }

    private int resolveAbsolute(Cpu cpu) {
        return cpu.fetchNextAddress();
    }

    private int resolveAbsoluteX(Cpu cpu) {
        return (cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF;
    }

    private void adc(Cpu cpu, int value) {
        int acc = cpu.getAccumulator();
        int carry = cpu.isFlagSet('C') ? 1 : 0;

        int sum = acc + value + carry;
        int result = sum & 0xFF;

        boolean overflow = ((acc ^ result) & (value ^ result) & 0x80) != 0;

        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(result);

        datapath.BypassALU = true;
        datapath.LoadA = true;
        cpu.pulseClock();

        datapath.BypassALU = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();

        cpu.forceFlag('C', sum > 0xFF);
        cpu.forceFlag('V', overflow);
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void sbc(Cpu cpu, int value) {
        int invertedValue = (~value) & 0xFF;
        this.adc(cpu, invertedValue);
    }

    private void executeInx(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();
        datapath.IncX = true;
        cpu.pulseClock();
        datapath.IncX = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(cpu.getRegisterX());
    }

    private void executeDex(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();
        datapath.DecX = true;
        cpu.pulseClock();
        datapath.DecX = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(cpu.getRegisterX());
    }

    private void executeIny(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();
        datapath.IncY = true;
        cpu.pulseClock();
        datapath.IncY = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(cpu.getRegisterY());
    }

    private void executeDey(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();
        datapath.DecY = true;
        cpu.pulseClock();
        datapath.DecY = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(cpu.getRegisterY());
    }

    private void incMem(Cpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        int result = (value + 1) & 0xFF;
        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void decMem(Cpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        int result = (value - 1) & 0xFF;
        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }
}