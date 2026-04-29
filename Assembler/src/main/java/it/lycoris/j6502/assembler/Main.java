package it.lycoris.j6502.assembler;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    static void main(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> helpOpt = parser.acceptsAll(List.of("help", "h"), "Shows this help menu").forHelp();
        OptionSpec<File> inputOpt = parser.acceptsAll(List.of("input", "i"), "The input .asm file to assemble").withRequiredArg().ofType(File.class);
        OptionSpec<File> outputOpt = parser.acceptsAll(List.of("output", "o"), "The output .bin file (defaults to out.bin)").withRequiredArg().ofType(File.class).defaultsTo(new File("out.bin"));

        try {
            OptionSet options = parser.parse(args);

            if (options.has(helpOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            if (!options.has(inputOpt)) {
                launchGuiMode();
            } else {
                File inputFile = options.valueOf(inputOpt);
                File outputFile = options.valueOf(outputOpt);
                launchCliMode(inputFile, outputFile);
            }
        } catch (OptionException e) {
            LOG.error("Invalid arguments: {}", e.getMessage());

            try {
                parser.printHelpOn(System.err);
            } catch (IOException ioException) {
                LOG.error("Failed to print help", ioException);
            }

            System.exit(1);
        } catch (Exception e) {
            LOG.error("A fatal error occurred", e);
            System.exit(1);
        }
    }

    private static void launchCliMode(File inputFile, File outputFile) {
        LOG.info("Launching in CLI mode...");

        // TODO
    }

    private static void launchGuiMode() {
        LOG.info("Launching in GUI...");

        try {
            Lexer lexer = new Lexer();
            List<TokenLine> parsedLines = lexer.tokenizeFile(new File("test.asm"));

            Assembler assembler = new Assembler();
            assembler.pass1(parsedLines);
            assembler.pass2(parsedLines, new File("test.bin"));
        } catch (IOException e) {
            LOG.error("Errore durante la lettura del file sorgente", e);
        }

        // TODO To be implemented later
    }
}
