//package it.lycoris.j6502.emulator.hardware;
//
//import it.lycoris.j6502.emulator.control.InstructionSet;
//import it.lycoris.j6502.emulator.control.OpcodeMetadata;
//import it.lycoris.j6502.emulator.emulated.SystemBus;
//import it.lycoris.j6502.emulator.model.CpuState;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * High-Performance Instruction-Level simulation of the MOS Technology 6502.
// * This class acts as a Facade: it completely replaces the slow gate-level datapath
// * with primitive variables (L1 Cache optimized), while perfectly maintaining the
// * public API contract required by the InstructionGroup modules.
// */
//public class MOS6502Performance {
//    private static final Logger LOG = LoggerFactory.getLogger(MOS6502Performance.class);
//
//    private final SystemBus bus;
//    private final InstructionSet instructionSet;
//
//    // ========================================================================
//    // CPU INTERNAL REGISTERS (Primitive State for maximum performance)
//    // ========================================================================
//    private int accumulator = 0x00;
//    private int xRegister = 0x00;
//    private int yRegister = 0x00;
//    private int programCounter = 0x0000;
//    private int stackPointer = 0xFF;
//
//    // Status Flags representation: N V - B D I Z C
//    // Bit 5 is always strictly 1 by hardware design.
//    private int statusRegister = 0x24;
//
//    // Internal execution state
//    private int internalDataBus = 0x00;
//    private int instructionRegister = 0x00;
//
//    /**
//     * Initializes the High-Performance CPU and links it to the external System Bus.
//     *
//     * @param bus The System Bus connected to the CPU.
//     */
//    public MOS6502Performance(SystemBus bus) {
//        this.bus = bus;
//        this.instructionSet = new InstructionSet();
//    }
//
//    // ========================================================================
//    // INITIALIZATION & LIFECYCLE
//    // ========================================================================
//
//    /**
//     * Simulates the hardware reset sequence.
//     */
//    public void reset() {
//        LOG.info("Executing fast hardware Reset sequence...");
//        this.stackPointer = 0xFF;
//
//        int lo = this.bus.read(0xFFFC);
//        int hi = this.bus.read(0xFFFD);
//        this.programCounter = (hi << 8) | lo;
//    }
//
//    /**
//     * Executes a single CPU instruction sequence directly.
//     */
//    public void step() {
//        // Fetch the opcode and increment PC
//        this.instructionRegister = this.bus.read(this.programCounter);
//        this.programCounter = (this.programCounter + 1) & 0xFFFF;
//
//        OpcodeMetadata metadata = this.instructionSet.get(this.instructionRegister);
//
//        if (metadata != null && metadata.logic() != null) {
//            metadata.logic().execute(this);
//        } else {
//            LOG.error("Unhandled or illegal Opcode detected: ${}", String.format("%02X", this.instructionRegister));
//        }
//    }
//
//    // ========================================================================
//    // INTERNAL BUS CONTROL (Adapter for Gate-Level API)
//    // ========================================================================
//
//    /**
//     * Emulates the internal multiplexer by routing a register to the internal data bus.
//     *
//     * @param source The integer ID of the source (0: DIn, 1: A, 2: X, 3: Y, 4: SP, 7: Status).
//     */
//    public void setBusSelector(int source) {
//        switch (source) {
//            case 1 -> this.internalDataBus = this.accumulator;
//            case 2 -> this.internalDataBus = this.xRegister;
//            case 3 -> this.internalDataBus = this.yRegister;
//            case 4 -> this.internalDataBus = this.stackPointer;
//            case 7 -> this.internalDataBus = this.getStatusRegister();
//            // Case 0 (DIn) maintains the current internalDataBus state set by writeToBus
//        }
//    }
//
//    /**
//     * Writes an 8-bit value to the internal data bus.
//     *
//     * @param value  The 8-bit value to write.
//     * @param source The internal bus source ID.
//     */
//    public void writeToBus(int value, int source) {
//        if (source == 0) {
//            this.internalDataBus = value & 0xFF;
//        } else {
//            this.setBusSelector(source);
//        }
//    }
//
//    /**
//     * Latch data from the internal data bus into specific registers.
//     *
//     * @param loadPins An array of pin names to assert.
//     */
//    public void pulseRegister(String... loadPins) {
//        for (String pin : loadPins) {
//            switch (pin) {
//                case "LoadA" -> this.accumulator = this.internalDataBus;
//                case "LoadX" -> this.xRegister = this.internalDataBus;
//                case "LoadY" -> this.yRegister = this.internalDataBus;
//                case "LoadSP" -> this.stackPointer = this.internalDataBus;
//                // LoadIR and LoadPC are handled natively by step() and jump()
//            }
//        }
//    }
//
//    /**
//     * Executes arithmetic index operations directly.
//     *
//     * @param pin The operation pin to assert.
//     */
//    public void indexOp(String pin) {
//        switch (pin) {
//            case "IncX" -> this.xRegister = (this.xRegister + 1) & 0xFF;
//            case "DecX" -> this.xRegister = (this.xRegister - 1) & 0xFF;
//            case "IncY" -> this.yRegister = (this.yRegister + 1) & 0xFF;
//            case "DecY" -> this.yRegister = (this.yRegister - 1) & 0xFF;
//            case "IncSP" -> this.stackPointer = (this.stackPointer + 1) & 0xFF;
//            case "DecSP" -> this.stackPointer = (this.stackPointer - 1) & 0xFF;
//        }
//    }
//
//    // ========================================================================
//    // EXTERNAL BUS ACCESS
//    // ========================================================================
//
//    public SystemBus getBus() {
//        return this.bus;
//    }
//
//    public int readSystemBus(int address) {
//        return this.bus.read(address);
//    }
//
//    public void writeSystemBus(int address, int value) {
//        this.bus.write(address, value);
//    }
//
//    public int getAddressBus() {
//        return this.programCounter;
//    }
//
//    // ========================================================================
//    // INSTRUCTION FETCH
//    // ========================================================================
//
//    public int fetchOperand() {
//        int val = this.bus.read(this.programCounter);
//        this.programCounter = (this.programCounter + 1) & 0xFFFF;
//        return val;
//    }
//
//    public int fetchAddress() {
//        int low = this.fetchOperand();
//        int high = this.fetchOperand();
//        return (high << 8) | low;
//    }
//
//    // ========================================================================
//    // ADDRESSING MODES
//    // ========================================================================
//
//    public int addrAbsolute() {
//        return this.fetchAddress();
//    }
//
//    public int addrAbsoluteX() {
//        return (this.fetchAddress() + this.xRegister) & 0xFFFF;
//    }
//
//    public int addrAbsoluteY() {
//        return (this.fetchAddress() + this.yRegister) & 0xFFFF;
//    }
//
//    public int addrZeroPage() {
//        return this.fetchOperand();
//    }
//
//    public int addrZeroPageX() {
//        return (this.fetchOperand() + this.xRegister) & 0xFF;
//    }
//
//    public int addrZeroPageY() {
//        return (this.fetchOperand() + this.yRegister) & 0xFF;
//    }
//
//    public int addrIndirect() {
//        int pointer = this.fetchAddress();
//        int lo = this.bus.read(pointer);
//        int hi = this.bus.read((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));
//        return (hi << 8) | lo;
//    }
//
//    public int addrIndexedIndirectX() {
//        int zpAddress = (this.fetchOperand() + this.xRegister) & 0xFF;
//        int lo = this.bus.read(zpAddress);
//        int hi = this.bus.read((zpAddress + 1) & 0xFF);
//        return (hi << 8) | lo;
//    }
//
//    public int addrIndirectIndexedY() {
//        int zpAddress = this.fetchOperand();
//        int lo = this.bus.read(zpAddress);
//        int hi = this.bus.read((zpAddress + 1) & 0xFF);
//        int baseAddress = (hi << 8) | lo;
//        return (baseAddress + this.yRegister) & 0xFFFF;
//    }
//
//    // ========================================================================
//    // EXECUTION & ALU CONTROL
//    // ========================================================================
//
//    public void jump(int targetAddress) {
//        this.programCounter = targetAddress & 0xFFFF;
//    }
//
//    public void loadAccumulatorDirect(int source) {
//        this.setBusSelector(source);
//        this.accumulator = this.internalDataBus;
//    }
//
//    public void executeALU(String opPin, int operandValue, boolean carryIn, boolean saveToAccumulator) {
//        int result = 0;
//        int a = this.accumulator;
//        int m = operandValue & 0xFF;
//
//        switch (opPin) {
//            case "OpADD": // Handles ADC and SBC (SBC passes ~operand)
//                result = a + m + (carryIn ? 1 : 0);
//                this.forceFlag('C', result > 0xFF);
//                this.forceFlag('V', ((a ^ result) & (m ^ result) & 0x80) != 0);
//                break;
//            case "OpAND":
//                result = a & m;
//                break;
//            case "OpOR":
//                result = a | m;
//                break;
//            case "OpXOR":
//                result = a ^ m;
//                break;
//        }
//
//        result &= 0xFF;
//
//        if (saveToAccumulator) {
//            this.accumulator = result;
//        }
//
//        this.updateZAndNFlags(result);
//    }
//
//    public void bitTest(int memoryValue) {
//        int masked = memoryValue & 0xFF;
//        this.forceFlag('Z', (this.accumulator & masked) == 0);
//        this.forceFlag('N', (masked & 0x80) != 0);
//        this.forceFlag('V', (masked & 0x40) != 0);
//    }
//
//    // ========================================================================
//    // STATUS FLAGS CONTROL
//    // ========================================================================
//
//    public boolean isFlagSet(char flag) {
//        return switch (flag) {
//            case 'C' -> (this.statusRegister & 0x01) != 0;
//            case 'Z' -> (this.statusRegister & 0x02) != 0;
//            case 'I' -> (this.statusRegister & 0x04) != 0;
//            case 'D' -> (this.statusRegister & 0x08) != 0;
//            case 'B' -> (this.statusRegister & 0x10) != 0;
//            case 'V' -> (this.statusRegister & 0x40) != 0;
//            case 'N' -> (this.statusRegister & 0x80) != 0;
//            default -> false;
//        };
//    }
//
//    public void updateZAndNFlags(int value) {
//        int maskedValue = value & 0xFF;
//        this.forceFlag('Z', maskedValue == 0);
//        this.forceFlag('N', (maskedValue & 0x80) != 0);
//    }
//
//    public void forceFlag(char flag, boolean state) {
//        int mask = switch (flag) {
//            case 'C' -> 0x01;
//            case 'Z' -> 0x02;
//            case 'I' -> 0x04;
//            case 'D' -> 0x08;
//            case 'B' -> 0x10;
//            case 'V' -> 0x40;
//            case 'N' -> 0x80;
//            default -> 0x00;
//        };
//
//        if (state) {
//            this.statusRegister |= mask;
//        } else {
//            this.statusRegister &= ~mask;
//        }
//    }
//
//    public int getStatusRegister() {
//        return this.statusRegister | 0x20; // Bit 5 is strictly 1
//    }
//
//    public void setStatusRegister(int value) {
//        this.statusRegister = value | 0x20;
//    }
//
//    // ========================================================================
//    // STACK OPERATIONS
//    // ========================================================================
//
//    public void pushStack(int value) {
//        this.bus.write(0x0100 | this.stackPointer, value & 0xFF);
//        this.stackPointer = (this.stackPointer - 1) & 0xFF;
//    }
//
//    public int pullStack() {
//        this.stackPointer = (this.stackPointer + 1) & 0xFF;
//        return this.bus.read(0x0100 | this.stackPointer);
//    }
//
//    // ========================================================================
//    // DEBUGGING & HARDWARE INSPECTION
//    // ========================================================================
//
//    public int getAccumulator() {
//        return this.accumulator;
//    }
//
//    public int readRegisterDirectly(String name) {
//        return switch (name) {
//            case "Accumulator" -> this.accumulator;
//            case "X" -> this.xRegister;
//            case "Y" -> this.yRegister;
//            case "SP" -> this.stackPointer;
//            case "Status" -> this.getStatusRegister();
//            case "IR" -> this.instructionRegister;
//            default -> 0;
//        };
//    }
//
//    public CpuState snapshot() {
//        OpcodeMetadata metadata = this.instructionSet.get(this.instructionRegister);
//
//        return new CpuState(
//                this.programCounter,
//                this.accumulator,
//                this.xRegister,
//                this.yRegister,
//                this.stackPointer,
//                this.getStatusRegister(),
//                this.instructionRegister,
//                metadata != null ? metadata.mnemonic() : "???"
//        );
//    }
//}