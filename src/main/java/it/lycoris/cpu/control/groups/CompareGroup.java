package it.lycoris.cpu.control.groups;

import it.lycoris.cpu.control.InstructionGroup;
import it.lycoris.cpu.control.OpcodeMetadata;
import it.lycoris.cpu.hardware.MOS6502;

import java.util.Map;

public class CompareGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // --- CMP (Compare A) ---
        registry.put(0xC9, new OpcodeMetadata("CMP #", cpu -> cmp(cpu, cpu.getAccumulator(), cpu.fetchOperand())));
        registry.put(0xC5, new OpcodeMetadata("CMP $zp", cpu -> cmp(cpu, cpu.getAccumulator(), cpu.readMemory(cpu.addrZeroPage()))));
        registry.put(0xCD, new OpcodeMetadata("CMP $abs", cpu -> cmp(cpu, cpu.getAccumulator(), cpu.readMemory(cpu.addrAbsolute()))));

        // --- CPX (Compare X) ---
        registry.put(0xE0, new OpcodeMetadata("CPX #", cpu -> cmp(cpu, cpu.snapshot().x(), cpu.fetchOperand())));
        registry.put(0xE4, new OpcodeMetadata("CPX $zp", cpu -> cmp(cpu, cpu.snapshot().x(), cpu.readMemory(cpu.addrZeroPage()))));
        registry.put(0xEC, new OpcodeMetadata("CPX $abs", cpu -> cmp(cpu, cpu.snapshot().x(), cpu.readMemory(cpu.addrAbsolute()))));

        // --- CPY (Compare Y) ---
        registry.put(0xC0, new OpcodeMetadata("CPY #", cpu -> cmp(cpu, cpu.snapshot().y(), cpu.fetchOperand())));
        registry.put(0xC4, new OpcodeMetadata("CPY $zp", cpu -> cmp(cpu, cpu.snapshot().y(), cpu.readMemory(cpu.addrZeroPage()))));
        registry.put(0xCC, new OpcodeMetadata("CPY $abs", cpu -> cmp(cpu, cpu.snapshot().y(), cpu.readMemory(cpu.addrAbsolute()))));
    }

    private void cmp(MOS6502 cpu, int reg, int val) {
        // Compare is essentially (Reg - Val) but result is not saved.
        // Carry flag is set if Reg >= Val (unsigned).
        cpu.forceFlag('C', reg >= (val & 0xFF));
        cpu.updateZAndNFlags((reg - val) & 0xFF);
    }
}
