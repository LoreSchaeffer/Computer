package it.lycoris.lycoscript.compiler;

import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import it.lycoris.lycoscript.compiler.parser.LycoScriptParser;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Command Line Interface entry point for the LycoScript Compiler.
 * Allows the compiler to be executed as a standalone standalone application.
 */
public class LycoScriptCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(LycoScriptCompiler.class);

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
            String sourceCode = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);

            LOG.info("Starting Lexical Analysis...");
            CharStream charStream = CharStreams.fromString(sourceCode);
            LycoScriptLexer lexer = new LycoScriptLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            LOG.info("Starting Syntax Analysis (Parsing)...");
            LycoScriptParser parser = new LycoScriptParser(tokens);

            ParseTree tree = parser.program();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                LOG.error("Compilation failed due to syntax errors in the source file.");
                System.exit(1);
            }

            LOG.info("Starting Code Generation (Backend)...");
            LycoCompilerVisitor visitor = new LycoCompilerVisitor();
            visitor.visit(tree);

            String compiledAssembly = visitor.getCompiledAssembly();

            LOG.info("Compilation successful. Writing output to: {}", outputFile.getName());
            Files.writeString(outputFile.toPath(), compiledAssembly, StandardCharsets.UTF_8);

            LOG.info("Process completed successfully.");
        } catch (IOException exception) {
            LOG.error("File I/O error occurred during compilation.", exception);
            System.exit(1);
        } catch (RuntimeException exception) {
            LOG.error("Semantic Error: {}", exception.getMessage());
            System.exit(1);
        } catch (Exception exception) {
            LOG.error("Compilation failed due to an internal error: {}", exception.getMessage(), exception);
            System.exit(1);
        }
    }
}