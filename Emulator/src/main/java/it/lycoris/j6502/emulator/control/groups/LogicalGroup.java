package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;

import java.util.Map;

public class LogicalGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- AND ---
        registry.put(0x29, new OpcodeMetadata("AND #", cpu -> cpu.executeALU("OpAND", cpu.fetchOperand(), false, true)));
        registry.put(0x25, new OpcodeMetadata("AND $zp", cpu -> cpu.executeALU("OpAND", cpu.readSystemBus(cpu.addrZeroPage()), false, true)));
        registry.put(0x2D, new OpcodeMetadata("AND $abs", cpu -> cpu.executeALU("OpAND", cpu.readSystemBus(cpu.addrAbsolute()), false, true)));

        // --- ORA ---
        registry.put(0x09, new OpcodeMetadata("ORA #", cpu -> cpu.executeALU("OpOR", cpu.fetchOperand(), false, true)));
        registry.put(0x05, new OpcodeMetadata("ORA $zp", cpu -> cpu.executeALU("OpOR", cpu.readSystemBus(cpu.addrZeroPage()), false, true)));
        registry.put(0x0D, new OpcodeMetadata("ORA $abs", cpu -> cpu.executeALU("OpOR", cpu.readSystemBus(cpu.addrAbsolute()), false, true)));

        // --- EOR ---
        registry.put(0x49, new OpcodeMetadata("EOR #", cpu -> cpu.executeALU("OpXOR", cpu.fetchOperand(), false, true)));
        registry.put(0x45, new OpcodeMetadata("EOR $zp", cpu -> cpu.executeALU("OpXOR", cpu.readSystemBus(cpu.addrZeroPage()), false, true)));
        registry.put(0x4D, new OpcodeMetadata("EOR $abs", cpu -> cpu.executeALU("OpXOR", cpu.readSystemBus(cpu.addrAbsolute()), false, true)));

        // --- BIT (Bit Test) ---
        registry.put(0x24, new OpcodeMetadata("BIT $zp", cpu -> cpu.bitTest(cpu.readSystemBus(cpu.addrZeroPage()))));
        registry.put(0x2C, new OpcodeMetadata("BIT $abs", cpu -> cpu.bitTest(cpu.readSystemBus(cpu.addrAbsolute()))));
    }
}
