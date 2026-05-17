package it.lycoris.j6502.emulator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import it.lycoris.j6502.emulator.core.InstructionSet;
import it.lycoris.j6502.emulator.core.Motherboard;
import it.lycoris.j6502.emulator.core.PrecisionEmulatorLoop;
import it.lycoris.j6502.emulator.ui.Display;
import it.lycoris.j6502.emulator.ui.HostGamepadPoller;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Bootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
    private static final String DEFAULT_ROM_FILENAME = "rom.bin";
    private static final int CPU_FREQUENCY_HZ = 1_000_000;
    private static final int TARGET_FPS = 60;

    static void main(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> helpOpt = parser.acceptsAll(List.of("help", "h"), "Show this help message").forHelp();
        OptionSpec<File> inputOpt = parser.acceptsAll(List.of("input", "i"), "The input .bin file to emulate").withRequiredArg().ofType(File.class);
        OptionSpec<String> originOpt = parser.acceptsAll(List.of("origin", "o"), "Start address in hex (default: 8000)").withOptionalArg().defaultsTo("8000");
        OptionSpec<Integer> freqOpt = parser.acceptsAll(List.of("freq", "f"), "Target Frequency in Hz (e.g. 20000). Default: 0 (Uncapped)").withOptionalArg().ofType(Integer.class).defaultsTo(0);
        OptionSpec<Motherboard.EmulationMode> emulationModeOpt = parser.acceptsAll(List.of("mode", "m"), "Select the emulation mode (GATE_LEVEL or HIGH_LEVEL)").withRequiredArg().ofType(Motherboard.EmulationMode.class).defaultsTo(Motherboard.EmulationMode.HIGH_LEVEL);
        OptionSpec<Void> instructionSetOpt = parser.acceptsAll(List.of("instructions", "s"), "Print a table with all supported instructions");
        OptionSpec<Integer> debugOpt = parser.acceptsAll(List.of("debug", "d"), "Launch debug mode").withOptionalArg().ofType(Integer.class).defaultsTo(0);

        try {
            OptionSet options = parser.parse(args);

            if (options.has(helpOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            if (options.has(instructionSetOpt)) {
                InstructionSet instructionSet = new InstructionSet();
                instructionSet.printOpcodeMatrix();
                return;
            }

            boolean debug = options.has(debugOpt);
            int debugLevel = options.has(debugOpt) ? options.valueOf(debugOpt) : -1;
            int targetFrequency = options.valueOf(freqOpt);
            int origin = 0x8000;
            Motherboard.EmulationMode emulationMode = options.valueOf(emulationModeOpt);

            try {
                origin = Integer.parseInt(options.valueOf(originOpt), 16);
            } catch (NumberFormatException ignored) {
                LOG.error("Invalid origin address provided. Must be a valid hexadecimal number. Defaulting to 0x8000.");
                System.exit(1);
            }

            if (debug) {
                LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
                context.getLogger("it.lycoris.j6502.emulator").setLevel(Level.DEBUG);
            }

            byte[] program;
            if (options.has(inputOpt)) {
                File binFile = options.valueOf(inputOpt);

                if (!binFile.exists() || !binFile.isFile()) {
                    LOG.error("ROM file not found: {}", binFile.getAbsolutePath());
                    System.exit(1);
                    return;
                }

                program = Files.readAllBytes(binFile.toPath());
            } else {
                File defaultRom = new File(DEFAULT_ROM_FILENAME);

                if (defaultRom.exists() && defaultRom.isFile()) {
                    LOG.info("No input file provided. Automatically loading default ROM: {}", DEFAULT_ROM_FILENAME);
                    program = Files.readAllBytes(defaultRom.toPath());
                } else {
                    LOG.error("No input file provided and '{}' not found in the current directory.", DEFAULT_ROM_FILENAME);
                    parser.printHelpOn(System.out);
                    System.exit(1);
                    return;
                }
            }

            int[] unsignedProgram = new int[program.length];
            for (int i = 0; i < program.length; i++) {
                unsignedProgram[i] = program[i] & 0xFF;
            }

            Motherboard motherboard = new Motherboard(emulationMode, debugLevel, unsignedProgram);
            motherboard.cpu().reset();

            Display window = new Display(motherboard.ppu(), motherboard.keyboard());
            SwingUtilities.invokeLater(() -> window.setVisible(true));

            HostGamepadPoller gamepadPoller = new HostGamepadPoller(motherboard.joypad());
            gamepadPoller.start();
            Runtime.getRuntime().addShutdownHook(new Thread(gamepadPoller::stop));

            PrecisionEmulatorLoop loop = new PrecisionEmulatorLoop(
                    motherboard,
                    window::renderFrame,
                    window::updateTitleWithFps,
                    TARGET_FPS,
                    CPU_FREQUENCY_HZ,
                    debugLevel
            );
            loop.start();
        } catch (OptionException e) {
            LOG.error("Invalid arguments: {}", e.getMessage());
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ioException) {
                LOG.error("Failed to print help", ioException);
            }
            System.exit(1);
        } catch (Exception e) {
            LOG.error("A fatal error occurred during bootstrap", e);
            System.exit(1);
        }
    }
}