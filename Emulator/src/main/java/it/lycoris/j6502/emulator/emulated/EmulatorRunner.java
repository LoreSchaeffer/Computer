package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.hardware.MOS6502;
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
     */
    public EmulatorRunner(int startAddress, int maxSteps) {
        LOG.info("Initializing Lyco-8...");
        LOG.debug("Debug mode enabled: Verbose CPU logging enabled");

        this.motherboard = new Motherboard();
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

            this.motherboard.loadProgram(this.startAddress, program);
            LOG.info("Program injected into emulated RAM at address ${}", String.format("%04X", this.startAddress));

            run();
        } catch (IOException e) {
            LOG.error("I/O error while reading the program file: {}", binFile.getAbsolutePath(), e);
            System.exit(1);
        }
    }

    public void loadProgramAndRun(@NotNull byte[] program) {
        LOG.info("Loading program to emulator ({} bytes)", program.length);
        motherboard.loadProgram(startAddress, program);
        LOG.info("Program loaded successfully!");

        run();
    }

    /**
     * The main execution loop.
     */
    private void run() {
        MOS6502 cpu = this.motherboard.cpu();
        SystemBus bus = this.motherboard.bus();

        cpu.reset();

        boolean infiniteLoop = (this.maxSteps <= 0);
        LOG.info("Starting CPU execution {}", infiniteLoop ? "(Infinite Mode)" : "(Max steps: " + this.maxSteps + ")");

        int stepCounter = 0;
        boolean halted = false;

        long startTimeNanos = System.nanoTime();

        while (!halted && (infiniteLoop || stepCounter < this.maxSteps)) {
            int pc = cpu.getAddressBus();
            int opcode = bus.read(pc);
            if (LOG.isDebugEnabled()) {
                CpuState state = cpu.snapshot();
                LOG.debug("[STEP {}] PC:${} | Op:${} ({}) | A:${} X:${} Y:${} P:${}",
                        String.format("%06d", stepCounter),
                        String.format("%04X", pc),
                        String.format("%02X", opcode),
                        String.format("%-8s", state.instructionName()),
                        String.format("%02X", state.accumulator()),
                        String.format("%02X", state.x()),
                        String.format("%02X", state.y()),
                        String.format("%02X", state.status()));
            }

            // Halt Detection Logic
            if (opcode == 0x00) {
                LOG.info(">>> BRK instruction reached (0x00). Halting execution gracefully.");
                halted = true;
            } else if (opcode == 0x4C) {
                // Fetch the JMP target from the bus
                int target = bus.read(pc + 1) | (bus.read(pc + 2) << 8);
                if (target == pc) {
                    LOG.info(">>> Infinite Loop detected (JMP to self). Halting execution gracefully.");
                    halted = true;
                }
            }

            if (!halted) {
                cpu.step();
                stepCounter++;
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

    private void logPerformanceMetrics(int totalSteps, long startTimeNanos, long endTimeNanos) {
        long durationNanos = endTimeNanos - startTimeNanos;
        if (durationNanos == 0) durationNanos = 1;

        double durationMillis = durationNanos / 1_000_000.0;
        double durationSeconds = durationNanos / 1_000_000_000.0;

        // Calculate Instructions Per Second (IPS)
        double instructionsPerSecond = totalSteps / durationSeconds;

        // Convert to MHz (Megahertz equivalent)
        double frequencyMHz = instructionsPerSecond / 1_000_000.0;

        LOG.info("================ PERFORMANCE RECAP ================");
        LOG.info("Total Instructions : {}", totalSteps);
        LOG.info("Elapsed Time       : {} ms", String.format("%.3f", durationMillis));
        LOG.info("Effective Speed    : {} IPS ({} MHz)",
                String.format("%,.0f", instructionsPerSecond),
                String.format("%.4f", frequencyMHz));
        LOG.info("===================================================");
    }
}
