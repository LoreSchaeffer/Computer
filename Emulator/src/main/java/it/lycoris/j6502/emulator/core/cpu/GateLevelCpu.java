package it.lycoris.j6502.emulator.core.cpu;

import it.lycoris.j6502.emulator.core.InstructionSet;
import it.lycoris.j6502.emulator.core.SystemBus;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import it.lycoris.j6502.hardware.generated.MOS6502;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central processing unit wrapper.
 * Acts as the hardware Control Unit (microcode sequencer), driving the
 * auto-generated gate-level MOS6502 datapath by asserting control pins.
 */
public class GateLevelCpu implements Cpu {
    private static final Logger LOG = LoggerFactory.getLogger(GateLevelCpu.class);

    private final SystemBus bus;
    private final MOS6502 datapath;
    private final InstructionSet instructionSet;
    private long totalClockCycles;
    private int statusRegister = 0x20;

    private boolean nmiLineActive = false;
    private boolean irqLineActive = false;
    private int suspendedCycles = 0;

    /**
     * Initializes the CPU, connects it to the motherboard bus, and instantiates
     * the gate-level datapath.
     *
     * @param bus            The system bus connecting RAM, ROM, and peripherals.
     * @param instructionSet The registry containing microcode logic for all opcodes.
     */
    public GateLevelCpu(SystemBus bus, InstructionSet instructionSet) {
        this.bus = bus;
        this.instructionSet = instructionSet;
        this.datapath = new MOS6502();
        this.totalClockCycles = 0L;
        this.setAllControlPinsIdle();
    }

    public MOS6502 getDatapath() {
        return this.datapath;
    }

    public void reset() {
        LOG.info("Initiating hardware reset sequence...");
        this.setAllControlPinsIdle();

        for (int i = 0; i < 5; i++) {
            this.pulseClock();
        }

        int resetVectorLow = this.bus.read(0xFFFC);
        this.pulseClock();

        int resetVectorHigh = this.bus.read(0xFFFD);
        this.pulseClock();

        int startAddress = (resetVectorHigh << 8) | resetVectorLow;
        this.jump(startAddress);

        LOG.info("CPU Reset complete. Execution begins at: ${}", String.format("%04X", startAddress));
    }

    public void jump(int address) {
        this.setAddressInPins(address);
        this.datapath.LoadPC = true;
        this.pulseClock();
        this.datapath.LoadPC = false;
    }

    /**
     * Executes a single instruction. This encompasses the Fetch, Decode,
     * and Execute phases managed via microcode signals.
     */
    public void step() {
        // --- 0. SUSPENDED STATE CHECK (DMA HALT) ---
        if (this.suspendedCycles > 0) {
            this.suspendedCycles--;
            this.pulseClock();
            return;
        }

        // --- 1. HARDWARE INTERRUPT POLLING PHASE ---
        if (this.nmiLineActive) {
            this.nmiLineActive = false;
            this.serviceHardwareInterrupt(0xFFFA);
            return; // Interrupt serviced, skip normal instruction fetch
        }

        if (this.irqLineActive && !this.isFlagSet('I')) {
            this.irqLineActive = false;
            this.serviceHardwareInterrupt(0xFFFE);
            return; // Interrupt serviced, skip normal instruction fetch
        }

        // --- 2. NORMAL FETCH PHASE ---
        int currentProgramCounter = this.readAddressOutPins();
        int opcode = this.bus.read(currentProgramCounter);

        this.setDataInPins(opcode);
        this.datapath.LoadIR = true;
        this.datapath.IncPC = true;

        this.pulseClock();

        this.datapath.LoadIR = false;
        this.datapath.IncPC = false;

        // --- 3. DECODE & EXECUTE PHASE ---
        int latchedOpcode = this.readOpcodePins();
        OpcodeMetadata metadata = this.instructionSet.get(latchedOpcode);

        metadata.logic().execute(this);
    }

    /**
     * Suspends the CPU execution for a specific number of clock cycles.
     * Used by external devices like the DMA controller (Direct Memory Access)
     * to halt the CPU while transferring data across the system bus.
     *
     * @param cycles The number of clock cycles to suspend execution.
     */
    public void suspendCycles(int cycles) {
        this.suspendedCycles += cycles;
    }

    public int fetchNextByte() {
        int currentPc = this.readAddressOutPins();
        int data = this.bus.read(currentPc);

        this.datapath.IncPC = true;
        this.pulseClock();
        this.datapath.IncPC = false;

        return data;
    }

    public int fetchNextAddress() {
        int lowByte = this.fetchNextByte();
        int highByte = this.fetchNextByte();
        return (highByte << 8) | lowByte;
    }

    public int readSystemBus(int address) {
        return this.bus.read(address);
    }

    public void writeSystemBus(int address, int data) {
        this.bus.write(address, data);
    }

    private int readInternalBus(int sourceSelect) {
        this.datapath.SelBus0 = (sourceSelect & 1) != 0;
        this.datapath.SelBus1 = (sourceSelect & 2) != 0;
        this.datapath.SelBus2 = (sourceSelect & 4) != 0;
        this.datapath.evaluateCombinational();

        int value = this.decodeDataOutBus();

        this.datapath.SelBus0 = false;
        this.datapath.SelBus1 = false;
        this.datapath.SelBus2 = false;
        this.datapath.evaluateCombinational();

        return value;
    }

    public int getAccumulator() {
        return this.readInternalBus(1);
    }

    public int getRegisterX() {
        return this.readInternalBus(2);
    }

    public int getRegisterY() {
        return this.readInternalBus(3);
    }

    public int getStackPointer() {
        return this.readInternalBus(4);
    }

    public int getProgramCounter() {
        return this.readAddressOutPins();
    }

    /**
     * Pushes an 8-bit value onto the hardware stack and decrements the Stack Pointer.
     *
     * @param value The 8-bit value to push.
     */
    public void pushStack(int value) {
        this.writeSystemBus(0x0100 | this.getStackPointer(), value);
        this.datapath.DecSP = true;
        this.pulseClock();
        this.datapath.DecSP = false;
        this.datapath.evaluateCombinational();
    }

    /**
     * Increments the Stack Pointer and pulls an 8-bit value from the hardware stack.
     *
     * @return The 8-bit value pulled from the stack.
     */
    public int pullStack() {
        this.datapath.IncSP = true;
        this.pulseClock();
        this.datapath.IncSP = false;
        this.datapath.evaluateCombinational();
        return this.readSystemBus(0x0100 | this.getStackPointer());
    }

    public int getStatusRegister() {
        return this.statusRegister;
    }

    public void setStatusRegister(int status) {
        this.statusRegister = status | 0x20;
    }

    public boolean isFlagSet(char flag) {
        int mask = this.getFlagMask(flag);
        return (this.statusRegister & mask) != 0;
    }

    public void forceFlag(char flag, boolean state) {
        int mask = this.getFlagMask(flag);
        if (state) {
            this.statusRegister |= mask;
        } else {
            this.statusRegister &= ~mask;
        }
    }

    private int getFlagMask(char flag) {
        return switch (flag) {
            case 'C' -> 0x01;
            case 'Z' -> 0x02;
            case 'I' -> 0x04;
            case 'D' -> 0x08;
            case 'B' -> 0x10;
            case 'V' -> 0x40;
            case 'N' -> 0x80;
            default -> 0x00;
        };
    }

    public void forceZeroAndNegativeFlags(int value) {
        this.forceFlag('Z', value == 0);
        this.forceFlag('N', (value & 0x80) != 0);
    }

    public void pulseClock() {
        this.datapath.evaluateCombinational();
        this.datapath.Clk = true;
        this.datapath.tickClock();
        this.datapath.Clk = false;
        this.datapath.tickClock();
        this.totalClockCycles++;
    }

    public void assertDataBus(int data) {
        this.datapath.DIn0 = (data & (1 << 0)) != 0;
        this.datapath.DIn1 = (data & (1 << 1)) != 0;
        this.datapath.DIn2 = (data & (1 << 2)) != 0;
        this.datapath.DIn3 = (data & (1 << 3)) != 0;
        this.datapath.DIn4 = (data & (1 << 4)) != 0;
        this.datapath.DIn5 = (data & (1 << 5)) != 0;
        this.datapath.DIn6 = (data & (1 << 6)) != 0;
        this.datapath.DIn7 = (data & (1 << 7)) != 0;
        this.datapath.evaluateCombinational();
    }

    private void setAddressInPins(int address) {
        this.datapath.AIn0 = (address & (1 << 0)) != 0;
        this.datapath.AIn1 = (address & (1 << 1)) != 0;
        this.datapath.AIn2 = (address & (1 << 2)) != 0;
        this.datapath.AIn3 = (address & (1 << 3)) != 0;
        this.datapath.AIn4 = (address & (1 << 4)) != 0;
        this.datapath.AIn5 = (address & (1 << 5)) != 0;
        this.datapath.AIn6 = (address & (1 << 6)) != 0;
        this.datapath.AIn7 = (address & (1 << 7)) != 0;
        this.datapath.AIn8 = (address & (1 << 8)) != 0;
        this.datapath.AIn9 = (address & (1 << 9)) != 0;
        this.datapath.AIn10 = (address & (1 << 10)) != 0;
        this.datapath.AIn11 = (address & (1 << 11)) != 0;
        this.datapath.AIn12 = (address & (1 << 12)) != 0;
        this.datapath.AIn13 = (address & (1 << 13)) != 0;
        this.datapath.AIn14 = (address & (1 << 14)) != 0;
        this.datapath.AIn15 = (address & (1 << 15)) != 0;
        this.datapath.evaluateCombinational();
    }

    /**
     * Asserts an 8-bit value onto the CPU's Data Input pins (DIn0-7).
     * This simulates the electrical state of the data bus during a read operation.
     *
     * @param data The 8-bit unsigned integer to assert on the pins.
     */
    public void setDataInPins(int data) {
        this.datapath.DIn0 = (data & (1 << 0)) != 0;
        this.datapath.DIn1 = (data & (1 << 1)) != 0;
        this.datapath.DIn2 = (data & (1 << 2)) != 0;
        this.datapath.DIn3 = (data & (1 << 3)) != 0;
        this.datapath.DIn4 = (data & (1 << 4)) != 0;
        this.datapath.DIn5 = (data & (1 << 5)) != 0;
        this.datapath.DIn6 = (data & (1 << 6)) != 0;
        this.datapath.DIn7 = (data & (1 << 7)) != 0;

        this.datapath.evaluateCombinational();
    }

    public int readAddressOutPins() {
        int address = 0;
        if (this.datapath.AOut0) address |= (1 << 0);
        if (this.datapath.AOut1) address |= (1 << 1);
        if (this.datapath.AOut2) address |= (1 << 2);
        if (this.datapath.AOut3) address |= (1 << 3);
        if (this.datapath.AOut4) address |= (1 << 4);
        if (this.datapath.AOut5) address |= (1 << 5);
        if (this.datapath.AOut6) address |= (1 << 6);
        if (this.datapath.AOut7) address |= (1 << 7);
        if (this.datapath.AOut8) address |= (1 << 8);
        if (this.datapath.AOut9) address |= (1 << 9);
        if (this.datapath.AOut10) address |= (1 << 10);
        if (this.datapath.AOut11) address |= (1 << 11);
        if (this.datapath.AOut12) address |= (1 << 12);
        if (this.datapath.AOut13) address |= (1 << 13);
        if (this.datapath.AOut14) address |= (1 << 14);
        if (this.datapath.AOut15) address |= (1 << 15);
        return address;
    }

    private int readOpcodePins() {
        int opcode = 0;
        if (this.datapath.OPCode0) opcode |= (1 << 0);
        if (this.datapath.OPCode1) opcode |= (1 << 1);
        if (this.datapath.OPCode2) opcode |= (1 << 2);
        if (this.datapath.OPCode3) opcode |= (1 << 3);
        if (this.datapath.OPCode4) opcode |= (1 << 4);
        if (this.datapath.OPCode5) opcode |= (1 << 5);
        if (this.datapath.OPCode6) opcode |= (1 << 6);
        if (this.datapath.OPCode7) opcode |= (1 << 7);
        return opcode;
    }

    private int decodeDataOutBus() {
        int data = 0;
        if (this.datapath.DOut0) data |= (1 << 0);
        if (this.datapath.DOut1) data |= (1 << 1);
        if (this.datapath.DOut2) data |= (1 << 2);
        if (this.datapath.DOut3) data |= (1 << 3);
        if (this.datapath.DOut4) data |= (1 << 4);
        if (this.datapath.DOut5) data |= (1 << 5);
        if (this.datapath.DOut6) data |= (1 << 6);
        if (this.datapath.DOut7) data |= (1 << 7);
        return data;
    }

    private void setAllControlPinsIdle() {
        this.datapath.LoadA = false;
        this.datapath.LoadX = false;
        this.datapath.LoadY = false;
        this.datapath.LoadSP = false;
        this.datapath.LoadIR = false;
        this.datapath.LoadPC = false;
        this.datapath.IncPC = false;
        this.datapath.IncX = false;
        this.datapath.DecX = false;
        this.datapath.IncY = false;
        this.datapath.DecY = false;
        this.datapath.IncSP = false;
        this.datapath.DecSP = false;
        this.datapath.OpADD = false;
        this.datapath.OpAND = false;
        this.datapath.OpOR = false;
        this.datapath.OpXOR = false;
        this.datapath.BypassALU = false;
        this.datapath.SetI = false;
        this.datapath.SetD = false;
        this.datapath.SetB = false;
        this.datapath.SelBus0 = false;
        this.datapath.SelBus1 = false;
        this.datapath.SelBus2 = false;
        this.datapath.SelC0 = false;
        this.datapath.SelC1 = false;
        this.datapath.SelZ0 = false;
        this.datapath.SelZ1 = false;
        this.datapath.SelI0 = false;
        this.datapath.SelI1 = false;
        this.datapath.SelV0 = false;
        this.datapath.SelV1 = false;
        this.datapath.SelN0 = false;
        this.datapath.SelN1 = false;
        this.datapath.SelD0 = false;
        this.datapath.SelD1 = false;
        this.datapath.SelB0 = false;
        this.datapath.SelB1 = false;

        this.datapath.evaluateCombinational();
    }

    /**
     * Triggers a Non-Maskable Interrupt (NMI).
     * Usually asserted by the Picture Processing Unit (PPU) during the Vertical Blanking interval.
     */
    public void triggerNmi() {
        this.nmiLineActive = true;
    }

    /**
     * Asserts the Interrupt Request (IRQ) line.
     * Will be serviced by the CPU only if the Interrupt Disable (I) flag is clear.
     */
    public void triggerIrq() {
        this.irqLineActive = true;
    }

    /**
     * Simulates the exact 6502 hardware interrupt sequence.
     * Pushes the Program Counter and Status Register to the stack, sets the Interrupt Disable flag,
     * and jumps to the specified vector address.
     *
     * @param vectorAddress The memory address containing the interrupt handler pointer (0xFFFA or 0xFFFE).
     */
    private void serviceHardwareInterrupt(int vectorAddress) {
        int currentProgramCounter = this.getProgramCounter();

        // Push PC High Byte and Low Byte
        this.pushStack((currentProgramCounter >> 8) & 0xFF);
        this.pushStack(currentProgramCounter & 0xFF);

        // Push Status Register
        // Hardware interrupts push the status register with the B-flag (bit 4) cleared to 0.
        // Bit 5 remains strictly 1.
        int statusToPush = (this.getStatusRegister() & 0xEF) | 0x20;
        this.pushStack(statusToPush);

        // Set the Interrupt Disable flag to prevent nested IRQs
        this.forceFlag('I', true);

        // Fetch the new Program Counter from the vector
        int lowByte = this.readSystemBus(vectorAddress);
        int highByte = this.readSystemBus(vectorAddress + 1);
        int interruptHandlerAddress = (highByte << 8) | lowByte;

        this.jump(interruptHandlerAddress);

        // Hardware interrupts consume 7 clock cycles
        this.pulseClock();
        this.pulseClock();
    }

    public long getTotalClockCycles() {
        return this.totalClockCycles;
    }

    public CpuState snapshot() {
        return new CpuState(
                this.getProgramCounter(),
                this.getAccumulator(),
                this.getRegisterX(),
                this.getRegisterY(),
                this.getStackPointer(),
                this.getStatusRegister(),
                this.getTotalClockCycles()
        );
    }
}