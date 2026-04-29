package it.lycoris.j6502.emulator;

import ch.qos.logback.classic.Level;
import it.lycoris.j6502.emulator.hardware.MOS6502;
import it.lycoris.j6502.emulator.hardware.io.ComponentLibrary;
import it.lycoris.j6502.emulator.model.CpuState;
import it.lycoris.j6502.emulator.system.Memory;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Emulator {
    private static final Logger LOG = LoggerFactory.getLogger(Emulator.class);

    static void main(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> helpOpt = parser.acceptsAll(List.of("help", "h"), "Show this help message").forHelp();
        OptionSpec<File> inputOpt = parser.acceptsAll(List.of("input", "i"), "The input .bin file to emulate").withRequiredArg().ofType(File.class);
        OptionSpec<String> originOpt = parser.acceptsAll(List.of("origin", "o"), "Start address in hex (default: 8000)").withOptionalArg().defaultsTo("8000");
        OptionSpec<Integer> stepsOpt = parser.acceptsAll(List.of("steps", "s"), "Max execution steps (default: infinite)").withOptionalArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<Void> debugOpt = parser.acceptsAll(List.of("debug", "d"), "Launch debug mode");

        try {
            OptionSet options = parser.parse(args);

            if (options.has(helpOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            if (!options.has(inputOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            File binFile = options.valueOf(inputOpt);
            int startAddress = Integer.parseInt(options.valueOf(originOpt).replace("$", ""), 16);
            int maxSteps = options.valueOf(stepsOpt);
            boolean debug = options.has(debugOpt);

            if (debug) {
                ch.qos.logback.classic.Logger emulatorLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("it.lycoris.j6502.emulator");
                emulatorLogger.setLevel(Level.DEBUG);
            }

            launchEmulator(binFile, startAddress, maxSteps, debug);
        } catch (OptionException e) {
            LOG.error("Invalid arguments: {}", e.getMessage());

            try {
                parser.printHelpOn(System.out);
            } catch (IOException ioException) {
                LOG.error("Failed to print help", ioException);
            }

            System.exit(1);
        } catch (Exception e) {
            LOG.error("A fatal error occurred", e);
            System.exit(1);
        }
    }

    private static void launchEmulator(File binFile, int startAddress, int maxSteps, boolean debug) {
        if (!binFile.exists() || !binFile.isFile()) {
            LOG.error("File not found: {}", binFile.getAbsolutePath());
            System.exit(1);
        }

        LOG.info("Starting J6502 Emulator...");
        if (debug) LOG.info("Debug mode enabled: Verbose CPU state logging activated");

        try {
            // Hardware initialization

            ComponentLibrary lib = new ComponentLibrary();
            lib.loadFromResources("hardware");

            Memory ram = new Memory();

            // Load program into memory

            byte[] program = Files.readAllBytes(binFile.toPath());
            ram.loadProgram(startAddress, program);
            LOG.info("Loaded {} bytes at the address ${}", program.length, String.format("%04X", startAddress));

            // Reset Vector configuration

            ram.write(0xFFFC, startAddress & 0xFF);
            ram.write(0xFFFD, (startAddress >> 8) & 0xFF);

            MOS6502 cpu = new MOS6502(lib, ram);
            cpu.reset();

            LOG.info("Starting execution (Max Steps: {})", maxSteps);

            // Dynamic Execution Lifecycle

            int stepCounter = 0;
            boolean halted = false;

            while (!halted) {
                if (maxSteps > 0 && stepCounter >= maxSteps) {
                    LOG.warn("Max steps exceeded ({}), stopping execution...", maxSteps);
                    break;
                }

                int pc = cpu.getAddressBus();
                int opcode = ram.read(pc);

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

                // Check for generic halt

                if (opcode == 0x00) {
                    LOG.info(">>> BRK instruction reached (0x00). Halting execution.");
                    halted = true;
                } else if (opcode == 0x4C) {
                    int target = ram.read(pc + 1) | (ram.read(pc + 2) << 8);
                    if (target == pc) {
                        LOG.info(">>> Infinite Loop detected (JMP to self). Terminating execution.");
                        halted = true;
                    }
                }

                if (!halted) {
                    cpu.step();
                    stepCounter++;
                }
            }

            LOG.info("Execution finished.");
            LOG.info("Final CPU State:\n{}", cpu.snapshot().toString());
        } catch (Exception e) {
            LOG.error("Fatal error occurred during emulation", e);
        }
    }
}
