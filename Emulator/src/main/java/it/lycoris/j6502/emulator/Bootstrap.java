package it.lycoris.j6502.emulator;

import ch.qos.logback.classic.Level;
import it.lycoris.j6502.emulator.emulated.EmulatorRunner;
import it.lycoris.j6502.emulator.hardware.io.ComponentLibrary;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Bootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

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

            try {
                ComponentLibrary lib = ComponentLibrary.get();
                lib.loadFromResources("hardware");
            } catch (Exception e) {
                LOG.error("Initialization of ComponentLibrary failed!", e);
                System.exit(1);
            }

            EmulatorRunner runner = new EmulatorRunner(startAddress, maxSteps);
            runner.loadProgramFromFileAndRun(binFile);
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
}
