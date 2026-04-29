package it.lycoris.cpu.control.groups;

import it.lycoris.cpu.control.InstructionGroup;
import it.lycoris.cpu.control.OpcodeMetadata;

import java.util.Map;

public class BranchGroup implements InstructionGroup {

    @Override
    public void install(Map<Integer, OpcodeMetadata> registry) {
        // BNE - Branch if Not Equal (Zero flag is clear/0)
        registry.put(0xD0, new OpcodeMetadata("BNE", cpu -> {
            // Read the relative offset (cast to byte for sign extension: -128 to +127)
            int offset = (byte) cpu.fetchOperand();

            if (!cpu.isFlagSet('Z')) {
                int currentPc = cpu.getAddressBus();
                cpu.jump(currentPc + offset);
                System.out.println("Branch taken to $" + Integer.toHexString(currentPc + offset));
            } else {
                System.out.println("Branch not taken");
            }
        }));
    }
}
