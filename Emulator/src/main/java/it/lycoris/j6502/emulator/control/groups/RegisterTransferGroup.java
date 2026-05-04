package it.lycoris.j6502.emulator.control.groups;

import it.lycoris.j6502.emulator.control.InstructionGroup;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
import it.lycoris.j6502.hardware.generated.MOS6502;

import java.util.Map;

/**
 * Registers inter-register transfer instructions (TAX, TXA, TAY, TYA).
 * Bypasses memory completely to move data internally within the CPU datapath.
 */
public class RegisterTransferGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        registry.put(0xAA, new OpcodeMetadata("TAX", this::executeTax));
        registry.put(0x8A, new OpcodeMetadata("TXA", this::executeTxa));
        registry.put(0xA8, new OpcodeMetadata("TAY", this::executeTay));
        registry.put(0x98, new OpcodeMetadata("TYA", this::executeTya));
    }

    // ========================================================================
    // HARDWARE EXECUTION LOGIC
    // ========================================================================

    private void executeTax(Cpu cpu) {
        int value = cpu.getAccumulator();
        this.transferToRegister(cpu, value, "LoadX");
    }

    private void executeTxa(Cpu cpu) {
        int value = cpu.getRegisterX();
        this.transferToRegister(cpu, value, "LoadA");
    }

    private void executeTay(Cpu cpu) {
        int value = cpu.getAccumulator();
        this.transferToRegister(cpu, value, "LoadY");
    }

    private void executeTya(Cpu cpu) {
        int value = cpu.getRegisterY();
        this.transferToRegister(cpu, value, "LoadA");
    }

    /**
     * Helper to route data to a specific register via the datapath.
     */
    private void transferToRegister(Cpu cpu, int data, String loadPin) {
        MOS6502 datapath = cpu.getDatapath();
        cpu.assertDataBus(data);

        switch (loadPin) {
            case "LoadX" -> datapath.LoadX = true;
            case "LoadY" -> datapath.LoadY = true;
            case "LoadA" -> {
                datapath.LoadA = true;
                datapath.BypassALU = true;
            }
        }

        cpu.pulseClock();

        datapath.LoadX = false;
        datapath.LoadY = false;
        datapath.LoadA = false;
        datapath.BypassALU = false;
        datapath.evaluateCombinational();

        cpu.forceZeroAndNegativeFlags(data);
    }
}