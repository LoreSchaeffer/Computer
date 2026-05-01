package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.control.InstructionSet;
import it.lycoris.j6502.emulator.control.OpcodeMetadata;
import it.lycoris.j6502.emulator.emulated.Cpu;
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
 * Gate-level implementation of the MOS 6502 using physical simulated wiring and components.
 * Extremely accurate but computationally heavy.
 */
public class GateLevelCpu implements Cpu {
    private static final Logger LOG = LoggerFactory.getLogger(GateLevelCpu.class);
    private final LogicComponent datapath;
    private final EmulationContext ctx;
    private final SystemBus bus;
    private final InstructionSet instructionSet = new InstructionSet();

    private final Map<String, Wire> inputs = new HashMap<>();
    private final Map<String, Wire> outputs = new HashMap<>();

    // ========================================================================
    // INITIALIZATION & LIFECYCLE
    // ========================================================================

    public GateLevelCpu(SystemBus bus) {
        this.bus = bus;
        this.ctx = new EmulationContext();

        ComponentLibrary lib = ComponentLibrary.get();

        ChipDefinition def = lib.getDefinition("MOS6502");
        def.pins().inputs().forEach(name -> this.inputs.put(name, new Wire()));
        def.pins().outputs().forEach(name -> this.outputs.put(name, new Wire()));

        this.datapath = lib.build("MOS6502", "CPU", this.inputs, this.outputs);

        if (this.datapath instanceof ComplexChip cc) {
            cc.powerOnReset(this.ctx);
        }

        // Run the initial simulation pass to propagate default states through the chip
        this.ctx.run();
    }

    @Override
    public void reset() {
        LOG.info("Executing Hardware Reset sequence...");
        this.setDataBusIn(0);

        // Initialize Stack Pointer to top of Page 1 ($01FF)
        this.writeToBus(0xFF, 0);
        this.pulseRegister("LoadSP");

        // Read the Reset Vector from top of memory
        int lo = this.bus.read(0xFFFC);
        int hi = this.bus.read(0xFFFD);
        int entryPoint = (hi << 8) | lo;

        // Force the Program Counter to the entry point
        this.jump(entryPoint);
    }

    @Override
    public void step() {
        // Fetch the opcode at the current PC
        int currentPc = this.getAddressBus();
        int opcode = this.bus.read(currentPc);

        // Load the fetched opcode into the hardware Instruction Register (IR)
        this.writeToBus(opcode, 0);
        this.pulseRegister("LoadIR");

        // Increment PC to point to the operand or the next instruction
        this.setPin("IncPC", true);
        this.pulseClock();
        this.setPin("IncPC", false);

        // Decode & Execute using the software Instruction Set (Strictly typed, no 'var')
        OpcodeMetadata metadata = this.instructionSet.get(opcode);
        if (metadata != null && metadata.logic() != null) {
            metadata.logic().execute(this);
        } else {
            LOG.error("Unhandled or illegal Opcode detected: ${}", String.format("%02X", opcode));
        }
    }

    // ========================================================================
    // HARDWARE PIN & CLOCK CONTROL
    // ========================================================================

    public void setPin(String name, boolean state) {
        if (this.inputs.containsKey(name)) {
            this.inputs.get(name).setState(state, this.ctx);
        }
    }

    public boolean getPin(String name) {
        return this.outputs.getOrDefault(name, new Wire()).getState();
    }

    public void pulseClock() {
        this.ctx.run();
        this.setPin("Clk", true);
        this.ctx.run();
        this.setPin("Clk", false);
        this.ctx.run();
    }

    public void pulseRegister(String... loadPins) {
        for (String pin : loadPins) this.setPin(pin, true);
        this.pulseClock();
        for (String pin : loadPins) this.setPin(pin, false);
    }

    public void indexOp(String pin) {
        this.setPin(pin, true);
        this.pulseClock();
        this.setPin(pin, false);
    }

    // ========================================================================
    // INTERNAL BUS CONTROL
    // ========================================================================

    public void setBusSelector(int source) {
        this.setPin("SelBus0", (source & 1) != 0);
        this.setPin("SelBus1", (source & 2) != 0);
        this.setPin("SelBus2", (source & 4) != 0);
    }

    private void setDataBusIn(int value) {
        for (int i = 0; i < 8; i++) {
            this.setPin("DIn" + i, ((value >> i) & 1) == 1);
        }
    }

    public void writeToBus(int value, int source) {
        this.setBusSelector(source);
        if (source == 0) {
            this.setDataBusIn(value);
        }
        this.ctx.run();
    }

    // ========================================================================
    // MEMORY & EXTERNAL BUS ACCESS
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

    public int getAddressBus() {
        int address = 0;
        for (int i = 0; i < 16; i++) {
            if (this.getPin("AOut" + i)) address |= (1 << i);
        }
        return address;
    }

    // ========================================================================
    // INSTRUCTION FETCH & DECODE
    // ========================================================================

    public int fetchOperand() {
        int val = this.bus.read(this.getAddressBus());
        this.setPin("IncPC", true);
        this.pulseClock();
        this.setPin("IncPC", false);
        return val;
    }

    public int fetchAddress() {
        int low = this.fetchOperand();
        int high = this.fetchOperand();
        return (high << 8) | low;
    }

    // ========================================================================
    // ADDRESSING MODES
    // ========================================================================

    public int addrAbsolute() {
        return this.fetchAddress();
    }

    public int addrAbsoluteX() {
        int base = this.fetchAddress();
        return (base + this.readRegisterDirectly("X")) & 0xFFFF;
    }

    public int addrAbsoluteY() {
        int base = this.fetchAddress();
        return (base + this.readRegisterDirectly("Y")) & 0xFFFF;
    }

    public int addrZeroPage() {
        return this.fetchOperand();
    }

    public int addrZeroPageX() {
        int zp = this.fetchOperand();
        return (zp + this.readRegisterDirectly("X")) & 0xFF;
    }

    public int addrZeroPageY() {
        int zp = this.fetchOperand();
        return (zp + this.readRegisterDirectly("Y")) & 0xFF;
    }

    public int addrIndirect() {
        int pointer = this.fetchAddress();
        int lo = this.bus.read(pointer);
        int hi = this.bus.read((pointer & 0xFF00) | ((pointer + 1) & 0x00FF));
        return (hi << 8) | lo;
    }

    public int addrIndexedIndirectX() {
        int zpAddress = (this.fetchOperand() + this.readRegisterDirectly("X")) & 0xFF;
        int lowByte = this.readSystemBus(zpAddress);
        int highByte = this.readSystemBus((zpAddress + 1) & 0xFF);
        return (highByte << 8) | lowByte;
    }

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

    public void jump(int targetAddress) {
        for (int i = 0; i < 16; i++) {
            this.setPin("AIn" + i, ((targetAddress >> i) & 1) == 1);
        }
        this.setPin("LoadPC", true);
        this.pulseClock();
        this.setPin("LoadPC", false);
    }

    public void loadAccumulatorDirect(int source) {
        this.setBusSelector(source);
        this.setPin("BypassALU", true);
        this.pulseRegister("LoadA");
        this.setPin("BypassALU", false);
    }

    public void executeALU(String opPin, int operandValue, boolean carryIn, boolean saveToAccumulator) {
        this.writeToBus(operandValue, 0);
        this.setPin("CIn", carryIn);
        this.setPin(opPin, true);

        this.setPin("SelC0", true);
        this.setPin("SelC1", false);
        this.setPin("SelZ0", true);
        this.setPin("SelZ1", false);
        this.setPin("SelV0", true);
        this.setPin("SelV1", false);
        this.setPin("SelN0", true);
        this.setPin("SelN1", false);

        if (saveToAccumulator) {
            this.setPin("LoadA", true);
        }

        this.pulseClock();

        this.setPin(opPin, false);
        this.setPin("LoadA", false);
        this.setPin("SelC0", false);
        this.setPin("SelZ0", false);
        this.setPin("SelV0", false);
        this.setPin("SelN0", false);
    }

    public void bitTest(int memoryValue) {
        boolean z = (this.getAccumulator() & memoryValue) == 0;
        boolean n = (memoryValue & 0x80) != 0;
        boolean v = (memoryValue & 0x40) != 0;

        this.setPin("ManZ", z);
        this.setPin("ManN", n);
        this.setPin("ManV", v);

        this.setPin("SelZ0", true);
        this.setPin("SelZ1", true);
        this.setPin("SelN0", true);
        this.setPin("SelN1", true);
        this.setPin("SelV0", true);
        this.setPin("SelV1", true);

        this.pulseClock();

        this.setPin("SelZ0", false);
        this.setPin("SelZ1", false);
        this.setPin("SelN0", false);
        this.setPin("SelN1", false);
        this.setPin("SelV0", false);
        this.setPin("SelV1", false);
    }

    // ========================================================================
    // STATUS FLAGS CONTROL
    // ========================================================================

    public boolean isFlagSet(char flag) {
        this.ctx.run();
        return switch (flag) {
            case 'C' -> this.getPin("OutC");
            case 'Z' -> this.getPin("OutZ");
            case 'V' -> this.getPin("OutV");
            case 'N' -> this.getPin("OutN");
            default -> false;
        };
    }

    public void updateZAndNFlags(int value) {
        this.setPin("ManZ", value == 0);
        this.setPin("ManN", (value & 0x80) != 0);

        this.setPin("SelZ0", true);
        this.setPin("SelZ1", true);
        this.setPin("SelN0", true);
        this.setPin("SelN1", true);

        this.pulseClock();

        this.setPin("SelZ0", false);
        this.setPin("SelZ1", false);
        this.setPin("SelN0", false);
        this.setPin("SelN1", false);
    }

    public void forceFlag(char flag, boolean state) {
        String manPin = "Man" + flag;
        String sel0 = "Sel" + flag + "0";
        String sel1 = "Sel" + flag + "1";

        this.setPin(manPin, state);

        this.setPin(sel0, true);
        this.setPin(sel1, true);

        this.pulseClock();

        this.setPin(sel0, false);
        this.setPin(sel1, false);
    }

    @Override
    public int getStatusRegister() {
        int p = 0x20;
        if (this.isFlagSet('C')) p |= 0x01;
        if (this.isFlagSet('Z')) p |= 0x02;
        if (this.isFlagSet('I')) p |= 0x04;
        if (this.isFlagSet('D')) p |= 0x08;
        if (this.isFlagSet('B')) p |= 0x10;
        if (this.isFlagSet('V')) p |= 0x40;
        if (this.isFlagSet('N')) p |= 0x80;
        return p;
    }

    public void setStatusRegister(int value) {
        this.forceFlag('C', (value & 0x01) != 0);
        this.forceFlag('Z', (value & 0x02) != 0);
        this.forceFlag('I', (value & 0x04) != 0);
        this.forceFlag('D', (value & 0x08) != 0);
        this.forceFlag('V', (value & 0x40) != 0);
        this.forceFlag('N', (value & 0x80) != 0);
    }

    // ========================================================================
    // STACK OPERATIONS
    // ========================================================================

    public void pushStack(int value) {
        int sp = this.readRegisterDirectly("SP");
        this.bus.write(0x0100 | sp, value);
        this.indexOp("DecSP");
    }

    public int pullStack() {
        this.indexOp("IncSP");
        int sp = this.readRegisterDirectly("SP");
        return this.bus.read(0x0100 | sp);
    }

    // ========================================================================
    // DEBUGGING & HARDWARE INSPECTION
    // ========================================================================

    @Override
    public int getAccumulator() {
        return this.readRegisterDirectly("Accumulator");
    }

    @Override
    public CpuState snapshot() {
        int irValue = this.readRegisterDirectly("IR");
        OpcodeMetadata metadata = this.instructionSet.get(irValue);

        return new CpuState(
                this.getAddressBus(),
                this.getAccumulator(),
                this.readRegisterDirectly("X"),
                this.readRegisterDirectly("Y"),
                this.readRegisterDirectly("SP"),
                this.getStatusRegister(),
                irValue,
                metadata != null ? metadata.mnemonic() : "???"
        );
    }

    @Override
    public int readRegisterDirectly(String name) {
        if (name.equals("IR")) {
            int val = 0;
            for (int i = 0; i < 8; i++) {
                if (this.getPin("OPCode" + i)) val |= (1 << i);
            }
            return val;
        }

        if (name.equals("PC")) {
            return this.getAddressBus();
        }

        int source = switch (name) {
            case "Accumulator" -> 1;
            case "X" -> 2;
            case "Y" -> 3;
            case "SP" -> 4;
            case "Status" -> 7;
            default -> {
                LOG.warn("Unmapped register read attempt: {}", name);
                yield 0;
            }
        };

        this.setBusSelector(source);
        this.ctx.run();

        int value = 0;
        for (int i = 0; i < 8; i++) {
            if (this.getPin("DOut" + i)) value |= (1 << i);
        }

        this.setBusSelector(0);
        return value;
    }
}