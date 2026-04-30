package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.hardware.MOS6502;

import java.util.Map;

public class ShiftRotateGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- ASL (Arithmetic Shift Left) ---
        registry.put(0x0A, new OpcodeMetadata("ASL A", cpu -> doASL(cpu, -1))); // -1 indicates Accumulator
        registry.put(0x06, new OpcodeMetadata("ASL $zp", cpu -> doASL(cpu, cpu.addrZeroPage())));
        registry.put(0x16, new OpcodeMetadata("ASL $zp,X", cpu -> doASL(cpu, cpu.addrZeroPageX())));
        registry.put(0x0E, new OpcodeMetadata("ASL $abs", cpu -> doASL(cpu, cpu.addrAbsolute())));
        registry.put(0x1E, new OpcodeMetadata("ASL $abs,X", cpu -> doASL(cpu, cpu.addrAbsoluteX())));

        // --- LSR (Logical Shift Right) ---
        registry.put(0x4A, new OpcodeMetadata("LSR A", cpu -> doLSR(cpu, -1)));
        registry.put(0x46, new OpcodeMetadata("LSR $zp", cpu -> doLSR(cpu, cpu.addrZeroPage())));
        registry.put(0x56, new OpcodeMetadata("LSR $zp,X", cpu -> doLSR(cpu, cpu.addrZeroPageX())));
        registry.put(0x4E, new OpcodeMetadata("LSR $abs", cpu -> doLSR(cpu, cpu.addrAbsolute())));
        registry.put(0x5E, new OpcodeMetadata("LSR $abs,X", cpu -> doLSR(cpu, cpu.addrAbsoluteX())));

        // --- ROL (Rotate Left) ---
        registry.put(0x2A, new OpcodeMetadata("ROL A", cpu -> doROL(cpu, -1)));
        registry.put(0x26, new OpcodeMetadata("ROL $zp", cpu -> doROL(cpu, cpu.addrZeroPage())));
        registry.put(0x36, new OpcodeMetadata("ROL $zp,X", cpu -> doROL(cpu, cpu.addrZeroPageX())));
        registry.put(0x2E, new OpcodeMetadata("ROL $abs", cpu -> doROL(cpu, cpu.addrAbsolute())));
        registry.put(0x3E, new OpcodeMetadata("ROL $abs,X", cpu -> doROL(cpu, cpu.addrAbsoluteX())));

        // --- ROR (Rotate Right) ---
        registry.put(0x6A, new OpcodeMetadata("ROR A", cpu -> doROR(cpu, -1)));
        registry.put(0x66, new OpcodeMetadata("ROR $zp", cpu -> doROR(cpu, cpu.addrZeroPage())));
        registry.put(0x76, new OpcodeMetadata("ROR $zp,X", cpu -> doROR(cpu, cpu.addrZeroPageX())));
        registry.put(0x6E, new OpcodeMetadata("ROR $abs", cpu -> doROR(cpu, cpu.addrAbsolute())));
        registry.put(0x7E, new OpcodeMetadata("ROR $abs,X", cpu -> doROR(cpu, cpu.addrAbsoluteX())));
    }

    private void doASL(MOS6502 cpu, int addr) {
        int val = (addr == -1) ? cpu.getAccumulator() : cpu.readSystemBus(addr);
        cpu.forceFlag('C', (val & 0x80) != 0); // Old bit 7 becomes Carry
        val = (val << 1) & 0xFF;
        writeBack(cpu, addr, val);
    }

    private void doLSR(MOS6502 cpu, int addr) {
        int val = (addr == -1) ? cpu.getAccumulator() : cpu.readSystemBus(addr);
        cpu.forceFlag('C', (val & 0x01) != 0); // Old bit 0 becomes Carry
        val = (val >> 1) & 0xFF;
        writeBack(cpu, addr, val);
    }

    private void doROL(MOS6502 cpu, int addr) {
        int val = (addr == -1) ? cpu.getAccumulator() : cpu.readSystemBus(addr);
        boolean oldCarry = cpu.isFlagSet('C');
        cpu.forceFlag('C', (val & 0x80) != 0);
        val = ((val << 1) | (oldCarry ? 1 : 0)) & 0xFF;
        writeBack(cpu, addr, val);
    }

    private void doROR(MOS6502 cpu, int addr) {
        int val = (addr == -1) ? cpu.getAccumulator() : cpu.readSystemBus(addr);
        boolean oldCarry = cpu.isFlagSet('C');
        cpu.forceFlag('C', (val & 0x01) != 0);
        val = ((val >> 1) | (oldCarry ? 0x80 : 0)) & 0xFF;
        writeBack(cpu, addr, val);
    }

    private void writeBack(MOS6502 cpu, int addr, int val) {
        if (addr == -1) {
            cpu.writeToBus(val, 0);
            cpu.loadAccumulatorDirect(0);
        } else {
            cpu.writeSystemBus(addr, val);
        }
        cpu.updateZAndNFlags(val);
    }
}
