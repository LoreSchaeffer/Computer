package it.lycoris.cpu.hardware;

import it.lycoris.cpu.hardware.io.ComponentLibrary;
import it.lycoris.cpu.hardware.io.dto.ChipDefinition;
import it.lycoris.cpu.simulation.SimulationContext;
import it.lycoris.cpu.system.Memory;

import java.util.HashMap;
import java.util.Map;

public class MOS6502 {
    private final LogicComponent datapath;
    private final SimulationContext ctx;
    private final Memory memory;

    private final Map<String, Wire> inputs = new HashMap<>();
    private final Map<String, Wire> outputs = new HashMap<>();

    public MOS6502(ComponentLibrary lib, Memory memory) {
        this.memory = memory;
        this.ctx = new SimulationContext();

        ChipDefinition def = lib.getDefinition("MOS6502");
        def.pins().inputs().forEach(name -> inputs.put(name, new Wire()));
        def.pins().outputs().forEach(name -> outputs.put(name, new Wire()));

        this.datapath = lib.build("MOS6502", "CPU", inputs, outputs);

        if (this.datapath instanceof ComplexChip cc) {
            cc.powerOnReset(ctx);
        }

        ctx.run();
    }

    public boolean getPin(String name) {
        return outputs.get(name).getState();
    }

    public void setPin(String name, boolean state) {
        inputs.get(name).setState(state, ctx);
    }

    public void pulseClock() {
        ctx.run();
        setPin("Clk", true);
        ctx.run();
        setPin("Clk", false);
        ctx.run();
    }

    private void setBusSelector(int source) {
        setPin("SelBus0", (source & 1) != 0);
        setPin("SelBus1", (source & 2) != 0);
        setPin("SelBus2", (source & 4) != 0);
    }

    private void setDataBusIn(int value) {
        for (int i = 0; i < 8; i++) {
            setPin("DIn" + i, ((value >> i) & 1) == 1);
        }
    }

    public int getAddressBus() {
        int address = 0;
        for (int i = 0; i < 16; i++) {
            if (getPin("AOut" + i)) address |= (1 << i);
        }
        return address;
    }

    public int getAccumulator() {
        setBusSelector(1);
        ctx.run();

        int value = 0;
        for (int i = 0; i < 8; i++) {
            if (getPin("DOut" + i)) value |= (1 << i);
        }

        setBusSelector(0);
        return value;
    }

    public void reset() {
        System.out.println("[HARDWARE] Running Reset Sequence...");
        setDataBusIn(0);

        for (int i = 0; i < 16; i++) {
            setPin("AIn" + i, ((0x8000 >> i) & 1) == 1);
        }

        setPin("LoadPC", true);
        pulseClock();
        setPin("LoadPC", false);
    }

    public void step() {
        // Fetch
        int pcAddress = getAddressBus();
        int opcode = memory.read(pcAddress);

        setDataBusIn(opcode);
        setBusSelector(0);

        setPin("LoadIR", true);
        pulseClock();
        setPin("LoadIR", false);

        // Increment Program Counter to the next byte
        setPin("IncPC", true);
        pulseClock();
        setPin("IncPC", false);

        // Execute
        execute(opcode);
    }

    private void execute(int opcode) {
        switch (opcode) {
            case 0xA9 -> ldaImmediate();
            default -> System.out.printf("Opcode sconosciuto: $%02X%n", opcode);
        }
    }

    private void ldaImmediate() {
        int value = memory.read(getAddressBus());
        setDataBusIn(value);
        setBusSelector(0);

        // Load inside Accumulator through ALU (OR with 0)
        setPin("OpOR", true);
        setPin("LoadA", true);
        pulseClock();
        setPin("LoadA", false);
        setPin("OpOR", false);

        // Increment PC
        setPin("IncPC", true);
        pulseClock();
        setPin("IncPC", false);

        System.out.println("Eseguito LDA #" + String.format("$%02X", value));
    }

    public void printState() {
        System.out.printf("PC: $%04X | Accumulator: $%02X%n", getAddressBus(), getAccumulator());
    }
}
