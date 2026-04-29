package it.lycoris.cpu.hardware;

import it.lycoris.cpu.control.InstructionSet;
import it.lycoris.cpu.hardware.io.ComponentLibrary;
import it.lycoris.cpu.hardware.io.dto.ChipDefinition;
import it.lycoris.cpu.model.CpuState;
import it.lycoris.cpu.simulation.SimulationContext;
import it.lycoris.cpu.system.Memory;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the MOS Technology 6502 Microprocessor.
 * This class acts as the bridge between the simulated hardware datapath
 * and the Java-based software Control Unit, orchestrating the execution of instructions.
 */
public class MOS6502 {
    private final LogicComponent datapath;
    private final SimulationContext ctx;
    private final Memory memory;
    private final InstructionSet instructionSet = new InstructionSet();

    private final Map<String, Wire> inputs = new HashMap<>();
    private final Map<String, Wire> outputs = new HashMap<>();

    // ========================================================================
    // 1. INITIALIZATION & LIFECYCLE
    // ========================================================================

    /**
     * Initializes the CPU, loading its physical structure from the component library.
     *
     * @param lib    The library containing the hardware blueprints.
     * @param memory The external RAM memory module.
     */
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
    }

    /**
     * Simulates the hardware reset sequence.
     * Initializes the Stack Pointer to $FF and loads the initial Program Counter
     * from the Reset Vector located at $FFFC-$FFFD.
     */
    public void reset() {
        System.out.println("[HARDWARE] Executing Reset Sequence...");

        // Initialize Stack Pointer to $FF (Top of Page 1)
        writeToBus(0xFF, 0);
        pulseRegister("LoadSP");

        // Load Reset Vector into PC
        int resetVectorLow = memory.read(0xFFFC);
        int resetVectorHigh = memory.read(0xFFFD);
        int entryPoint = (resetVectorHigh << 8) | resetVectorLow;

        jump(entryPoint);
    }

    /**
     * Executes a single CPU cycle (Instruction Fetch -> Decode -> Execute).
     */
    public void step() {
        int opcode = fetchOperand();
        var metadata = instructionSet.get(opcode);

        if (metadata != null && metadata.logic() != null) {
            metadata.logic().execute(this);
        } else {
            System.err.printf("Unhandled Opcode: $%02X%n", opcode);
        }
    }

    // ========================================================================
    // 2. HARDWARE ABSTRACTION (PINS & CLOCK)
    // ========================================================================

    /**
     * Sets the physical state of a global input pin on the CPU.
     *
     * @param pinName The name of the pin (e.g., "LoadA", "Clk").
     * @param state   The boolean state (true = HIGH, false = LOW).
     */
    public void setPin(String pinName, boolean state) {
        Wire w = inputs.get(pinName);
        if (w != null) w.setState(state, ctx);
    }

    /**
     * Reads the physical state of a global output pin from the CPU.
     */
    public boolean getPin(String pinName) {
        Wire w = outputs.get(pinName);
        return w != null && w.getState();
    }

    /**
     * Sends a single clock pulse (LOW -> HIGH -> LOW) to synchronize the hardware state.
     */
    public void pulseClock() {
        setPin("Clk", true);
        setPin("Clk", false);
    }

    /**
     * Pulses a specific register to latch data from the bus, passing through an ALU operation.
     */
    public void pulseRegister(String loadPin, String operationPin) {
        setPin(operationPin, true);
        setPin(loadPin, true);
        pulseClock();
        setPin(loadPin, false);
        setPin(operationPin, false);
    }

    /**
     * Pulses a specific register to latch data directly from the bus (ALU bypassed or irrelevant).
     */
    public void pulseRegister(String loadPin) {
        setPin(loadPin, true);
        pulseClock();
        setPin(loadPin, false);
    }

    /**
     * Activates an index operation (e.g., increment/decrement) and pulses the clock.
     */
    public void indexOp(String operationPin) {
        setPin(operationPin, true);
        pulseClock();
        setPin(operationPin, false);
    }

    /**
     * Get the memory
     */
    public Memory getMemory() {
        return this.memory;
    }

    // ========================================================================
    // 3. BUS & MEMORY OPERATIONS
    // ========================================================================

    /**
     * Configures the multiplexer to select the source for the internal bus.
     */
    public void setBusSelector(int sourceId) {
        setPin("SelBus0", (sourceId & 1) != 0);
        setPin("SelBus1", (sourceId & 2) != 0);
        setPin("SelBus2", (sourceId & 4) != 0);
    }

    /**
     * Writes an 8-bit value to the internal data bus.
     */
    public void writeToBus(int value, int source) {
        setBusSelector(source);
        for (int i = 0; i < 8; i++) {
            setPin("DIn" + i, (value & (1 << i)) != 0);
        }
    }

    /**
     * Reads the 16-bit address currently asserted on the Address Bus.
     */
    public int getAddressBus() {
        int address = 0;
        for (int i = 0; i < 16; i++) {
            if (getPin("AIn" + i)) address |= (1 << i);
        }
        return address;
    }

    public int readMemory(int address) {
        return memory.read(address);
    }

    public void writeMemory(int address, int value) {
        memory.write(address, value);
    }

    // ========================================================================
    // 4. INSTRUCTION FETCH & ADDRESSING MODES
    // ========================================================================

    /**
     * Fetches the next 8-bit operand from memory and increments the Program Counter.
     */
    public int fetchOperand() {
        int pc = getAddressBus();
        int val = memory.read(pc);
        pulseRegister("IncPC");
        return val;
    }

    /**
     * Fetches a 16-bit address (Little-Endian) from memory and advances the PC by 2.
     */
    public int fetchAddress() {
        int lo = fetchOperand();
        int hi = fetchOperand();
        return (hi << 8) | lo;
    }

    /**
     * Absolute Addressing ($abs): Returns a 16-bit address read from the PC.
     */
    public int addrAbsolute() {
        return fetchAddress();
    }

    /**
     * Absolute Indexed X ($abs,X): Base 16-bit address + X Register.
     */
    public int addrAbsoluteX() {
        return (fetchAddress() + snapshot().x()) & 0xFFFF;
    }

    /**
     * Zero Page Indexed Y ($zp,Y): 8-bit address + Y (wraps around in Page 0).
     */
    public int addrZeroPageY() {
        int zp = fetchOperand();
        return (zp + snapshot().y()) & 0xFF;
    }

    /**
     * Indirect Addressing ($abs): Used exclusively by JMP ($abs).
     * IMPORTANT: Accurately reproduces the infamous 6502 hardware bug where
     * reading a pointer at the end of a page (e.g., $02FF) wraps around
     * to the start of the same page ($0200) instead of crossing the boundary.
     */
    public int addrIndirect() {
        int pointer = fetchAddress();
        int lo = memory.read(pointer);
        int hi = memory.read((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));
        return (hi << 8) | lo;
    }

    /**
     * Zero Page Addressing ($zp): Returns an 8-bit address in Page 0.
     */
    public int addrZeroPage() {
        return fetchOperand();
    }

    /**
     * Zero Page Indexed X ($zp,X): Base 8-bit address + X Register (wraps in Page 0).
     */
    public int addrZeroPageX() {
        return (fetchOperand() + snapshot().x()) & 0xFF;
    }

    /**
     * Absolute Indexed Y ($abs,Y): Base 16-bit address + Y Register.
     */
    public int addrAbsoluteY() {
        return (fetchAddress() + snapshot().y()) & 0xFFFF;
    }

    // ========================================================================
    // 5. EXECUTION, ALU & STATUS FLAGS
    // ========================================================================

    /**
     * Forces the Program Counter to a specific 16-bit address.
     */
    public void jump(int address) {
        writeToBus(address & 0xFF, 0); // Low byte
        pulseRegister("LoadIR");       // Temporarily use IR as a buffer

        writeToBus((address >> 8) & 0xFF, 0); // High byte
        pulseRegister("LoadPC");       // Loads full 16-bit value into PC
    }

    /**
     * Loads raw data into the Accumulator, physically bypassing the ALU.
     *
     * @param source The internal bus source ID (0: DIn, 1: A, 2: X, 3: Y, etc.)
     */
    public void loadAccumulatorDirect(int source) {
        setBusSelector(source);
        setPin("BypassALU", true);  // Switch the MUX to the Internal Bus
        pulseRegister("LoadA");     // Latch the data into the Accumulator
        setPin("BypassALU", false); // Switch the MUX back to the ALU output
    }

    /**
     * Executes an ALU operation, saving the result and updating physical status flags.
     *
     * @param opPin             The ALU operation pin to activate (e.g., "OpADD").
     * @param operandValue      The value to put on the bus (ALU B input).
     * @param carryIn           The value for the ALU CIn pin.
     * @param saveToAccumulator If true, routes the result to A. If false, only updates flags (used for CMP).
     */
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

        pulseClock(); // Execute and latch flags

        // Cleanup
        setPin(opPin, false);
        setPin("LoadA", false);
        setPin("SelC0", false);
        setPin("SelZ0", false);
        setPin("SelV0", false);
        setPin("SelN0", false);
    }

    /**
     * Executes the BIT instruction logic.
     * N = memory bit 7, V = memory bit 6, Z = (A AND memory) == 0.
     */
    public void bitTest(int memoryValue) {
        boolean z = (getAccumulator() & memoryValue) == 0;
        boolean n = (memoryValue & 0x80) != 0;
        boolean v = (memoryValue & 0x40) != 0;

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

    /**
     * Updates the Zero (Z) and Negative (N) flags in the Status Register.
     * Uses the hardware manual input paths to bypass the ALU.
     */
    public void updateZAndNFlags(int value) {
        setPin("ManZ", value == 0);
        setPin("ManN", (value & 0x80) != 0);

        // MUX to Manual Input (Binary 11)
        setPin("SelZ0", true);
        setPin("SelZ1", true);
        setPin("SelN0", true);
        setPin("SelN1", true);

        pulseClock();

        // MUX to Hold (Binary 00)
        setPin("SelZ0", false);
        setPin("SelZ1", false);
        setPin("SelN0", false);
        setPin("SelN1", false);
    }

    /**
     * Manually forces a specific physical flag to a new state.
     */
    public void forceFlag(char flag, boolean state) {
        String manPin = "Man" + flag;
        String sel0 = "Sel" + flag + "0";
        String sel1 = "Sel" + flag + "1";

        setPin(manPin, state);

        // MUX 11 = Manual
        setPin(sel0, true);
        setPin(sel1, true);
        pulseClock();

        // MUX 00 = Hold
        setPin(sel0, false);
        setPin(sel1, false);
    }

    public boolean isFlagSet(char flag) {
        return getPin("Flag" + flag);
    }

    // ========================================================================
    // 6. STACK OPERATIONS
    // ========================================================================

    /**
     * Pushes an 8-bit value onto the hardware stack (Page 1) and decrements SP.
     */
    public void pushStack(int value) {
        int sp = snapshot().stackPointer();
        memory.write(0x0100 | sp, value);
        indexOp("DecSP");
    }

    /**
     * Increments SP and pulls an 8-bit value from the hardware stack.
     */
    public int pullStack() {
        indexOp("IncSP");
        int sp = snapshot().stackPointer();
        return memory.read(0x0100 | sp);
    }

    /**
     * Retrieves the Status Register as an 8-bit integer.
     */
    public int getStatusRegister() {
        int p = 0x20; // Bit 5 is always strictly 1
        if (isFlagSet('C')) p |= 0x01;
        if (isFlagSet('Z')) p |= 0x02;
        if (isFlagSet('I')) p |= 0x04;
        if (isFlagSet('D')) p |= 0x08;
        if (isFlagSet('B')) p |= 0x10;
        if (isFlagSet('V')) p |= 0x40;
        if (isFlagSet('N')) p |= 0x80;
        return p;
    }

    /**
     * Restores the Status Register from an 8-bit integer.
     */
    public void setStatusRegister(int value) {
        forceFlag('C', (value & 0x01) != 0);
        forceFlag('Z', (value & 0x02) != 0);
        forceFlag('I', (value & 0x04) != 0);
        forceFlag('D', (value & 0x08) != 0);
        forceFlag('V', (value & 0x40) != 0);
        forceFlag('N', (value & 0x80) != 0);
    }

    // ========================================================================
    // 7. DEBUGGING & STATE INSPECTION
    // ========================================================================

    public int getAccumulator() {
        int val = 0;
        for (int i = 0; i < 8; i++) {
            if (getPin("A" + i)) val |= (1 << i);
        }
        return val;
    }

    /**
     * Creates a snapshot of the current CPU registers and flags for monitoring.
     */
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
     * Internal helper to read register values directly from the bus without side effects.
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
            default -> throw new IllegalArgumentException("Unknown register: " + name);
        };

        // Setup MUX and read from Output Bus logic (Mocked for abstraction)
        return 0;
    }
}