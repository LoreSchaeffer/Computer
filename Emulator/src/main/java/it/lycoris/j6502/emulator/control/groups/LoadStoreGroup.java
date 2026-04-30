package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.hardware.MOS6502;

import java.util.Map;

public class LoadStoreGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- LDA (Load Accumulator) ---
        registry.put(0xA9, new OpcodeMetadata("LDA #", cpu -> {
            cpu.writeToBus(cpu.fetchOperand(), 0);
            loadA(cpu);
        }));
        registry.put(0xA5, new OpcodeMetadata("LDA $zp", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrZeroPage()), 0);
            loadA(cpu);
        }));
        registry.put(0xB5, new OpcodeMetadata("LDA $zp,X", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrZeroPageX()), 0);
            loadA(cpu);
        }));
        registry.put(0xAD, new OpcodeMetadata("LDA $abs", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsolute()), 0);
            loadA(cpu);
        }));
        registry.put(0xBD, new OpcodeMetadata("LDA $abs,X", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsoluteX()), 0);
            loadA(cpu);
        }));
        registry.put(0xB9, new OpcodeMetadata("LDA $abs,Y", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsoluteY()), 0);
            loadA(cpu);
        }));

        // --- LDX (Load X Register) ---
        registry.put(0xA2, new OpcodeMetadata("LDX #", cpu -> {
            cpu.writeToBus(cpu.fetchOperand(), 0);
            loadX(cpu);
        }));
        registry.put(0xA6, new OpcodeMetadata("LDX $zp", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrZeroPage()), 0);
            loadX(cpu);
        }));
        registry.put(0xB6, new OpcodeMetadata("LDX $zp,Y", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrZeroPageY()), 0);
            loadX(cpu);
        }));
        registry.put(0xAE, new OpcodeMetadata("LDX $abs", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsolute()), 0);
            loadX(cpu);
        }));
        registry.put(0xBE, new OpcodeMetadata("LDX $abs,Y", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsoluteY()), 0);
            loadX(cpu);
        }));

        // --- LDY (Load Y Register) ---
        registry.put(0xA0, new OpcodeMetadata("LDY #", cpu -> {
            cpu.writeToBus(cpu.fetchOperand(), 0);
            loadY(cpu);
        }));
        registry.put(0xA4, new OpcodeMetadata("LDY $zp", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrZeroPage()), 0);
            loadY(cpu);
        }));
        registry.put(0xB4, new OpcodeMetadata("LDY $zp,X", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrZeroPageX()), 0);
            loadY(cpu);
        }));
        registry.put(0xAC, new OpcodeMetadata("LDY $abs", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsolute()), 0);
            loadY(cpu);
        }));
        registry.put(0xBC, new OpcodeMetadata("LDY $abs,X", cpu -> {
            cpu.writeToBus(cpu.readMemory(cpu.addrAbsoluteX()), 0);
            loadY(cpu);
        }));

        // --- STA (Store Accumulator) ---
        registry.put(0x85, new OpcodeMetadata("STA $zp", cpu -> cpu.writeMemory(cpu.addrZeroPage(), cpu.getAccumulator())));
        registry.put(0x95, new OpcodeMetadata("STA $zp,X", cpu -> cpu.writeMemory(cpu.addrZeroPageX(), cpu.getAccumulator())));
        registry.put(0x8D, new OpcodeMetadata("STA $abs", cpu -> cpu.writeMemory(cpu.addrAbsolute(), cpu.getAccumulator())));
        registry.put(0x9D, new OpcodeMetadata("STA $abs,X", cpu -> cpu.writeMemory(cpu.addrAbsoluteX(), cpu.getAccumulator())));
        registry.put(0x99, new OpcodeMetadata("STA $abs,Y", cpu -> cpu.writeMemory(cpu.addrAbsoluteY(), cpu.getAccumulator())));

        // --- STX/STY ---
        registry.put(0x86, new OpcodeMetadata("STX $zp", cpu -> cpu.writeMemory(cpu.addrZeroPage(), cpu.snapshot().x())));
        registry.put(0x8E, new OpcodeMetadata("STX $abs", cpu -> cpu.writeMemory(cpu.addrAbsolute(), cpu.snapshot().x())));
        registry.put(0x84, new OpcodeMetadata("STY $zp", cpu -> cpu.writeMemory(cpu.addrZeroPage(), cpu.snapshot().y())));
        registry.put(0x8C, new OpcodeMetadata("STY $abs", cpu -> cpu.writeMemory(cpu.addrAbsolute(), cpu.snapshot().y())));
    }

    private void loadA(MOS6502 cpu) {
        cpu.loadAccumulatorDirect(0);
        cpu.updateZAndNFlags(cpu.getAccumulator());
    }

    private void loadX(MOS6502 cpu) {
        cpu.pulseRegister("LoadX");
        cpu.updateZAndNFlags(cpu.snapshot().x());
    }

    private void loadY(MOS6502 cpu) {
        cpu.pulseRegister("LoadY");
        cpu.updateZAndNFlags(cpu.snapshot().y());
    }
}
