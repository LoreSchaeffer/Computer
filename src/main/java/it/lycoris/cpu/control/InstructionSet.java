package it.lycoris.cpu.control;

import java.util.HashMap;
import java.util.Map;

public class InstructionSet {
    private final Map<Integer, InstructionMetadata> opcodes = new HashMap<>();

    public InstructionSet() {
        // --- LOAD Group ---
        add(0xA9, "LDA #", cpu -> {
            int val = cpu.fetchOperand();
            cpu.writeToBus(val, 0); // Source 0 (DIn)
            cpu.pulseRegister("LoadA", "OpOR");
        });

        // --- TRANSFER Group ---
        add(0xAA, "TAX", cpu -> {
            cpu.setBusSelector(1); // Source 1 (Acc)
            cpu.pulseRegister("LoadX");
        });

        add(0x8A, "TXA", cpu -> {
            cpu.setBusSelector(2); // Source 2 (X)
            cpu.pulseRegister("LoadA", "OpOR");
        });

        // --- MEMORY Group ---
        add(0x8D, "STA $abs", cpu -> {
            int addr = cpu.fetchAddress();
            int val = cpu.getAccumulator();
            cpu.getMemory().write(addr, val);
        });

        // INX - Increment Index X
        add(0xE8, "INX", cpu -> {
            cpu.indexOp("IncX");
            // Note: A real 6502 would update Z and N flags here.
            // We should call a method to sync flags if needed.
        });

        // --- DECREMENT Group ---
        add(0xCA, "DEX", cpu -> {
            // 1. Esegue il decremento hardware
            cpu.indexOp("DecX");

            // 2. Legge il nuovo valore di X
            int newX = cpu.snapshot().x();

            // 3. Calcola le flag corrette
            cpu.setPin("ManZ", newX == 0);
            cpu.setPin("ManN", (newX & 0x80) != 0);

            // 4. MUX su 11 (Ingresso Manuale)
            cpu.setPin("SelZ0", true);
            cpu.setPin("SelZ1", true);
            cpu.setPin("SelN0", true);
            cpu.setPin("SelN1", true);

            // 5. Fotografa le flag
            cpu.pulseClock();

            // 6. Reset dei MUX a 00 (Mantenimento)
            cpu.setPin("SelZ0", false);
            cpu.setPin("SelZ1", false);
            cpu.setPin("SelN0", false);
            cpu.setPin("SelN1", false);
        });

        // BNE - Branch if Not Equal (Zero flag is clear)
        add(0xD0, "BNE", cpu -> {
            int offset = (byte) cpu.fetchOperand(); // Relative offset (-128 to +127)
            if (!cpu.isFlagSet('Z')) {
                int currentPc = cpu.getAddressBus();
                cpu.jump(currentPc + offset);
                System.out.println("Branch taken to $" + Integer.toHexString(currentPc + offset));
            } else {
                System.out.println("Branch not taken");
            }
        });
    }

    private void add(int op, String name, Instruction logic) {
        opcodes.put(op, new InstructionMetadata(name, logic));
    }

    public InstructionMetadata get(int opcode) {
        return opcodes.getOrDefault(opcode, new InstructionMetadata("???", cpu -> {
        }));
    }
}
