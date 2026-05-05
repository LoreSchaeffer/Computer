package it.lycoris.j6502.emulator.core;

import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.LockSupport;

/**
 * Handles the execution lifecycle of the emulator, managing the main clock loop,
 * halting conditions, and watchdog limits.
 */
public class EmulatorRunner {
    private static final Logger LOG = LoggerFactory.getLogger(EmulatorRunner.class);

    private final Motherboard motherboard;
    private final int startAddress;
    private final int maxSteps;
    private final int targetFrequencyHz;

    /**
     * Constructs a new EmulatorRunner.
     *
     * @param startAddress      The 16-bit memory address where execution starts.
     * @param maxSteps          The watchdog limit for execution cycles (-1 for infinite).
     * @param targetFrequencyHz The target emulation frequency in Hz (for future timing control).
     */
    public EmulatorRunner(int startAddress, int maxSteps, int targetFrequencyHz) {
        LOG.info("Initializing Lyco-8 Gate-Level Emulator...");
        LOG.debug("Debug mode enabled: Verbose CPU logging enabled");

        this.motherboard = new Motherboard(targetFrequencyHz);
        this.startAddress = startAddress;
        this.maxSteps = maxSteps;
        this.targetFrequencyHz = targetFrequencyHz;
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

            this.loadProgramAndRun(program);
        } catch (IOException exception) {
            LOG.error("I/O error while reading the program file: {}", binFile.getAbsolutePath(), exception);
            System.exit(1);
        }
    }

    /**
     * Injects the raw byte array into the motherboard and starts the execution thread.
     *
     * @param program The compiled machine code.
     */
    public void loadProgramAndRun(byte[] program) {
        LOG.info("Injecting program into emulated ROM at address ${} ({} bytes)", String.format("%04X", this.startAddress), program.length);
        this.motherboard.loadProgram(this.startAddress, program);

        Thread.ofVirtual()
                .name("CPU-Execution-Thread")
                .start(this::run);
    }

    /**
     * The main hardware execution loop.
     */
    private void run() {
        Cpu cpu = this.motherboard.cpu();
        SystemBus bus = this.motherboard.bus();

        cpu.reset();

        boolean infiniteLoop = (this.maxSteps <= 0);
        String speedMode = (this.targetFrequencyHz > 0) ? String.format("(Capped at %d Hz)", this.targetFrequencyHz) : "(Uncapped)";

        LOG.info("Starting CPU execution {} {}", infiniteLoop ? "[Infinite Mode]" : "[Max steps: " + this.maxSteps + "]", speedMode);

        int stepCounter = 0;
        long startTimeNanos = System.nanoTime();

        final double nanosPerInstruction = (this.targetFrequencyHz > 0) ? (1_000_000_000.0 / this.targetFrequencyHz) : 0;
        long nextInstructionTime = startTimeNanos;

        while (infiniteLoop || stepCounter < this.maxSteps) {
            int programCounter = cpu.getProgramCounter();
            int opcode = bus.read(programCounter);

            if (LOG.isDebugEnabled()) {
                CpuState state = cpu.snapshot();
                OpcodeMetadata metadata = this.motherboard.instructionSet().get(opcode);
                this.logCpuTrace(state, opcode, metadata.mnemonic());
            }

            if (this.checkHaltConditions(opcode)) break;

            long cyclesBeforeStep = cpu.getTotalClockCycles();

            cpu.step();
            stepCounter++;

            long cyclesConsumed = cpu.getTotalClockCycles() - cyclesBeforeStep;
            this.motherboard.ppu().tick((int) cyclesConsumed, cpu);

            if (this.targetFrequencyHz > 0) {
                nextInstructionTime += (long) nanosPerInstruction;
                long sleepNanos = nextInstructionTime - System.nanoTime();

                if (sleepNanos > 0) {
                    LockSupport.parkNanos(sleepNanos);
                } else if (sleepNanos < -1_000_000_000) {
                    nextInstructionTime = System.nanoTime();
                }
            }
        }

        long endTimeNanos = System.nanoTime();
        LOG.info("Execution sequence finished.");

        if (!infiniteLoop && stepCounter >= this.maxSteps) {
            LOG.warn("Execution watchdog triggered: Max steps exceeded ({}).", this.maxSteps);
        }

        this.logPerformanceMetrics(stepCounter, startTimeNanos, endTimeNanos);
        LOG.info("Final CPU State:\n{}", cpu.snapshot());
    }

    /**
     * Evaluates specific opcodes to determine if the CPU should halt execution.
     *
     * @param opcode The next opcode to execute.
     * @return true if a halt condition is met, false otherwise.
     */
    private boolean checkHaltConditions(int opcode) {
        // Halt on BRK
        if (opcode == 0x00) {
            System.out.flush();
            LOG.info(">>> BRK instruction reached (0x00). Halting execution gracefully.");
            return true;
        }

        return false;
    }

    public Motherboard getMotherboard() {
        return this.motherboard;
    }

    /**
     * Prints a highly readable, single-line trace of the CPU state for debugging purposes.
     *
     * @param state           The snapshot of the CPU at the current cycle.
     * @param currentOpcode   The opcode being executed at this cycle.
     * @param instructionName The human-readable name of the instruction being executed.
     */
    private void logCpuTrace(CpuState state, int currentOpcode, String instructionName) {
        if (state == null) return;
        LOG.debug(state.toTraceString(currentOpcode, instructionName));
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
        double frequency = frequencyMHz < 1 ? frequencyMHz * 1000 : frequencyMHz;
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