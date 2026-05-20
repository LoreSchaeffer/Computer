package it.lycoris.lycoscript.compiler;

import it.lycoris.lycoscript.compiler.lsp.LycoLanguageServer;
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
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
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
        OptionSpec<File> inputOpt = parser.acceptsAll(List.of("input", "i"), "The input .ls file to compile").withRequiredArg().ofType(File.class);
        OptionSpec<File> outputOpt = parser.acceptsAll(List.of("output", "o"), "The output .asm file (defaults to out.asm)").withRequiredArg().ofType(File.class).defaultsTo(new File("out.asm"));
        OptionSpec<String> initOpt = parser.acceptsAll(List.of("init"), "Initializes a new LycoScript project workspace").withRequiredArg().ofType(String.class);
        OptionSpec<Void> lspOpt = parser.acceptsAll(List.of("lsp"), "Starts the LycoScript Language Server via Standard I/O");

        try {
            OptionSet options = parser.parse(args);

            if (options.has(helpOpt)) {
                parser.printHelpOn(System.out);
                return;
            }

            if (options.has(lspOpt)) {
                startLanguageServer();
                return;
            }

            if (options.has(initOpt)) {
                String projectName = options.valueOf(initOpt);
                ProjectScaffolder.createNewProject(projectName);
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
            LOG.info("Reading and Pre-processing source file: {}", inputFile.getName());

            String resolvedSourceCode = processFileImports(inputFile);

            LOG.info("Starting Lexical Analysis...");
            CharStream charStream = CharStreams.fromString(resolvedSourceCode);
            LycoScriptLexer lexer = new LycoScriptLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            LOG.info("Starting Syntax Analysis (Parsing)...");
            LycoScriptParser parser = new LycoScriptParser(tokens);
            ParseTree tree = parser.program();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                LOG.error("Compilation failed due to syntax errors.");
                System.exit(1);
            }

            LOG.info("Starting Code Generation (Backend)...");
            LycoCompilerVisitor visitor = new LycoCompilerVisitor();
            visitor.visit(tree);

            String compiledAssembly = visitor.getCompiledAssembly();

            LOG.info("Compilation successful. Writing output to: {}", outputFile.getName());
            Files.writeString(outputFile.toPath(), compiledAssembly, StandardCharsets.UTF_8);
            LOG.info("Process completed successfully.");
        } catch (Exception exception) {
            LOG.error("Compilation failed: {}", exception.getMessage(), exception);
            System.exit(1);
        }
    }

    /**
     * Entry point for the recursive preprocessor.
     *
     * @param sourceFile The source .ls file to process.
     */
    private static String processFileImports(File sourceFile) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile.toPath(), StandardCharsets.UTF_8);
        File currentDir = sourceFile.getParentFile();
        if (currentDir == null) currentDir = new File(".");

        File projectRoot = currentDir.getName().equals("src") ? currentDir.getParentFile() : currentDir;

        return resolveImports(lines, currentDir, projectRoot);
    }

    /**
     * Recursively parses lines looking for "import" statements.
     * Handles TypeScript-style relative/global resolution.
     *
     * @param lines       The lines of the source file to process.
     * @param currentDir  The directory of the current file being processed (used for relative imports).
     * @param projectRoot The root directory of the project (used for resolving non-relative imports).
     */
    private static String resolveImports(List<String> lines, File currentDir, File projectRoot) throws IOException {
        StringBuilder finalSource = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("import") && trimmed.endsWith(";")) {
                int firstQuote = trimmed.indexOf('"');
                int lastQuote = trimmed.lastIndexOf('"');

                if (firstQuote != -1 && lastQuote > firstQuote) {
                    String importPath = trimmed.substring(firstQuote + 1, lastQuote);
                    String headerContent = null;
                    File targetFile;

                    boolean isRelative = importPath.startsWith("./") || importPath.startsWith("../");

                    if (isRelative) {
                        targetFile = new File(currentDir, importPath).getCanonicalFile();
                    } else {
                        targetFile = new File(currentDir, importPath);
                        if (!targetFile.exists()) targetFile = new File(projectRoot, "stdlib/headers/" + importPath);
                    }

                    if (targetFile.exists()) {
                        LOG.info("Resolving import: {}", targetFile.getAbsolutePath());
                        List<String> headerLines = Files.readAllLines(targetFile.toPath(), StandardCharsets.UTF_8);

                        headerContent = resolveImports(headerLines, targetFile.getParentFile(), projectRoot);
                    } else if (!isRelative) {
                        String resourcePath = "/stdlib/headers/" + importPath;
                        try (java.io.InputStream is = LycoScriptCompiler.class.getResourceAsStream(resourcePath)) {
                            if (is != null) {
                                LOG.info("Resolving internal STDLIB import: {}", importPath);
                                String rawContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                                List<String> headerLines = java.util.Arrays.asList(rawContent.split("\\r?\\n"));

                                headerContent = resolveImports(headerLines, currentDir, projectRoot);
                            }
                        }
                    }

                    if (headerContent == null) throw new RuntimeException("Import Error: Could not resolve -> " + importPath);

                    finalSource.append(headerContent).append("\n");
                }
            } else {
                finalSource.append(line).append("\n");
            }
        }

        return finalSource.toString();
    }

    private static void startLanguageServer() {
        try {
            LycoLanguageServer server = new LycoLanguageServer();
            LSPLauncher.Builder<LanguageClient> launcherBuilder = new LSPLauncher.Builder<>();

            Launcher<LanguageClient> launcher = launcherBuilder
                    .setLocalService(server)
                    .setRemoteInterface(LanguageClient.class)
                    .setInput(System.in)
                    .setOutput(System.out)
                    .create();

            server.connect(launcher.getRemoteProxy());
            launcher.startListening().get();
        } catch (Exception e) {
            LOG.error("Failed to start Language Server", e);
            System.exit(1);
        }
    }
}