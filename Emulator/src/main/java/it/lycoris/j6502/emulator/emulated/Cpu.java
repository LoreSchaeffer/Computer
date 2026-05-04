package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.control.InstructionSet;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.hardware.generated.MOS6502;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central processing unit wrapper.
 * Acts as the hardware Control Unit (microcode sequencer), driving the
 * auto-generated gate-level MOS6502 datapath by asserting control pins.
 */
public class Cpu {
    private static final Logger LOG = LoggerFactory.getLogger(Cpu.class);

    private final SystemBus bus;
    private final MOS6502 datapath;
    private final InstructionSet instructionSet;
    private long totalClockCycles;

    /**
     * Initializes the CPU, connects it to the motherboard bus, and instantiates
     * the gate-level datapath.
     *
     * @param bus            The system bus connecting RAM, ROM, and peripherals.
     * @param instructionSet The registry containing microcode logic for all opcodes.
     */
    public Cpu(SystemBus bus, InstructionSet instructionSet) {
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
        // --- FETCH PHASE ---
        int currentPc = this.readAddressOutPins();
        int opcode = this.bus.read(currentPc);

        this.setDataInPins(opcode);
        this.datapath.LoadIR = true;
        this.datapath.IncPC = true;

        this.pulseClock();

        this.datapath.LoadIR = false;
        this.datapath.IncPC = false;

        // --- DECODE & EXECUTE PHASE ---
        int latchedOpcode = this.readOpcodePins();
        OpcodeMetadata metadata = this.instructionSet.get(latchedOpcode);

        // Execute the hardware-level microcode mapped to this opcode
        metadata.logic().execute(this);
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

    public int getStatusRegister() {
        int status = 0x20;
        if (this.isFlagSet('C')) status |= 0x01;
        if (this.isFlagSet('Z')) status |= 0x02;
        if (this.isFlagSet('I')) status |= 0x04;
        if (this.isFlagSet('D')) status |= 0x08;
        if (this.isFlagSet('V')) status |= 0x40;
        if (this.isFlagSet('N')) status |= 0x80;
        return status;
    }

    public boolean isFlagSet(char flag) {
        return switch (flag) {
            case 'C' -> this.datapath.OutC;
            case 'Z' -> this.datapath.OutZ;
            case 'V' -> this.datapath.OutV;
            case 'N' -> this.datapath.OutN;
            default -> false;
        };
    }

    public void forceFlag(char flag, boolean state) {
        switch (flag) {
            case 'C' -> {
                this.datapath.ManC = state;
                this.datapath.SelC0 = false;
                this.datapath.SelC1 = true;
            }
            case 'Z' -> {
                this.datapath.ManZ = state;
                this.datapath.SelZ0 = false;
                this.datapath.SelZ1 = true;
            }
            case 'I' -> {
                this.datapath.ManI = state;
                this.datapath.SelI0 = false;
                this.datapath.SelI1 = true;
            }
            case 'V' -> {
                this.datapath.ManV = state;
                this.datapath.SelV0 = false;
                this.datapath.SelV1 = true;
            }
            case 'N' -> {
                this.datapath.ManN = state;
                this.datapath.SelN0 = false;
                this.datapath.SelN1 = true;
            }
            case 'D' -> {
                this.datapath.ManD = state;
                this.datapath.SelD0 = false;
                this.datapath.SelD1 = true;
            }
            case 'B' -> {
                this.datapath.ManB = state;
                this.datapath.SelB0 = false;
                this.datapath.SelB1 = true;
            }
            default -> {
                return;
            }
        }

        this.pulseClock();
        this.setAllControlPinsIdle();
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