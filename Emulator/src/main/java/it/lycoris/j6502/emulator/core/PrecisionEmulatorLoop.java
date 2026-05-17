package it.lycoris.j6502.emulator.core;

import it.lycoris.j6502.emulator.core.cpu.Cpu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;
import java.util.function.DoubleConsumer;

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
    private final DoubleConsumer fpsCallback;

    private volatile boolean running;

    private long totalInstructionsExecuted;
    private long emulatorStartTimeNanos;

    private int framesRenderedThisSecond;
    private long lastDebugReportTimeNanos;

    /**
     * Initializes the precision emulation loop container.
     *
     * @param motherboard          The initialized system motherboard.
     * @param renderCallback       The UI refresh callback trigger.
     * @param targetTicksPerSecond Target frames per second (e.g., 60).
     * @param targetCpuFrequencyHz Target CPU clock rate in Hz (e.g., 1000000).
     * @param debugLevel           Logging verbosity control.
     */
    public PrecisionEmulatorLoop(Motherboard motherboard, Runnable renderCallback, DoubleConsumer fpsCallback, int targetTicksPerSecond, int targetCpuFrequencyHz, int debugLevel) {
        this.motherboard = motherboard;
        this.renderCallback = renderCallback;
        this.fpsCallback = fpsCallback;
        this.targetTicksPerSecond = targetTicksPerSecond;
        this.targetCpuFrequencyHz = targetCpuFrequencyHz;
        this.timePerTick = NANOSECONDS_IN_SECOND / targetTicksPerSecond;
        this.cpuCyclesPerTick = targetCpuFrequencyHz / targetTicksPerSecond;
        this.debugLevel = debugLevel;
    }

    /**
     * Starts the emulator loop using modern Java Platform Threads.
     */
    public void start() {
        this.running = true;
        this.totalInstructionsExecuted = 0L;
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
        this.running = true;
        this.emulatorStartTimeNanos = System.nanoTime();
        this.lastDebugReportTimeNanos = this.emulatorStartTimeNanos;
        long nextTickTimeNanos = System.nanoTime();

        LOG.info("Main loop started. Target: {} FPS, Target Frequency: {} Hz ({} cycles/frame)", targetTicksPerSecond, targetCpuFrequencyHz, cpuCyclesPerTick);

        while (running) {
            int cyclesExecuted = 0;

            // Step through the total allocated clock cycles budget for the current video frame
            while (cyclesExecuted < cpuCyclesPerTick) {
                long cyclesBefore = motherboard.cpu().getTotalClockCycles();
                motherboard.cpu().step();

                long cyclesAfter = motherboard.cpu().getTotalClockCycles();
                int cyclesTaken = (int) (cyclesAfter - cyclesBefore);

                cyclesExecuted += cyclesTaken;
                this.totalInstructionsExecuted++;

                // Synchronize the PPU precisely with the elapsed CPU cycles
                motherboard.ppu().tick(cyclesTaken);
            }

            // Refresh the graphical user interface display layout after all frame cycles complete
            if (renderCallback != null) {
                renderCallback.run();
            }

            this.framesRenderedThisSecond++;

            long currentNanos = System.nanoTime();

            if (this.debugLevel > 0 && (currentNanos - this.lastDebugReportTimeNanos) >= (DEBUG_REPORT_INTERVAL_SECONDS * NANOSECONDS_IN_SECOND)) {
                double actualFps = (double) this.framesRenderedThisSecond / DEBUG_REPORT_INTERVAL_SECONDS;
                LOG.debug("Performance Status -> FPS: {}, CPU Instructions Executed: {}", String.format("%.2f", actualFps), this.totalInstructionsExecuted);

                if (this.fpsCallback != null) this.fpsCallback.accept(actualFps);

                this.framesRenderedThisSecond = 0;
                this.lastDebugReportTimeNanos = currentNanos;
            }

            nextTickTimeNanos += timePerTick;
            long remainingNanos = nextTickTimeNanos - System.nanoTime();

            // Precision throttling execution flow mechanics
            if (remainingNanos > 0) {
                if (remainingNanos > 2_000_000L) {
                    LockSupport.parkNanos(remainingNanos - 500_000L);
                }
                while (System.nanoTime() < nextTickTimeNanos) {
                    Thread.yield();
                }
            } else if (Math.abs(remainingNanos) > timePerTick * MAX_FRAME_SKIP) {
                // Fall-behind fallback processing context reset
                nextTickTimeNanos = System.nanoTime();
            }
        }

        logPerformanceMetrics(totalInstructionsExecuted, emulatorStartTimeNanos, System.nanoTime());
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