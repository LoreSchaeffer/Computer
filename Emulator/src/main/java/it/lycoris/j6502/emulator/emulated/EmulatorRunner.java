package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.hardware.GateLevelCpu;
import it.lycoris.j6502.emulator.model.CpuState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles the execution lifecycle of the emulator, managing the main clock loop,
 * halting conditions, and watchdog limits.
 */
public class EmulatorRunner {
    private static final Logger LOG = LoggerFactory.getLogger(EmulatorRunner.class);
    private final Motherboard motherboard;
    private final int startAddress;
    private final int maxSteps;

    /**
     * Constructs a new EmulatorRunner.
     *
     * @param startAddress The 16-bit memory address where execution starts.
     * @param maxSteps     The watchdog limit for execution cycles (-1 for infinite).
     * @param cpuType      The type of CPU emulation to use (e.g., SOFTWARE_EMULATED, HARDWARE_EMULATED).
     */
    public EmulatorRunner(int startAddress, int maxSteps, Cpu.Type cpuType) {
        LOG.info("Initializing Lyco-8 using {} CPU...", cpuType.equals(Cpu.Type.HARDWARE_EMULATED) ? "Hardware-Emulated (Gate-Level)" : "Software-Emulated (Instruction-Level)");
        LOG.debug("Debug mode enabled: Verbose CPU logging enabled");

        this.motherboard = new Motherboard(cpuType);
        this.startAddress = startAddress;
        this.maxSteps = maxSteps;
    }

    /**
     * Loads a binary file into the emulated memory and begins execution.
     *
     * @param binFile The compiled machine code file.
     */
    public void loadProgramFromFileAndRun(@NotNull File binFile) {
        LOG.info("Loading program file: {}", binFile.getName());

        if (!binFile.exists() || !binFile.isFile()) {
            LOG.error("File not found or invalid: {}", binFile.getAbsolutePath());
            System.exit(1);
        }

        try {
            byte[] program = Files.readAllBytes(binFile.toPath());
            LOG.info("Program loaded into Java memory ({} bytes)", program.length);
            loadProgramAndRun(program);
        } catch (IOException e) {
            LOG.error("I/O error while reading the program file: {}", binFile.getAbsolutePath(), e);
            System.exit(1);
        }
    }

    public void loadProgramAndRun(@NotNull byte[] program) {
        LOG.info("Injecting program into emulated RAM at address ${} ({} bytes)", String.format("%04X", this.startAddress), program.length);
        this.motherboard.loadProgram(this.startAddress, program);
        LOG.info("Program injected into emulated RAM at address ${} ({} bytes)", String.format("%04X", this.startAddress), program.length);

        Thread.ofVirtual()
                .name("CPU")
                .start(this::run);
    }

    /**
     * The main execution loop.
     */
    private void run() {
        Cpu cpu = this.motherboard.cpu();
        SystemBus bus = this.motherboard.bus();

        cpu.reset();

        boolean infiniteLoop = (this.maxSteps <= 0);
        LOG.info("Starting CPU execution {}", infiniteLoop ? "(Infinite Mode)" : "(Max steps: " + this.maxSteps + ")");

        int stepCounter = 0;
        long startTimeNanos = System.nanoTime();

        while (infiniteLoop || stepCounter < this.maxSteps) {
            int programCounter = cpu.readRegisterDirectly("PC");
            int opcode = bus.read(programCounter);

            if (LOG.isDebugEnabled()) {
                CpuState state = cpu.snapshot();
                this.logCpuTrace(stepCounter, state);
            }

            if (this.checkHaltConditions(opcode, programCounter, bus)) break;

            cpu.step();
            stepCounter++;
        }

        long endTimeNanos = System.nanoTime();

        LOG.info("Execution sequence finished.");

        if (!infiniteLoop && stepCounter >= this.maxSteps) LOG.warn("Execution watchdog triggered: Max steps exceeded ({}).", this.maxSteps);

        this.logPerformanceMetrics(stepCounter, startTimeNanos, endTimeNanos);

        LOG.info("Final CPU State:\n{}", cpu.snapshot());
    }

    /**
     * Evaluates specific opcodes to determine if the CPU should halt execution.
     *
     * @param opcode         The next opcode to execute.
     * @param programCounter The current Program Counter address.
     * @param bus            The system memory bus.
     * @return true if a halt condition is met, false otherwise.
     */
    private boolean checkHaltConditions(int opcode, int programCounter, SystemBus bus) {
        if (opcode == 0x00) {
            System.out.flush();
            LOG.info(">>> BRK instruction reached (0x00). Halting execution gracefully.");
            return true;
        }

        if (opcode == 0x4C) {
            int targetAddress = bus.read(programCounter + 1) | (bus.read(programCounter + 2) << 8);
            if (targetAddress == programCounter) {
                System.out.flush();
                LOG.info(">>> Infinite Loop detected (JMP to self). Halting execution gracefully.");
                return true;
            }
        }

        return false;
    }

    /**
     * Prints a highly readable, single-line trace of the CPU state for debugging purposes.
     *
     * @param stepCounter The current execution cycle.
     * @param state       The snapshot of the CPU at the current cycle.
     */
    private void logCpuTrace(int stepCounter, CpuState state) {
        String binaryFlags = Integer.toBinaryString(state.status() | 0x100).substring(1);

        String traceLog = "[STEP %06d] PC:$%04X | A:$%02X X:$%02X Y:$%02X P:%s | Op:$%02X (%s)"
                .formatted(
                        stepCounter,
                        state.pc(),
                        state.accumulator(),
                        state.x(),
                        state.y(),
                        binaryFlags,
                        state.currentOpcode(),
                        state.instructionName()
                );

        LOG.debug(traceLog);
    }

    /**
     * Calculates and logs the execution performance metrics using Java Text Blocks.
     *
     * @param totalSteps     Total instructions executed.
     * @param startTimeNanos Execution start time in nanoseconds.
     * @param endTimeNanos   Execution end time in nanoseconds.
     */
    private void logPerformanceMetrics(int totalSteps, long startTimeNanos, long endTimeNanos) {
        long durationNanos = Math.max(endTimeNanos - startTimeNanos, 1);

        double durationMillis = durationNanos / 1_000_000.0;
        double durationSeconds = durationNanos / 1_000_000_000.0;

        double instructionsPerSecond = totalSteps / durationSeconds;
        double frequencyMHz = instructionsPerSecond / 1_000_000.0;
        double frequency = frequencyMHz < 1 ? frequencyMHz * 1000 :  frequencyMHz;
        String frequencyUnit = frequencyMHz < 1 ? "KHz" : "MHz";


        String recapLog = """
                
                ================ PERFORMANCE RECAP ================
                Total Instructions : %,d
                Elapsed Time       : %.3f ms
                Effective Speed    : %,.0f IPS (%.4f %s)
                ===================================================
                """.formatted(totalSteps, durationMillis, instructionsPerSecond, frequency, frequencyUnit);

        LOG.info(recapLog);
    }
}
