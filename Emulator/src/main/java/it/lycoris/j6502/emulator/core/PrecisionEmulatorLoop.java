package it.lycoris.j6502.emulator.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

/**
 * Enterprise-grade high-precision game loop for emulators and games.
 * Implements the "Fixed Timestep" pattern to ensure deterministic and smooth physics
 * regardless of the host machine's varying rendering capabilities.
 */
public class PrecisionEmulatorLoop implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PrecisionEmulatorLoop.class);

    private static final long NANOSECONDS_IN_SECOND = 1_000_000_000L;
    private static final int MAX_FRAME_SKIP = 5;
    private static final int DEBUG_REPORT_INTERVAL_SECONDS = 5;

    private final int targetTicksPerSecond;
    private final int targetCpuFrequencyHz;
    private final long timePerTick;
    private final int cpuCyclesPerTick;
    private final int debugLevel;

    private final Motherboard motherboard;
    private final Runnable renderCallback;

    private volatile boolean running;

    private long totalInstructionsExecuted;
    private long emulatorStartTimeNanos;
    private int framesRenderedThisSecond;

    /**
     * Initializes the precision loop with dependency injection.
     *
     * @param targetTicksPerSecond The desired frame rate (e.g., 60 Hz).
     * @param targetCpuFrequencyHz The desired CPU speed (e.g., 1000000 Hz for 1 MHz).
     * @param motherboard          The system motherboard containing all hardware components.
     * @param renderCallback       A functional interface to trigger the UI repaint.
     */
    public PrecisionEmulatorLoop(int targetTicksPerSecond, int targetCpuFrequencyHz, Motherboard motherboard, Runnable renderCallback) {
        this.targetTicksPerSecond = targetTicksPerSecond;
        this.targetCpuFrequencyHz = targetCpuFrequencyHz;
        this.debugLevel = motherboard.debugLevel();

        this.timePerTick = NANOSECONDS_IN_SECOND / targetTicksPerSecond;

        this.cpuCyclesPerTick = targetCpuFrequencyHz / targetTicksPerSecond;

        this.motherboard = motherboard;
        this.renderCallback = renderCallback;
        this.running = false;

        this.totalInstructionsExecuted = 0L;
        this.framesRenderedThisSecond = 0;
    }

    /**
     * Starts the emulator loop using modern Java Platform Threads.
     */
    public void start() {
        this.running = true;
        this.totalInstructionsExecuted = 0L;
        this.framesRenderedThisSecond = 0;
        this.emulatorStartTimeNanos = System.nanoTime();

        Thread.ofPlatform()
                .name("MainLoop")
                .start(this);

        LOG.info("Precision loop started. Target: {} FPS, Target Frequency: {} Hz ({} cycles/frame)",
                this.targetTicksPerSecond, this.targetCpuFrequencyHz, this.cpuCyclesPerTick);
    }

    /**
     * Signals the loop to terminate gracefully.
     */
    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        LOG.info("Initiating hardware reset sequence...");
        this.motherboard.cpu().reset();

        long previousTime = System.nanoTime();
        long accumulatedTime = 0L;

        long lastMetricsReportTime = System.nanoTime();
        long lastDebugReportTime = System.nanoTime();

        while (this.running) {
            long loopStartTime = System.nanoTime();
            long elapsedTime = loopStartTime - previousTime;
            previousTime = loopStartTime;

            accumulatedTime += elapsedTime;

            // Spiral of Death Prevention: Avoid catastrophic lag buildup
            if (accumulatedTime > this.timePerTick * MAX_FRAME_SKIP) {
                accumulatedTime = this.timePerTick;
            }

            // 1. FIXED TIMESTEP UPDATE (Hardware Emulation)
            while (accumulatedTime >= this.timePerTick && this.running) {
                this.updateHardwareState();
                accumulatedTime -= this.timePerTick;
            }

            // 2. RENDER PHASE
            if (this.running) {
                this.renderGraphics();
                this.framesRenderedThisSecond++;
            }

            // 3. TELEMETRY REPORTING (Every 1 Second - INFO Level)
            if (loopStartTime - lastMetricsReportTime >= NANOSECONDS_IN_SECOND) {
                if (LOG.isDebugEnabled()) LOG.info("TELEMETRY | Rendering: {} FPS | Target Frequency: {} Hz", this.framesRenderedThisSecond, this.targetCpuFrequencyHz);

                this.framesRenderedThisSecond = 0;
                lastMetricsReportTime = loopStartTime;
            }

            // 4. PERIODIC PERFORMANCE RECAP (Every 5 Seconds - DEBUG Level)
            if (LOG.isDebugEnabled() && (loopStartTime - lastDebugReportTime >= DEBUG_REPORT_INTERVAL_SECONDS * NANOSECONDS_IN_SECOND)) {
                this.logPerformanceMetrics(this.totalInstructionsExecuted, this.emulatorStartTimeNanos, loopStartTime);
                lastDebugReportTime = loopStartTime;
            }

            // 5. BUSY-WAIT OPTIMIZATION
            this.yieldOrSleep(loopStartTime, accumulatedTime);
        }

        long emulatorEndTimeNanos = System.nanoTime();
        this.logPerformanceMetrics(this.totalInstructionsExecuted, this.emulatorStartTimeNanos, emulatorEndTimeNanos);
        LOG.info("Final CPU State:\n{}", this.motherboard.cpu().snapshot());
    }

    /**
     * Steps the CPU and PPU forward by the exact number of cycles required for one frame.
     */
    private void updateHardwareState() {
        int cyclesExecutedInFrame = 0;
        Cpu cpu = this.motherboard.cpu();
        SystemBus bus = this.motherboard.bus();

        while (cyclesExecutedInFrame < this.cpuCyclesPerTick && this.running) {
            long cyclesBeforeStep = cpu.getTotalClockCycles();

            int programCounter = cpu.getProgramCounter();
            int opcode = bus.read(programCounter);

            if (LOG.isDebugEnabled() && this.debugLevel > 0) {
                LOG.debug("PC: ${} | Opcode: ${}", String.format("%04X", programCounter), String.format("%02X", opcode));
            }

            if (opcode == 0x00) {
                LOG.info(">>> BRK instruction reached (0x00). Halting execution gracefully.");
                this.stop();
                break;
            }

            cpu.step();
            this.totalInstructionsExecuted++;

            long stepCycles = cpu.getTotalClockCycles() - cyclesBeforeStep;

            // Fail-Safe: Detect CPU Crash to prevent infinite loop deadlock
            if (stepCycles == 0) {
                LOG.error("CPU execution halted (0 cycles consumed). Stopping precision loop.");
                this.stop();
                break;
            }

            cyclesExecutedInFrame += (int) stepCycles;

            this.motherboard.ppu().tick((int) stepCycles, cpu);
        }
    }

    /**
     * Pushes the current double-buffer to the UI safely.
     */
    private void renderGraphics() {
        if (this.renderCallback != null) this.renderCallback.run();
    }

    private void yieldOrSleep(long loopStartTime, long accumulatedTime) {
        long timeSpentProcessing = System.nanoTime() - loopStartTime;

        long remainingNanos = this.timePerTick - (accumulatedTime + timeSpentProcessing);

        if (remainingNanos > 1_500_000L) {
            LockSupport.parkNanos(remainingNanos - 500_000L);
        } else if (remainingNanos > 0) {
            Thread.yield();
        }
    }

    /**
     * Calculates and logs the execution performance metrics using Java Text Blocks.
     *
     * @param totalSteps     Total instructions executed.
     * @param startTimeNanos Execution start time in nanoseconds.
     * @param endTimeNanos   Execution end time in nanoseconds.
     */
    private void logPerformanceMetrics(long totalSteps, long startTimeNanos, long endTimeNanos) {
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