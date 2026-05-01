package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.control.InstructionSet;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.model.CpuState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-performance, instruction-level implementation of the MOS 6502.
 * This class bypasses logic gate simulation, executing opcodes directly via software logic.
 * It is designed for maximum throughput, utilizing primitive fields for CPU state
 * to ensure optimal JVM Just-In-Time (JIT) compilation and cache locality.
 */
public class InstructionLevelCpu implements Cpu {
    private static final Logger LOG = LoggerFactory.getLogger(InstructionLevelCpu.class);
    private final SystemBus bus;
    private final InstructionSet instructionSet = new InstructionSet();

    // ========================================================================
    // CPU REGISTERS (Using primitives for maximum performance)
    // ========================================================================
    private int programCounter; // 16-bit
    private int accumulator;    // 8-bit
    private int registerX;      // 8-bit
    private int registerY;      // 8-bit
    private int stackPointer;   // 8-bit

    // ========================================================================
    // STATUS FLAGS (Deconstructed for fast bitwise logic and branching)
    // ========================================================================
    private boolean flagC; // Carry
    private boolean flagZ; // Zero
    private boolean flagI; // Interrupt Disable
    private boolean flagD; // Decimal Mode
    private boolean flagB; // Break Command
    private boolean flagV; // Overflow
    private boolean flagN; // Negative

    /**
     * Initializes the high-speed CPU and connects it to the memory bus.
     *
     * @param bus The external system memory bus.
     */
    public InstructionLevelCpu(SystemBus bus) {
        this.bus = bus;
        this.reset();
    }

    // ========================================================================
    // LIFECYCLE & EXECUTION
    // ========================================================================

    @Override
    public void reset() {
        LOG.info("Executing High-Speed Software Reset sequence...");

        // Default hardware states on power-up
        this.accumulator = 0x00;
        this.registerX = 0x00;
        this.registerY = 0x00;
        this.stackPointer = 0xFD; // Historically, SP initializes around $FD on many systems

        // Clear all flags, but Break is conventionally 1
        this.flagC = false;
        this.flagZ = false;
        this.flagI = true;  // Interrupts disabled on reset
        this.flagD = false;
        this.flagB = true;
        this.flagV = false;
        this.flagN = false;

        // Read the 16-bit Reset Vector from $FFFC-$FFFD
        int lowerByte = this.bus.read(0xFFFC);
        int upperByte = this.bus.read(0xFFFD);
        this.programCounter = (upperByte << 8) | lowerByte;
    }

    @Override
    public void step() {
        // 1. Fetch
        int opcode = this.bus.read(this.programCounter);
        this.programCounter = (this.programCounter + 1) & 0xFFFF;

        // 2. Decode
        OpcodeMetadata metadata = this.instructionSet.get(opcode);

        // 3. Execute
        if (metadata != null && metadata.logic() != null) {
            metadata.logic().execute(this);
        } else {
            LOG.error("Execution halted. Unknown Opcode: ${}", String.format("%02X", opcode));
        }
    }

    // ========================================================================
    // HIGH-SPEED MEMORY ACCESS HELPERS (Used by Instruction Groups)
    // ========================================================================

    /**
     * Fetches the next 8-bit byte from the instruction stream and advances the PC.
     *
     * @return The 8-bit operand.
     */
    public int fetchNextByte() {
        int operand = this.bus.read(this.programCounter);
        this.programCounter = (this.programCounter + 1) & 0xFFFF;
        return operand;
    }

    /**
     * Fetches the next 16-bit address (Little-Endian) from the instruction stream and advances the PC by 2.
     *
     * @return The 16-bit address.
     */
    public int fetchNextAddress() {
        int low = this.fetchNextByte();
        int high = this.fetchNextByte();
        return (high << 8) | low;
    }

    // ========================================================================
    // MUTATORS (Exposed strictly for Instruction Group manipulation)
    // ========================================================================

    public void setAccumulator(int value) {
        this.accumulator = value & 0xFF;
    }

    public void setRegisterX(int value) {
        this.registerX = value & 0xFF;
    }

    public void setRegisterY(int value) {
        this.registerY = value & 0xFF;
    }

    public void setProgramCounter(int address) {
        this.programCounter = address & 0xFFFF;
    }

    public int getProgramCounter() {
        return this.programCounter;
    }

    public int getRegisterX() {
        return this.registerX;
    }

    public int getRegisterY() {
        return this.registerY;
    }

    public void setFlagC(boolean state) {
        this.flagC = state;
    }

    public void setFlagV(boolean state) {
        this.flagV = state;
    }

    public boolean isFlagC() {
        return this.flagC;
    }

    public void setStackPointer(int value) {
        this.stackPointer = value & 0xFF;
    }

    public void setFlagZ(boolean state) {
        this.flagZ = state;
    }

    public void setFlagN(boolean state) {
        this.flagN = state;
    }

    public void setFlagI(boolean state) {
        this.flagI = state;
    }

    public void setFlagD(boolean state) {
        this.flagD = state;
    }

    /**
     * Updates Zero and Negative flags based on the provided 8-bit result.
     * This is highly optimized and used extensively by nearly all instructions.
     *
     * @param result The 8-bit value to evaluate.
     */
    public void updateZeroAndNegativeFlags(int result) {
        int maskedResult = result & 0xFF;
        this.flagZ = (maskedResult == 0);
        this.flagN = (maskedResult & 0x80) != 0;
    }

    // ========================================================================
    // INTERFACE CONTRACT FULFILLMENT
    // ========================================================================

    @Override
    public SystemBus getBus() {
        return this.bus;
    }

    @Override
    public int readSystemBus(int address) {
        return this.bus.read(address);
    }

    @Override
    public void writeSystemBus(int address, int value) {
        this.bus.write(address, value);
    }

    @Override
    public int getAccumulator() {
        return this.accumulator;
    }

    @Override
    public int getStatusRegister() {
        int status = 0x20; // Bit 5 is strictly 1 in 6502 architecture
        if (this.flagC) status |= 0x01;
        if (this.flagZ) status |= 0x02;
        if (this.flagI) status |= 0x04;
        if (this.flagD) status |= 0x08;
        if (this.flagB) status |= 0x10;
        if (this.flagV) status |= 0x40;
        if (this.flagN) status |= 0x80;
        return status;
    }

    @Override
    public CpuState snapshot() {
        // Lookahead to fetch the mnemonic without advancing the PC
        int opcode = this.bus.read(this.programCounter);
        OpcodeMetadata metadata = this.instructionSet.get(opcode);

        return new CpuState(
                this.programCounter,
                this.accumulator,
                this.registerX,
                this.registerY,
                this.stackPointer,
                this.getStatusRegister(),
                opcode,
                metadata != null ? metadata.mnemonic() : "???"
        );
    }

    @Override
    public int readRegisterDirectly(String registerName) {
        return switch (registerName.toUpperCase()) {
            case "A", "ACCUMULATOR" -> this.accumulator;
            case "X" -> this.registerX;
            case "Y" -> this.registerY;
            case "SP" -> this.stackPointer;
            case "PC" -> this.programCounter;
            case "STATUS", "P" -> this.getStatusRegister();
            default -> throw new IllegalArgumentException("Unknown register: " + registerName);
        };
    }
}