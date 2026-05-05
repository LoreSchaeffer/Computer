package it.lycoris.j6502.emulator.instructions.microcode;

import it.lycoris.j6502.emulator.instructions.InstructionGroup;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.emulator.core.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers arithmetic instructions (ADC, SBC, INC, DEC, INX, DEX, INY, DEY).
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
        registry.put(0x61, new OpcodeMetadata("ADC ($zp,X)", cpu -> this.adc(cpu, this.fetchIndexedIndirectX(cpu))));
        registry.put(0x71, new OpcodeMetadata("ADC ($zp),Y", cpu -> this.adc(cpu, this.fetchIndirectIndexedY(cpu))));

        // --- SBC (Subtract with Carry) ---
        registry.put(0xE9, new OpcodeMetadata("SBC #", cpu -> this.sbc(cpu, this.fetchImmediate(cpu))));
        registry.put(0xE5, new OpcodeMetadata("SBC $zp", cpu -> this.sbc(cpu, this.fetchZeroPage(cpu))));
        registry.put(0xF5, new OpcodeMetadata("SBC $zp,X", cpu -> this.sbc(cpu, this.fetchZeroPageX(cpu))));
        registry.put(0xED, new OpcodeMetadata("SBC $abs", cpu -> this.sbc(cpu, this.fetchAbsolute(cpu))));
        registry.put(0xFD, new OpcodeMetadata("SBC $abs,X", cpu -> this.sbc(cpu, this.fetchAbsoluteX(cpu))));
        registry.put(0xF9, new OpcodeMetadata("SBC $abs,Y", cpu -> this.sbc(cpu, this.fetchAbsoluteY(cpu))));
        registry.put(0xE1, new OpcodeMetadata("SBC ($zp,X)", cpu -> this.sbc(cpu, this.fetchIndexedIndirectX(cpu))));
        registry.put(0xF1, new OpcodeMetadata("SBC ($zp),Y", cpu -> this.sbc(cpu, this.fetchIndirectIndexedY(cpu))));

        // --- Index Register Increments/Decrements ---
        registry.put(0xE8, new OpcodeMetadata("INX", this::executeInx));
        registry.put(0xCA, new OpcodeMetadata("DEX", this::executeDex));
        registry.put(0xC8, new OpcodeMetadata("INY", this::executeIny));
        registry.put(0x88, new OpcodeMetadata("DEY", this::executeDey));

        // --- Memory Increments/Decrements ---
        registry.put(0xE6, new OpcodeMetadata("INC $zp", cpu -> this.incMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xF6, new OpcodeMetadata("INC $zp,X", cpu -> this.incMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xEE, new OpcodeMetadata("INC $abs", cpu -> this.incMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xFE, new OpcodeMetadata("INC $abs,X", cpu -> this.incMem(cpu, this.resolveAbsoluteX(cpu))));

        registry.put(0xC6, new OpcodeMetadata("DEC $zp", cpu -> this.decMem(cpu, this.resolveZeroPage(cpu))));
        registry.put(0xD6, new OpcodeMetadata("DEC $zp,X", cpu -> this.decMem(cpu, this.resolveZeroPageX(cpu))));
        registry.put(0xCE, new OpcodeMetadata("DEC $abs", cpu -> this.decMem(cpu, this.resolveAbsolute(cpu))));
        registry.put(0xDE, new OpcodeMetadata("DEC $abs,X", cpu -> this.decMem(cpu, this.resolveAbsoluteX(cpu))));
    }

    // ========================================================================
    // LOGIC EXECUTION WITH BCD SUPPORT
    // ========================================================================

    private void adc(Cpu cpu, int value) {
        int accumulator = cpu.getAccumulator();
        int carryIn = cpu.isFlagSet('C') ? 1 : 0;
        int binarySum = accumulator + value + carryIn;

        // In NMOS 6502, N, V, and Z flags in Decimal mode are calculated
        // based on the regular BINARY sum, not the BCD adjusted result!
        boolean zFlag = (binarySum & 0xFF) == 0;
        boolean nFlag = (binarySum & 0x80) != 0;
        boolean vFlag = (~(accumulator ^ value) & (accumulator ^ binarySum) & 0x80) != 0;

        int finalResult;
        if (cpu.isFlagSet('D')) {
            int lowerNibble = (accumulator & 0x0F) + (value & 0x0F) + carryIn;
            int upperNibble = (accumulator >> 4) + (value >> 4) + (lowerNibble > 0x09 ? 1 : 0);

            if (lowerNibble > 0x09) {
                lowerNibble += 0x06;
            }

            boolean bcdCarry = upperNibble > 0x09;
            if (bcdCarry) {
                upperNibble += 0x06;
            }

            finalResult = ((upperNibble << 4) | (lowerNibble & 0x0F)) & 0xFF;
            cpu.forceFlag('C', bcdCarry);
        } else {
            finalResult = binarySum & 0xFF;
            cpu.forceFlag('C', binarySum > 0xFF);
        }

        cpu.forceFlag('Z', zFlag);
        cpu.forceFlag('N', nFlag);
        cpu.forceFlag('V', vFlag);

        this.latchAccumulator(cpu, finalResult);
    }

    private void sbc(Cpu cpu, int value) {
        int accumulator = cpu.getAccumulator();
        int carryIn = cpu.isFlagSet('C') ? 1 : 0;
        int invertedValue = (~value) & 0xFF;
        int binarySum = accumulator + invertedValue + carryIn;

        // Flags N, V, Z follow binary subtraction rules even in Decimal Mode
        boolean zFlag = (binarySum & 0xFF) == 0;
        boolean nFlag = (binarySum & 0x80) != 0;
        boolean vFlag = ((accumulator ^ binarySum) & (invertedValue ^ binarySum) & 0x80) != 0;

        int finalResult;
        if (cpu.isFlagSet('D')) {
            int lowerNibble = (accumulator & 0x0F) - (value & 0x0F) - (1 - carryIn);
            int upperNibble = (accumulator >> 4) - (value >> 4) - (lowerNibble < 0 ? 1 : 0);

            if (lowerNibble < 0) {
                lowerNibble -= 0x06;
            }
            if (upperNibble < 0) {
                upperNibble -= 0x06;
            }

            finalResult = ((upperNibble << 4) | (lowerNibble & 0x0F)) & 0xFF;

            // In NMOS 6502, SBC carry in BCD mode is identical to binary mode carry
            cpu.forceFlag('C', binarySum > 0xFF);
        } else {
            finalResult = binarySum & 0xFF;
            cpu.forceFlag('C', binarySum > 0xFF);
        }

        cpu.forceFlag('Z', zFlag);
        cpu.forceFlag('N', nFlag);
        cpu.forceFlag('V', vFlag);

        this.latchAccumulator(cpu, finalResult);
    }

    private void latchAccumulator(Cpu cpu, int result) {
        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(result);
        datapath.BypassALU = true;
        datapath.LoadA = true;
        cpu.pulseClock();
        datapath.BypassALU = false;
        datapath.LoadA = false;
        datapath.evaluateCombinational();
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

    // ========================================================================
    // ADDRESS FETCHING
    // ========================================================================

    private int fetchImmediate(Cpu cpu) {
        return cpu.fetchNextByte();
    }

    private int fetchZeroPage(Cpu cpu) {
        return cpu.readSystemBus(cpu.fetchNextByte());
    }

    private int fetchZeroPageX(Cpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF);
    }

    private int fetchAbsolute(Cpu cpu) {
        return cpu.readSystemBus(cpu.fetchNextAddress());
    }

    private int fetchAbsoluteX(Cpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextAddress() + cpu.getRegisterX()) & 0xFFFF);
    }

    private int fetchAbsoluteY(Cpu cpu) {
        return cpu.readSystemBus((cpu.fetchNextAddress() + cpu.getRegisterY()) & 0xFFFF);
    }

    private int fetchIndexedIndirectX(Cpu cpu) {
        int zpAddress = (cpu.fetchNextByte() + cpu.getRegisterX()) & 0xFF;
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        return cpu.readSystemBus((highByte << 8) | lowByte);
    }

    private int fetchIndirectIndexedY(Cpu cpu) {
        int zpAddress = cpu.fetchNextByte();
        int lowByte = cpu.readSystemBus(zpAddress);
        int highByte = cpu.readSystemBus((zpAddress + 1) & 0xFF);
        int baseAddress = (highByte << 8) | lowByte;
        return cpu.readSystemBus((baseAddress + cpu.getRegisterY()) & 0xFFFF);
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
}