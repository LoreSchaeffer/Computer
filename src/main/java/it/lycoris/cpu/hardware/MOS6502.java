package it.lycoris.cpu.hardware;

import it.lycoris.cpu.control.InstructionSet;
import it.lycoris.cpu.hardware.io.ComponentLibrary;
import it.lycoris.cpu.hardware.io.dto.ChipDefinition;
import it.lycoris.cpu.model.CpuState;
import it.lycoris.cpu.simulation.SimulationContext;
import it.lycoris.cpu.system.Memory;

import java.util.HashMap;
import java.util.Map;

public class MOS6502 {
    private final LogicComponent datapath;
    private final SimulationContext ctx;
    private final Memory memory;
    private final InstructionSet instructionSet = new InstructionSet();

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

    // --- Hardware Pin Interface ---

    public void setPin(String name, boolean state) {
        if (inputs.containsKey(name)) {
            inputs.get(name).setState(state, ctx);
        }
    }

    public boolean getPin(String name) {
        return outputs.getOrDefault(name, new Wire()).getState();
    }

    public void pulseClock() {
        ctx.run(); // Setup time: stabilize signals on wires
        setPin("Clk", true);
        ctx.run(); // Rising edge: data is latched
        setPin("Clk", false);
        ctx.run(); // Falling edge
    }

    // --- Bus & Data Control ---

    public void setBusSelector(int source) {
        setPin("SelBus0", (source & 1) != 0);
        setPin("SelBus1", (source & 2) != 0);
        setPin("SelBus2", (source & 4) != 0);
    }

    private void setDataBusIn(int value) {
        for (int i = 0; i < 8; i++) {
            setPin("DIn" + i, ((value >> i) & 1) == 1);
        }
    }

    public void writeToBus(int value, int source) {
        setBusSelector(source);
        if (source == 0) {
            setDataBusIn(value);
        }
        ctx.run();
    }

    // --- CPU Control Unit Interface ---

    public Memory getMemory() {
        return memory;
    }

    public int getAddressBus() {
        int address = 0;
        for (int i = 0; i < 16; i++) {
            if (getPin("AOut" + i)) address |= (1 << i);
        }
        return address;
    }

    public int getAccumulator() {
        return readRegisterDirectly("Accumulator");
    }

    public void pulseRegister(String... loadPins) {
        for (String pin : loadPins) setPin(pin, true);
        pulseClock();
        for (String pin : loadPins) setPin(pin, false);
    }

    public int fetchOperand() {
        int val = memory.read(getAddressBus());
        setPin("IncPC", true);
        pulseClock();
        setPin("IncPC", false);
        return val;
    }

    public int fetchAddress() {
        int low = fetchOperand();
        int high = fetchOperand();
        return (high << 8) | low;
    }

    public boolean isFlagSet(char flag) {
        ctx.run();
        return switch (flag) {
            case 'C' -> getPin("OutC");
            case 'Z' -> getPin("OutZ");
            case 'V' -> getPin("OutV");
            case 'N' -> getPin("OutN");
            default -> false;
        };
    }

    public void indexOp(String pin) {
        setPin(pin, true);
        pulseClock();
        setPin(pin, false);
    }

    public void jump(int targetAddress) {
        for (int i = 0; i < 16; i++) {
            setPin("AIn" + i, ((targetAddress >> i) & 1) == 1);
        }
        setPin("LoadPC", true);
        pulseClock();
        setPin("LoadPC", false);
    }

    public void updateZAndNFlags(int value) {
        setPin("ManZ", value == 0);
        setPin("ManN", (value & 0x80) != 0);

        // MUX to Manual Input (Binary 11)
        setPin("SelZ0", true);
        setPin("SelZ1", true);
        setPin("SelN0", true);
        setPin("SelN1", true);

        pulseClock(); // Latch the flags

        // MUX to Hold (Binary 00)
        setPin("SelZ0", false);
        setPin("SelZ1", false);
        setPin("SelN0", false);
        setPin("SelN1", false);
    }

    public void loadAccumulatorDirect(int source) {
        setBusSelector(source);

        setPin("BypassALU", true);  // Switch the MUX to the Internal Bus
        pulseRegister("LoadA");     // Latch the data into the Accumulator
        setPin("BypassALU", false); // Switch the MUX back to the ALU output
    }

    public void executeALU(String opPin, int operandValue, boolean carryIn) {
        // 1. Set data on the bus (Source 0 = DIn)
        writeToBus(operandValue, 0);

        // 2. Set initial Carry In in the ALU
        setPin("CIn", carryIn);

        // 3. Enable the required operation
        setPin(opPin, true);

        // 4. MUX of Status Register: Source ALU (Binary 01)
        setPin("SelC0", true);
        setPin("SelC1", false);
        setPin("SelZ0", true);
        setPin("SelZ1", false);
        setPin("SelV0", true);
        setPin("SelV1", false);
        setPin("SelN0", true);
        setPin("SelN1", false);

        // 5. Enable Accubumator writing (BypassALU is false by default, it goes through the ALU!)
        setPin("LoadA", true);

        // 6. Clock: The result is saved in A and flags in P
        pulseClock();

        // 7. Clean-up
        setPin(opPin, false);
        setPin("LoadA", false);
        setPin("SelC0", false);
        setPin("SelZ0", false);
        setPin("SelV0", false);
        setPin("SelN0", false);
    }

    public void reset() {
        System.out.println("[HARDWARE] Executing Reset Sequence...");
        setDataBusIn(0);

        int startAddress = 0x8000;
        for (int i = 0; i < 16; i++) {
            setPin("AIn" + i, ((startAddress >> i) & 1) == 1);
        }

        setPin("LoadPC", true);
        pulseClock();
        setPin("LoadPC", false);

        writeToBus(0xFF, 0);
        pulseRegister("LoadSP");
    }

    public void step() {
        // Fetch
        int currentPc = getAddressBus();
        int opcode = memory.read(currentPc);

        // Load opcode into the Instruction Register (IR)
        writeToBus(opcode, 0);
        pulseRegister("LoadIR");

        // Increment PC to point to the operand or next instruction
        setPin("IncPC", true);
        pulseClock();
        setPin("IncPC", false);

        // Decode & Execute
        instructionSet.get(opcode).logic().execute(this);
    }

    public CpuState snapshot() {
        int irValue = readRegisterDirectly("IR");
        var metadata = instructionSet.get(irValue);

        return new CpuState(
                getAddressBus(),
                getAccumulator(),
                readRegisterDirectly("X"),
                readRegisterDirectly("Y"),
                readRegisterDirectly("SP"),
                readRegisterDirectly("Status"),
                irValue,
                metadata.mnemonic()
        );
    }

    /**
     * Debug helper to read register values directly from the bus without side effects.
     */
    private int readRegisterDirectly(String name) {
        if (name.equals("IR")) {
            int val = 0;
            for (int i = 0; i < 8; i++) if (getPin("OPCode" + i)) val |= (1 << i);
            return val;
        }

        int source = switch (name) {
            case "Accumulator" -> 1;
            case "X" -> 2;
            case "Y" -> 3;
            case "SP" -> 4;
            case "Status" -> 7;
            default -> 0;
        };

        setBusSelector(source);
        ctx.run();
        int value = 0;
        for (int i = 0; i < 8; i++) {
            if (getPin("DOut" + i)) value |= (1 << i);
        }
        setBusSelector(0);
        return value;
    }
}