package it.lycoris.lycoscript.compiler;

import it.lycoris.lycoscript.compiler.parser.LycoScriptLexer;
import it.lycoris.lycoscript.compiler.parser.LycoScriptParser;
import it.lycoris.lycoscript.compiler.semantic.CodeGenerator;
import it.lycoris.lycoscript.compiler.semantic.SemanticAnalyzer;
import it.lycoris.lycoscript.compiler.semantic.SymbolTable;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Facade service for the LycoScript compiler.
 * Orchestrates the full compilation pipeline from source code to 6502 Assembly.
 * Completely agnostic of the execution environment (CLI, GUI, or IDE Plugin).
 */
public class LycoCompilerService {

    /**
     * Compiles LycoScript source code into 6502 Assembly.
     *
     * @param sourceCode The raw LycoScript code.
     * @return The generated 6502 Assembly string.
     */
    public String compile(String sourceCode) {
        // 1. Lexical Analysis
        LycoScriptLexer lexer = new LycoScriptLexer(CharStreams.fromString(sourceCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 2. Parsing (Syntax Analysis)
        LycoScriptParser parser = new LycoScriptParser(tokens);
        ParseTree tree = parser.program();

        // 3. Semantic Analysis (Type checking and Scope resolution)
        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(symbolTable);
        semanticAnalyzer.visit(tree);

        // 4. Code Generation (Backend)
        CodeGenerator codeGenerator = new CodeGenerator(symbolTable);
        codeGenerator.visit(tree);

        return codeGenerator.getGeneratedCode();
    }
}