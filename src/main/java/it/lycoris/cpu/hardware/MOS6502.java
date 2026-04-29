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

    public void executeALU(String opPin, int operandValue, boolean carryIn, boolean saveToAccumulator) {
        writeToBus(operandValue, 0); // Source 0 = DIn
        setPin("CIn", carryIn);
        setPin(opPin, true);

        // MUX to ALU output (Binary 01)
        setPin("SelC0", true);
        setPin("SelC1", false);
        setPin("SelZ0", true);
        setPin("SelZ1", false);
        setPin("SelV0", true);
        setPin("SelV1", false);
        setPin("SelN0", true);
        setPin("SelN1", false);

        if (saveToAccumulator) {
            setPin("LoadA", true);
        }

        pulseClock();

        setPin(opPin, false);
        setPin("LoadA", false);
        setPin("SelC0", false);
        setPin("SelZ0", false);
        setPin("SelV0", false);
        setPin("SelN0", false);
    }

    public void forceFlag(char flag, boolean state) {
        String manPin = "Man" + flag;
        String sel0 = "Sel" + flag + "0";
        String sel1 = "Sel" + flag + "1";

        setPin(manPin, state);
        setPin(sel0, true);
        setPin(sel1, true); // MUX 11 = Manual
        pulseClock();
        setPin(sel0, false);
        setPin(sel1, false); // MUX 00 = Hold
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

    // --- ADDRESSING MODES ---

    public int addrAbsolute() {
        return fetchAddress();
    }

    public int addrAbsoluteX() {
        int base = fetchAddress();
        return (base + snapshot().x()) & 0xFFFF;
    }

    public int addrAbsoluteY() {
        int base = fetchAddress();
        return (base + snapshot().y()) & 0xFFFF;
    }

    public int addrZeroPage() {
        return fetchOperand();
    }

    public int addrZeroPageX() {
        int zp = fetchOperand();
        return (zp + snapshot().x()) & 0xFF;
    }

    public int addrZeroPageY() {
        int zp = fetchOperand();
        return (zp + snapshot().y()) & 0xFF;
    }

    public int addrIndirect() {
        int pointer = fetchAddress();
        int lo = memory.read(pointer);
        // 6502 Hardware Bug: if pointer ends in $FF, it wraps around the same page!
        int hi = memory.read((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));
        return (hi << 8) | lo;
    }

    // --- SPECIAL ALU OPERATIONS ---

    public void bitTest(int memoryValue) {
        boolean z = (getAccumulator() & memoryValue) == 0;
        boolean n = (memoryValue & 0x80) != 0;
        boolean v = (memoryValue & 0x40) != 0;

        // Update flags manually
        setPin("ManZ", z);
        setPin("ManN", n);
        setPin("ManV", v);

        // Select Manual MUX for Z, N, V
        setPin("SelZ0", true);
        setPin("SelZ1", true);
        setPin("SelN0", true);
        setPin("SelN1", true);
        setPin("SelV0", true);
        setPin("SelV1", true);

        pulseClock();

        // Reset MUX
        setPin("SelZ0", false);
        setPin("SelZ1", false);
        setPin("SelN0", false);
        setPin("SelN1", false);
        setPin("SelV0", false);
        setPin("SelV1", false);
    }

    // --- MEMORY HELPERS ---

    public int readMemory(int address) {
        return memory.read(address);
    }

    public void writeMemory(int address, int value) {
        memory.write(address, value);
    }

    // --- STACK HELPERS ---

    public void pushStack(int value) {
        int sp = snapshot().stackPointer();
        memory.write(0x0100 | sp, value);
        indexOp("DecSP");
    }

    public int pullStack() {
        indexOp("IncSP");
        int sp = snapshot().stackPointer();
        return memory.read(0x0100 | sp);
    }

    public int getStatusRegister() {
        int p = 0x20; // Bit 5 is always strictly 1
        if (isFlagSet('C')) p |= 0x01;
        if (isFlagSet('Z')) p |= 0x02;
        if (isFlagSet('I')) p |= 0x04;
        if (isFlagSet('D')) p |= 0x08;
        if (isFlagSet('B')) p |= 0x10; // Break flag (software)
        if (isFlagSet('V')) p |= 0x40;
        if (isFlagSet('N')) p |= 0x80;
        return p;
    }

    public void setStatusRegister(int value) {
        forceFlag('C', (value & 0x01) != 0);
        forceFlag('Z', (value & 0x02) != 0);
        forceFlag('I', (value & 0x04) != 0);
        forceFlag('D', (value & 0x08) != 0);
        forceFlag('V', (value & 0x40) != 0);
        forceFlag('N', (value & 0x80) != 0);
    }

    // --- EXTRA ---

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