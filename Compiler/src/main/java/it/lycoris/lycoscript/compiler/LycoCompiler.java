package it.lycoris.lycoscript.compiler;

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

/**
 * Command Line Interface entry point for the LycoScript Compiler.
 * Allows the compiler to be executed as a standalone standalone application.
 */
public class LycoCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(LycoCompiler.class);

    static void main(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> helpOpt = parser.acceptsAll(List.of("help", "h"), "Shows this help menu").forHelp();
        OptionSpec<File> inputOpt = parser.acceptsAll(List.of("input", "i"), "The input .lyco file to compile").withRequiredArg().ofType(File.class);
        OptionSpec<File> outputOpt = parser.acceptsAll(List.of("output", "o"), "The output .asm file (defaults to out.asm)").withRequiredArg().ofType(File.class).defaultsTo(new File("out.asm"));

        try {
            OptionSet options = parser.parse(args);

            if (options.has(helpOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            if (!options.has(inputOpt) || !options.has(outputOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            File inputFile = options.valueOf(inputOpt);
            File outputFile = options.valueOf(outputOpt);
            launchCliMode(inputFile, outputFile);
        } catch (OptionException exception) {
            LOG.error("Invalid arguments: {}", exception.getMessage());

            try {
                parser.printHelpOn(System.err);
            } catch (IOException ioException) {
                LOG.error("Failed to print help", ioException);
            }

            System.exit(1);
        } catch (Exception exception) {
            LOG.error("A fatal error occurred", exception);
            System.exit(1);
        }
    }

    /**
     * Executes the compiler in Command Line mode.
     *
     * @param inputFile  The source LycoScript file.
     * @param outputFile The destination Assembly file.
     */
    private static void launchCliMode(File inputFile, File outputFile) {
        LOG.info("Launching in CLI mode...");

        if (!inputFile.exists()) {
            LOG.error("Input file not found: {}", inputFile.getAbsolutePath());
            System.exit(1);
        }

        try {
            LOG.info("Reading source file: {}", inputFile.getName());
            String sourceCode = Files.readString(inputFile.toPath());

            LOG.info("Starting compilation process...");
            LycoCompilerService compilerService = new LycoCompilerService();
            String compiledAssembly = compilerService.compile(sourceCode);

            LOG.info("Compilation successful. Writing output to: {}", outputFile.getName());
            Files.writeString(outputFile.toPath(), compiledAssembly);

            LOG.info("Process completed successfully.");

        } catch (IOException exception) {
            LOG.error("File I/O error occurred during compilation.", exception);
            System.exit(1);
        } catch (Exception exception) {
            LOG.error("Compilation failed due to an internal error: {}", exception.getMessage());
            System.exit(1);
        }
    }
}