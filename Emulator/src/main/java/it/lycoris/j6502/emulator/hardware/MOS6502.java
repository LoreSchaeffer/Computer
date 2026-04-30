package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.control.InstructionSet;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.EmulationContext;
import it.lycoris.j6502.emulator.emulated.SystemBus;
import it.lycoris.j6502.emulator.hardware.io.ComponentLibrary;
import it.lycoris.j6502.emulator.hardware.io.dto.ChipDefinition;
import it.lycoris.j6502.emulator.model.CpuState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the simulated MOS Technology 6502 Microprocessor.
 * This class serves as the bridge between the physical simulated hardware (datapath)
 * and the software-based control unit (InstructionSet).
 */
public class MOS6502 {
    private static final Logger LOG = LoggerFactory.getLogger(MOS6502.class);
    private final LogicComponent datapath;
    private final EmulationContext ctx;
    private final SystemBus bus;
    private final InstructionSet instructionSet = new InstructionSet();

    private final Map<String, Wire> inputs = new HashMap<>();
    private final Map<String, Wire> outputs = new HashMap<>();

    // ========================================================================
    // INITIALIZATION & LIFECYCLE
    // ========================================================================

    /**
     * Initializes the CPU by loading its physical structure from the component library
     * and linking it to the provided external ram.
     *
     * @param bus The System Bus connected to the CPU.
     */
    public MOS6502(SystemBus bus) {
        this.bus = bus;
        this.ctx = new EmulationContext();

        ComponentLibrary lib = ComponentLibrary.get();

        ChipDefinition def = lib.getDefinition("MOS6502");
        def.pins().inputs().forEach(name -> inputs.put(name, new Wire()));
        def.pins().outputs().forEach(name -> outputs.put(name, new Wire()));

        this.datapath = lib.build("MOS6502", "CPU", inputs, outputs);

        if (this.datapath instanceof ComplexChip cc) {
            cc.powerOnReset(ctx);
        }

        // Run the initial simulation pass to propagate default states through the chip
        ctx.run();
    }

    /**
     * Simulates the hardware reset sequence of the 6502.
     * It sets the Stack Pointer to $FF and loads the Program Counter
     * from the Reset Vector located at $FFFC-$FFFD.
     */
    public void reset() {
        LOG.info("Executing Reset sequence...");
        setDataBusIn(0);

        // Initialize Stack Pointer to top of Page 1 ($01FF)
        writeToBus(0xFF, 0);
        pulseRegister("LoadSP");

        // Read the Reset Vector from top of memory
        int lo = bus.read(0xFFFC);
        int hi = bus.read(0xFFFD);
        int entryPoint = (hi << 8) | lo;

        // Force the Program Counter to the entry point
        jump(entryPoint);
    }

    /**
     * Executes a single CPU cycle: Fetches the next opcode, updates the PC,
     * decodes the instruction, and executes its associated micro-operations.
     */
    public void step() {
        // Fetch the opcode at the current PC
        int currentPc = getAddressBus();
        int opcode = bus.read(currentPc);

        // Load the fetched opcode into the hardware Instruction Register (IR)
        writeToBus(opcode, 0);
        pulseRegister("LoadIR");

        // Increment PC to point to the operand or the next instruction
        setPin("IncPC", true);
        pulseClock();
        setPin("IncPC", false);

        // Decode & Execute using the software Instruction Set
        var metadata = instructionSet.get(opcode);
        if (metadata != null && metadata.logic() != null) {
            metadata.logic().execute(this);
        } else {
            LOG.error("Unhandled or illegal Opcode detected: ${}", String.format("%02X", opcode));
        }
    }

    // ========================================================================
    // HARDWARE PIN & CLOCK CONTROL
    // ========================================================================

    /**
     * Sets the physical state of a global input pin on the CPU.
     *
     * @param name  The name of the pin (e.g., "LoadA", "Clk").
     * @param state The boolean state to apply (true = HIGH, false = LOW).
     */
    public void setPin(String name, boolean state) {
        if (inputs.containsKey(name)) {
            inputs.get(name).setState(state, ctx);
        }
    }

    /**
     * Reads the physical state of a global output pin from the CPU.
     *
     * @param name The name of the pin to read.
     * @return The current boolean state of the pin.
     */
    public boolean getPin(String name) {
        return outputs.getOrDefault(name, new Wire()).getState();
    }

    /**
     * Simulates a full clock pulse (LOW -> HIGH -> LOW) to synchronize hardware state.
     * The SimulationContext is executed at each edge to accurately propagate signals.
     */
    public void pulseClock() {
        ctx.run(); // Setup time: stabilize incoming signals on wires before the clock rises
        setPin("Clk", true);
        ctx.run(); // Rising edge: registers latch the data
        setPin("Clk", false);
        ctx.run(); // Falling edge: complete the cycle
    }

    /**
     * Asserts multiple load pins, pulses the clock, and de-asserts them.
     * Used to latch data into one or more registers simultaneously.
     *
     * @param loadPins An array of pin names to assert (e.g., "LoadA", "LoadX").
     */
    public void pulseRegister(String... loadPins) {
        for (String pin : loadPins) setPin(pin, true);
        pulseClock();
        for (String pin : loadPins) setPin(pin, false);
    }

    /**
     * Activates an index operation pin (e.g., increment/decrement) and pulses the clock.
     *
     * @param pin The operation pin to assert (e.g., "IncX", "DecSP").
     */
    public void indexOp(String pin) {
        setPin(pin, true);
        pulseClock();
        setPin(pin, false);
    }

    // ========================================================================
    // INTERNAL BUS CONTROL
    // ========================================================================

    /**
     * Configures the internal 8-bit Multiplexer (InternalBus8Bit) to select a specific source.
     *
     * @param source The integer ID of the source (0: DIn, 1: A, 2: X, 3: Y, 4: SP).
     */
    public void setBusSelector(int source) {
        setPin("SelBus0", (source & 1) != 0);
        setPin("SelBus1", (source & 2) != 0);
        setPin("SelBus2", (source & 4) != 0);
    }

    /**
     * Internally forces an 8-bit value onto the DIn pins.
     *
     * @param value The 8-bit value to assert.
     */
    private void setDataBusIn(int value) {
        for (int i = 0; i < 8; i++) {
            setPin("DIn" + i, ((value >> i) & 1) == 1);
        }
    }

    /**
     * Writes an 8-bit value to the internal data bus by setting the input pins
     * and configuring the bus multiplexer.
     *
     * @param value  The 8-bit value to write.
     * @param source The internal bus source ID (usually 0 for DIn).
     */
    public void writeToBus(int value, int source) {
        setBusSelector(source);
        if (source == 0) {
            setDataBusIn(value);
        }
        ctx.run(); // Propagate the bus selection immediately
    }

    // ========================================================================
    // MEMORY & EXTERNAL BUS ACCESS
    // ========================================================================

    /**
     * Gets the System Bus attached to the CPU.
     *
     * @return The SystemBus instance.
     */
    public SystemBus getBus() {
        return bus;
    }

    /**
     * Reads a byte from the external System Bus at the specified address.
     *
     * @param address The 16-bit address to read from.
     * @return The 8-bit value read from System Bus.
     */
    public int readSystemBus(int address) {
        return bus.read(address);
    }

    /**
     * Writes a byte to the external System Bus at the specified address.
     *
     * @param address The 16-bit address to write to.
     * @param value   The 8-bit value to write.
     */
    public void writeSystemBus(int address, int value) {
        bus.write(address, value);
    }

    /**
     * Reads the current 16-bit address physically asserted on the Address Out pins.
     *
     * @return The 16-bit address from AOut0-AOut15.
     */
    public int getAddressBus() {
        int address = 0;
        for (int i = 0; i < 16; i++) {
            if (getPin("AOut" + i)) address |= (1 << i);
        }
        return address;
    }

    // ========================================================================
    // INSTRUCTION FETCH & DECODE
    // ========================================================================

    /**
     * Fetches the next 8-bit operand from memory and physically increments the Program Counter.
     *
     * @return The fetched 8-bit operand.
     */
    public int fetchOperand() {
        int val = bus.read(getAddressBus());
        setPin("IncPC", true);
        pulseClock();
        setPin("IncPC", false);
        return val;
    }

    /**
     * Fetches a 16-bit address (Little-Endian) from memory and advances the PC by 2.
     *
     * @return The fetched 16-bit address.
     */
    public int fetchAddress() {
        int low = fetchOperand();
        int high = fetchOperand();
        return (high << 8) | low;
    }

    // ========================================================================
    // ADDRESSING MODES
    // ========================================================================

    /**
     * Absolute Addressing.
     *
     * @return A full 16-bit address fetched from the instruction stream.
     */
    public int addrAbsolute() {
        return fetchAddress();
    }

    /**
     * Absolute Indexed X Addressing.
     *
     * @return A 16-bit address equal to the absolute address plus the X register.
     */
    public int addrAbsoluteX() {
        int base = fetchAddress();
        return (base + readRegisterDirectly("X")) & 0xFFFF;
    }

    /**
     * Absolute Indexed Y Addressing.
     *
     * @return A 16-bit address equal to the absolute address plus the Y register.
     */
    public int addrAbsoluteY() {
        int base = fetchAddress();
        return (base + readRegisterDirectly("Y")) & 0xFFFF;
    }

    /**
     * Zero Page Addressing.
     *
     * @return An 8-bit address mapped strictly to Page 0 ($0000 - $00FF).
     */
    public int addrZeroPage() {
        return fetchOperand();
    }

    /**
     * Zero Page Indexed X Addressing.
     * Wraps around within Page 0 if the addition exceeds $FF.
     *
     * @return An 8-bit Zero Page address.
     */
    public int addrZeroPageX() {
        int zp = fetchOperand();
        return (zp + readRegisterDirectly("X")) & 0xFF;
    }

    /**
     * Zero Page Indexed Y Addressing.
     * Wraps around within Page 0 if the addition exceeds $FF.
     *
     * @return An 8-bit Zero Page address.
     */
    public int addrZeroPageY() {
        int zp = fetchOperand();
        return (zp + readRegisterDirectly("Y")) & 0xFF;
    }

    /**
     * Indirect Addressing.
     * Accurately reproduces the historical 6502 hardware bug where page boundaries are not crossed.
     *
     * @return The 16-bit target address read from the pointer.
     */
    public int addrIndirect() {
        int pointer = fetchAddress();
        int lo = bus.read(pointer);

        // 6502 Hardware Bug: if pointer ends in $FF, it wraps around the same page
        // instead of crossing into the next page.
        int hi = bus.read((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));

        return (hi << 8) | lo;
    }

    /**
     * Indexed Indirect X Addressing: ($zp,X)
     * Adds X to the operand to find a Zero Page address, then reads a 16-bit pointer from there.
     *
     * @return The resolved 16-bit memory address.
     */
    public int addrIndexedIndirectX() {
        int zpAddress = (this.fetchOperand() + this.readRegisterDirectly("X")) & 0xFF;
        int lowByte = this.readSystemBus(zpAddress);
        int highByte = this.readSystemBus((zpAddress + 1) & 0xFF);
        return (highByte << 8) | lowByte;
    }

    /**
     * Indirect Indexed Y Addressing: ($zp),Y
     * Reads a 16-bit pointer from the Zero Page, then adds Y to it.
     *
     * @return The resolved 16-bit memory address.
     */
    public int addrIndirectIndexedY() {
        int zpAddress = this.fetchOperand();
        int lowByte = this.readSystemBus(zpAddress);
        int highByte = this.readSystemBus((zpAddress + 1) & 0xFF);
        int baseAddress = (highByte << 8) | lowByte;
        return (baseAddress + this.readRegisterDirectly("Y")) & 0xFFFF;
    }

    // ========================================================================
    // EXECUTION & ALU CONTROL
    // ========================================================================

    /**
     * Forces the hardware Program Counter to jump to a specific 16-bit address.
     *
     * @param targetAddress The destination 16-bit address.
     */
    public void jump(int targetAddress) {
        // Assert the target address onto the PC input pins
        for (int i = 0; i < 16; i++) {
            setPin("AIn" + i, ((targetAddress >> i) & 1) == 1);
        }
        setPin("LoadPC", true);
        pulseClock();
        setPin("LoadPC", false);
    }

    /**
     * Bypasses the ALU to load a value directly from the internal bus into the Accumulator.
     *
     * @param source The internal bus source ID to load from.
     */
    public void loadAccumulatorDirect(int source) {
        setBusSelector(source);

        setPin("BypassALU", true);  // Switch the MUX to directly route the Internal Bus
        pulseRegister("LoadA");     // Latch the data into the Accumulator
        setPin("BypassALU", false); // Restore the MUX to output ALU results
    }

    /**
     * Orchestrates the physical Arithmetic Logic Unit (ALU) to perform an operation.
     * Manages the multiplexers to route the result and update flags.
     *
     * @param opPin             The specific ALU operation pin to assert (e.g., "OpADD", "OpAND").
     * @param operandValue      The 8-bit value to supply to the ALU (usually from DIn).
     * @param carryIn           The Carry-In value for the operation.
     * @param saveToAccumulator True if the result should be latched back into A, False for comparison only.
     */
    public void executeALU(String opPin, int operandValue, boolean carryIn, boolean saveToAccumulator) {
        writeToBus(operandValue, 0); // Route operand from DIn (Source 0)
        setPin("CIn", carryIn);
        setPin(opPin, true);

        // Configure the Status MUX to read physical flags from the ALU output (Binary 01)
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

        pulseClock(); // Execute the operation and latch registers/flags

        // Cleanup asserted pins and restore MUX to Hold state
        setPin(opPin, false);
        setPin("LoadA", false);
        setPin("SelC0", false);
        setPin("SelZ0", false);
        setPin("SelV0", false);
        setPin("SelN0", false);
    }

    /**
     * Executes the BIT instruction logic.
     * Evaluates Zero flag based on an AND operation, while directly copying N and V from memory.
     *
     * @param memoryValue The 8-bit operand fetched from memory.
     */
    public void bitTest(int memoryValue) {
        boolean z = (getAccumulator() & memoryValue) == 0;
        boolean n = (memoryValue & 0x80) != 0;
        boolean v = (memoryValue & 0x40) != 0;

        // Apply calculated flags to the manual input pins
        setPin("ManZ", z);
        setPin("ManN", n);
        setPin("ManV", v);

        // Switch the specific Flag MUXes to Manual Input mode (Binary 11)
        setPin("SelZ0", true);
        setPin("SelZ1", true);
        setPin("SelN0", true);
        setPin("SelN1", true);
        setPin("SelV0", true);
        setPin("SelV1", true);

        pulseClock(); // Latch the manual flags

        // Restore MUX to Hold mode
        setPin("SelZ0", false);
        setPin("SelZ1", false);
        setPin("SelN0", false);
        setPin("SelN1", false);
        setPin("SelV0", false);
        setPin("SelV1", false);
    }

    // ========================================================================
    // STATUS FLAGS CONTROL
    // ========================================================================

    /**
     * Checks if a specific flag is currently set in the hardware Status Register.
     *
     * @param flag The character representing the flag ('C', 'Z', 'V', 'N').
     * @return True if the flag is 1, false otherwise.
     */
    public boolean isFlagSet(char flag) {
        ctx.run(); // Ensure all signals are stabilized before reading
        return switch (flag) {
            case 'C' -> getPin("OutC");
            case 'Z' -> getPin("OutZ");
            case 'V' -> getPin("OutV");
            case 'N' -> getPin("OutN");
            default -> false; // I, D, and B flags are handled via software logic
        };
    }

    /**
     * Updates the Zero (Z) and Negative (N) flags based on the provided 8-bit value.
     * Used mainly during register transfers and increments/decrements.
     *
     * @param value The value to evaluate.
     */
    public void updateZAndNFlags(int value) {
        setPin("ManZ", value == 0);
        setPin("ManN", (value & 0x80) != 0);

        // MUX to Manual Input mode for Z and N (Binary 11)
        setPin("SelZ0", true);
        setPin("SelZ1", true);
        setPin("SelN0", true);
        setPin("SelN1", true);

        pulseClock(); // Latch the flags

        // Restore MUX to Hold mode
        setPin("SelZ0", false);
        setPin("SelZ1", false);
        setPin("SelN0", false);
        setPin("SelN1", false);
    }

    /**
     * Forcibly sets or clears a single flag in the Status Register using manual input lines.
     *
     * @param flag  The character representing the flag.
     * @param state The boolean state to apply.
     */
    public void forceFlag(char flag, boolean state) {
        String manPin = "Man" + flag;
        String sel0 = "Sel" + flag + "0";
        String sel1 = "Sel" + flag + "1";

        setPin(manPin, state);

        // Switch MUX to Manual mode (11)
        setPin(sel0, true);
        setPin(sel1, true);

        pulseClock();

        // Restore MUX to Hold mode (00)
        setPin(sel0, false);
        setPin(sel1, false);
    }

    /**
     * Constructs an 8-bit representation of the Status Register.
     * Note: Bit 5 is always strictly 1 by hardware design.
     *
     * @return The 8-bit Processor Status (P).
     */
    public int getStatusRegister() {
        int p = 0x20; // Bit 5 is historically always 1
        if (isFlagSet('C')) p |= 0x01;
        if (isFlagSet('Z')) p |= 0x02;
        if (isFlagSet('I')) p |= 0x04;
        if (isFlagSet('D')) p |= 0x08;
        if (isFlagSet('B')) p |= 0x10; // Break flag
        if (isFlagSet('V')) p |= 0x40;
        if (isFlagSet('N')) p |= 0x80;
        return p;
    }

    /**
     * Restores the Status Register states from an 8-bit integer.
     * Used primarily by RTI (Return from Interrupt).
     *
     * @param value The 8-bit Processor Status to restore.
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
    // STACK OPERATIONS
    // ========================================================================

    /**
     * Pushes an 8-bit value onto the hardware stack (Page 1) and decrements the Stack Pointer.
     *
     * @param value The value to push.
     */
    public void pushStack(int value) {
        int sp = readRegisterDirectly("SP");
        bus.write(0x0100 | sp, value);
        indexOp("DecSP");
    }

    /**
     * Increments the Stack Pointer and pulls an 8-bit value from the hardware stack (Page 1).
     *
     * @return The pulled 8-bit value.
     */
    public int pullStack() {
        indexOp("IncSP");
        int sp = readRegisterDirectly("SP");
        return bus.read(0x0100 | sp);
    }

    // ========================================================================
    // DEBUGGING & HARDWARE INSPECTION
    // ========================================================================

    /**
     * Convenience method to read the current value of the Accumulator.
     *
     * @return The 8-bit value inside the Accumulator.
     */
    public int getAccumulator() {
        return readRegisterDirectly("Accumulator");
    }

    /**
     * Generates a read-only snapshot of the CPU's current internal state.
     * Useful for debugging, system monitoring, and trace logging.
     *
     * @return A CpuState record containing all primary registers and flags.
     */
    public CpuState snapshot() {
        int irValue = readRegisterDirectly("IR");
        OpcodeMetadata metadata = instructionSet.get(irValue);

        return new CpuState(
                getAddressBus(),
                getAccumulator(),
                readRegisterDirectly("X"),
                readRegisterDirectly("Y"),
                readRegisterDirectly("SP"),
                readRegisterDirectly("Status"),
                irValue,
                metadata != null ? metadata.mnemonic() : "???"
        );
    }

    /**
     * A diagnostic helper that reads register values directly from the internal bus
     * without causing side effects like clock pulses.
     *
     * @param name The name of the register to inspect (e.g., "Accumulator", "X", "IR").
     * @return The 8-bit value currently held in the requested register.
     */
    public int readRegisterDirectly(String name) {
        if (name.equals("IR")) {
            int val = 0;
            for (int i = 0; i < 8; i++) {
                if (getPin("OPCode" + i)) val |= (1 << i);
            }
            return val;
        }

        // Map register name to Multiplexer source ID
        int source = switch (name) {
            case "Accumulator" -> 1;
            case "X" -> 2;
            case "Y" -> 3;
            case "SP" -> 4;
            case "Status" -> 7;
            default -> 0;
        };

        // Temporarily configure the MUX to output the selected register
        setBusSelector(source);
        ctx.run(); // Propagate the signals without pulsing the clock

        // Read the result from the DOut pins
        int value = 0;
        for (int i = 0; i < 8; i++) {
            if (getPin("DOut" + i)) value |= (1 << i);
        }

        // Restore the MUX to its default state
        setBusSelector(0);
        return value;
    }
}