package it.lycoris.j6502.emulator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import it.lycoris.j6502.emulator.core.EmulatorRunner;
import it.lycoris.j6502.emulator.ui.LycoWindow;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Bootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
    private static final String DEFAULT_ROM_FILENAME = "rom.bin";

    static void main(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> helpOpt = parser.acceptsAll(List.of("help", "h"), "Show this help message").forHelp();
        OptionSpec<File> inputOpt = parser.acceptsAll(List.of("input", "i"), "The input .bin file to emulate").withRequiredArg().ofType(File.class);
        OptionSpec<String> originOpt = parser.acceptsAll(List.of("origin", "o"), "Start address in hex (default: 8000)").withOptionalArg().defaultsTo("8000");
        OptionSpec<Integer> stepsOpt = parser.acceptsAll(List.of("steps", "s"), "Max execution steps (default: infinite)").withOptionalArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<Integer> freqOpt = parser.acceptsAll(List.of("freq", "f"), "Target Frequency in Hz (e.g. 20000). Default: 0 (Uncapped)").withOptionalArg().ofType(Integer.class).defaultsTo(0);
        OptionSpec<Void> debugOpt = parser.acceptsAll(List.of("debug", "d"), "Launch debug mode");

        try {
            OptionSet options = parser.parse(args);

            if (options.has(helpOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            int startAddress = Integer.parseInt(options.valueOf(originOpt).replace("$", ""), 16);
            int maxSteps = options.valueOf(stepsOpt);
            boolean debug = options.has(debugOpt);
            int targetFrequency = options.valueOf(freqOpt);

            if (debug) {
                LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
                context.getLogger("it.lycoris.j6502.emulator").setLevel(Level.DEBUG);
            }

            EmulatorRunner runner = new EmulatorRunner(startAddress, maxSteps, targetFrequency);

            SwingUtilities.invokeLater(() -> {
                LycoWindow window = new LycoWindow(runner.getMotherboard().ppu(), runner.getMotherboard().keyboard());
                window.setVisible(true);
            });

            if (options.has(inputOpt)) {
                File binFile = options.valueOf(inputOpt);
                runner.loadProgramFromFileAndRun(binFile);
            } else {
                File defaultRom = new File(DEFAULT_ROM_FILENAME);
                if (defaultRom.exists() && defaultRom.isFile()) {
                    LOG.info("No input file provided. Automatically loading default ROM: {}", DEFAULT_ROM_FILENAME);
                    runner.loadProgramFromFileAndRun(defaultRom);
                } else {
                    LOG.error("No input file provided and '{}' not found in the current directory.", DEFAULT_ROM_FILENAME);
                    parser.printHelpOn(System.out);
                    System.exit(1);
                }
            }
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