package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers arithmetic instructions (ADC, SBC, INC, DEC, INX, DEX, INY, DEY)
 * into the execution environment.
 * Executes operations exclusively by manipulating the hardware datapath pins.
 */
public class ArithmeticGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ADC (Add with Carry) ---
        registry.put(0x69, new OpcodeMetadata("ADC #", cpu -> this.adc(cpu, this.fetchImmediate(cpu))));
        registry.put(0x65, new OpcodeMetadata("ADC $zp", cpu -> this.adc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0x75, new OpcodeMetadata("ADC $zp,X", cpu -> this.adc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0x6D, new OpcodeMetadata("ADC $abs", cpu -> this.adc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0x7D, new OpcodeMetadata("ADC $abs,X", cpu -> this.adc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0x79, new OpcodeMetadata("ADC $abs,Y", cpu -> this.adc(cpu, this.fetchAbsoluteY(cpu))));

        // --- SBC (Subtract with Carry) ---
        registry.put(0xE9, new OpcodeMetadata("SBC #", cpu -> this.sbc(cpu, this.fetchImmediate(cpu))));
        registry.put(0xE5, new OpcodeMetadata("SBC $zp", cpu -> this.sbc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0xF5, new OpcodeMetadata("SBC $zp,X", cpu -> this.sbc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0xED, new OpcodeMetadata("SBC $abs", cpu -> this.sbc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0xFD, new OpcodeMetadata("SBC $abs,X", cpu -> this.sbc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0xF9, new OpcodeMetadata("SBC $abs,Y", cpu -> this.sbc(cpu, this.fetchAbsoluteY(cpu))));

        // --- Increments/Decrements (Register) ---
        registry.put(0xE8, new OpcodeMetadata("INX", this::executeInx));
        registry.put(0xCA, new OpcodeMetadata("DEX", this::executeDex));
        registry.put(0xC8, new OpcodeMetadata("INY", this::executeIny));
        registry.put(0x88, new OpcodeMetadata("DEY", this::executeDey));

        // --- INC (Increment Memory) ---
        registry.put(0xE6, new OpcodeMetadata("INC $zp", cpu -> this.incMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xF6, new OpcodeMetadata("INC $zp,X", cpu -> this.incMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xEE, new OpcodeMetadata("INC $abs", cpu -> this.incMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xFE, new OpcodeMetadata("INC $abs,X", cpu -> this.incMem(cpu, this.resolveAbsoluteX(cpu))));

        // --- DEC (Decrement Memory) ---
        registry.put(0xC6, new OpcodeMetadata("DEC $zp", cpu -> this.decMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xD6, new OpcodeMetadata("DEC $zp,X", cpu -> this.decMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xCE, new OpcodeMetadata("DEC $abs", cpu -> this.decMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xDE, new OpcodeMetadata("DEC $abs,X", cpu -> this.decMem(cpu, this.resolveAbsoluteX(cpu))));
    }

    // ========================================================================
    // VALUE FETCHING ABSTRACTIONS 
    // ========================================================================

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

    // ========================================================================
    // ADDRESS RESOLUTION ABSTRACTIONS
    // ========================================================================

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

    // ========================================================================
    // HARDWARE EXECUTION LOGIC
    // ========================================================================

    private void adc(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        // Assert the fetched value onto the data bus pins
        cpu.assertDataBus(value);

        // Instruct the ALU to perform addition and the Accumulator to latch the result
        datapath.OpADD = true;
        datapath.LoadA = true;

        // Clock edge applies the hardware operation
        cpu.pulseClock();

        // Cleanup hardware pins
        datapath.OpADD = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
    }

    private void sbc(Cpu cpu, int value) {
        MOS6502 datapath = cpu.getDatapath();

        // Subtraction in 6502 is Addition with the inverted operand
        int invertedValue = (~value) & 0xFF;
        cpu.assertDataBus(invertedValue);

        datapath.OpADD = true;
        datapath.LoadA = true;

        cpu.pulseClock();

        datapath.OpADD = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
    }

    private void executeInx(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();

        datapath.IncX = true;
        cpu.pulseClock();
        datapath.IncX = false;
        datapath.evaluateCombinational();
    }

    private void executeDex(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();

        datapath.DecX = true;
        cpu.pulseClock();
        datapath.DecX = false;
        datapath.evaluateCombinational();
    }

    private void executeIny(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();

        datapath.IncY = true;
        cpu.pulseClock();
        datapath.IncY = false;
        datapath.evaluateCombinational();
    }

    private void executeDey(Cpu cpu) {
        MOS6502 datapath = cpu.getDatapath();

        datapath.DecY = true;
        cpu.pulseClock();
        datapath.DecY = false;
        datapath.evaluateCombinational();
    }

    private void incMem(Cpu cpu, int address) {
        // Read memory
        int value = cpu.readSystemBus(address);

        // Since IncMem isn't typically an ALU operation that targets a register,
        // we manually increment and write back, simulating the hardware read-modify-write cycle.
        int result = (value + 1) & 0xFF;

        // Write back
        cpu.writeSystemBus(address, result);

        // Force the N and Z flags based on the result
        cpu.forceZeroAndNegativeFlags(result);
    }

    private void decMem(Cpu cpu, int address) {
        int value = cpu.readSystemBus(address);
        int result = (value - 1) & 0xFF;

        cpu.writeSystemBus(address, result);
        cpu.forceZeroAndNegativeFlags(result);
    }
}