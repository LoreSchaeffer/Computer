package it.lycoris.j6502.emulator.core;

import it.lycoris.j6502.emulator.core.cpu.CpuState;
import it.lycoris.j6502.emulator.core.cpu.HighLevelCpu;
import it.lycoris.j6502.emulator.hardware.Ram;
import it.lycoris.j6502.emulator.instructions.OpcodeMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enterprise Integration Test suite for the High-Level 6502 CPU core.
 * Executes the Klaus2m5 functional test binary in a headless,
 * maximum-performance environment to validate HLE accuracy.
 */
@Tag("fast-integration")
public class HleKlausFunctionalTest {
    private static final Logger LOG = LoggerFactory.getLogger(HleKlausFunctionalTest.class);

    private static final int SUCCESS_ADDRESS = 0x3469;
    private static final int KLAUS_START_ADDRESS = 0x0400;
    private static final int MAXIMUM_CYCLES = 100_000_000;
    private static final int TRACE_BUFFER_SIZE = 20;

    private SystemBus systemBus;
    private HighLevelCpu cpu;
    private Ram memory;

    private InstructionSet instructionMetadataRegistry;
    private Map<Integer, String> assemblyListingMap;

    @BeforeEach
    public void setUp() {
        this.systemBus = new SystemBus();
        this.memory = new Ram(0x0000, 0x10000); // Flat 64KB memory space
        this.systemBus.attachDevice(this.memory);

        this.cpu = new HighLevelCpu(this.systemBus);
        this.instructionMetadataRegistry = new InstructionSet();

        this.assemblyListingMap = this.loadAssemblyListing("6502_functional_test.lst");
    }

    @Test
    @DisplayName("Execute Klaus2m5 6502 Functional Test Suite (HLE Engine)")
    public void executeKlausFunctionalTest() {
        this.loadBinaryToMemory("6502_functional_test.bin", 0x0000);

        this.cpu.reset();

        int lowByte = KLAUS_START_ADDRESS & 0xFF;
        int highByte = (KLAUS_START_ADDRESS >> 8) & 0xFF;
        this.systemBus.write(0xFFFC, lowByte);
        this.systemBus.write(0xFFFD, highByte);
        this.cpu.reset();

        int previousProgramCounter = -1;
        long executionSteps = 0L;
        long heartbeatThreshold = 10_000_000L;
        long startTimeMillis = System.currentTimeMillis();

        Deque<String> traceBuffer = new ArrayDeque<>(TRACE_BUFFER_SIZE);

        LOG.info("Starting Klaus2m5 functional test on High-Level CPU engine...");

        while (executionSteps < MAXIMUM_CYCLES) {
            int currentProgramCounter = this.cpu.getProgramCounter();
            int currentOpcode = this.systemBus.read(currentProgramCounter);

            this.recordTrace(traceBuffer, currentOpcode);

            if (executionSteps > 0 && executionSteps % heartbeatThreshold == 0) {
                long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                LOG.info("Executed {} million instructions in {} ms.", (executionSteps / 1_000_000L), elapsedTimeMillis);
            }

            // The Klaus test signals success or failure by trapping itself in an infinite loop
            if (currentProgramCounter == previousProgramCounter) {
                if (currentProgramCounter == SUCCESS_ADDRESS) {
                    long totalTimeMillis = System.currentTimeMillis() - startTimeMillis;
                    LOG.info("SUCCESS! Reached target trap address in {} ms.", totalTimeMillis);
                    assertEquals(SUCCESS_ADDRESS, currentProgramCounter, "Klaus test successfully completed.");
                    return;
                } else {
                    this.failWithTrace(currentProgramCounter, executionSteps, traceBuffer);
                }
            }

            previousProgramCounter = currentProgramCounter;

            try {
                this.cpu.step();
            } catch (Exception exception) {
                this.failWithTrace("Java exception during native execution: " + exception.getMessage(), currentProgramCounter, executionSteps, traceBuffer);
            }

            executionSteps++;
        }

        fail("Klaus test timed out. Maximum cycles exceeded without reaching a trap.");
    }

    /**
     * Records a formatted string of the current CPU state into the rolling buffer.
     *
     * @param traceBuffer   The deque acting as a circular buffer.
     * @param currentOpcode The opcode currently fetched.
     */
    private void recordTrace(Deque<String> traceBuffer, int currentOpcode) {
        if (traceBuffer.size() >= TRACE_BUFFER_SIZE) {
            traceBuffer.removeFirst();
        }

        CpuState state = this.cpu.snapshot();
        OpcodeMetadata metadata = this.instructionMetadataRegistry.get(currentOpcode);
        String mnemonic = (metadata != null) ? metadata.mnemonic() : "UNKNOWN";

        traceBuffer.addLast(state.toTraceString(currentOpcode, mnemonic));
    }

    /**
     * Fails the test, dumping the execution history to standard error for forensic analysis.
     *
     * @param trapAddress    The address where the infinite loop occurred.
     * @param executionSteps Total instructions executed before failure.
     * @param traceBuffer    The rolling buffer containing the execution history.
     */
    private void failWithTrace(int trapAddress, long executionSteps, Deque<String> traceBuffer) {
        this.failWithTrace(String.format("Trapped in an infinite loop at address 0x%04X.", trapAddress), trapAddress, executionSteps, traceBuffer);
    }

    private void failWithTrace(String reason, int trapAddress, long executionSteps, Deque<String> traceBuffer) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        errorMessageBuilder.append("Klaus test failed! ").append(reason).append("\n");
        errorMessageBuilder.append("Step: ").append(executionSteps).append("\n\n");

        errorMessageBuilder.append("--- TARGET ASSEMBLY LISTING ---\n");
        String listingSource = this.assemblyListingMap.getOrDefault(trapAddress, "[No assembly source found for this address in the listing file]");
        errorMessageBuilder.append(listingSource).append("\n\n");

        errorMessageBuilder.append("--- EXECUTION TRACE FORENSICS ---\n");
        for (String traceLine : traceBuffer) {
            errorMessageBuilder.append(traceLine).append("\n");
        }

        errorMessageBuilder.append("---------------------------------\n");
        fail(errorMessageBuilder.toString());
    }

    private void loadBinaryToMemory(String resourceName, int startAddress) {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(inputStream, "Test binary file not found in classpath.");

            byte[] programBytes = inputStream.readAllBytes();
            int[] unsignedProgram = new int[programBytes.length];

            for (int index = 0; index < programBytes.length; index++) {
                unsignedProgram[index] = programBytes[index] & 0xFF;
            }

            this.memory.loadProgram(startAddress, unsignedProgram);
        } catch (Exception exception) {
            fail("Failed to load Klaus binary into test environment.", exception);
        }
    }

    /**
     * Parses the assembler listing file to map memory addresses to their original source code.
     *
     * @param resourceName The name of the listing file (e.g., .lst).
     * @return A map containing memory addresses as keys and the raw listing line as values.
     */
    private Map<Integer, String> loadAssemblyListing(String resourceName) {
        Map<Integer, String> listingMap = new HashMap<>();

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                LOG.warn("Listing file {} not found. Traces will not include source mapping.", resourceName);
                return listingMap;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.length() >= 4) {
                        String addressString = currentLine.substring(0, 4);
                        if (addressString.matches("^[0-9A-Fa-f]{4}$")) {
                            int address = Integer.parseInt(addressString, 16);
                            listingMap.merge(address, currentLine, (oldLine, newLine) -> oldLine + "\n" + newLine);
                        }
                    }
                }
            }
        } catch (Exception exception) {
            LOG.error("Failed to parse assembly listing file: {}", resourceName, exception);
        }

        return listingMap;
    }
}