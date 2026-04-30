package it.lycoris.lycoscript.compiler.semantic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static final Logger LOG = LoggerFactory.getLogger(SymbolTable.class);
    private final Map<String, Symbol> globalScope = new HashMap<>();

    public SymbolTable() {
        this.injectStandardLibrary();
    }

    /**
     * Pre-populates the symbol table with system constants and intrinsic functions.
     * This acts as the "LycoLib" standard library for the language.
     */
    private void injectStandardLibrary() {
        LOG.info("Injecting LycoLib standard library into global scope...");

        // System Constants
        this.define(new ConstantSymbol("SCREEN", DataType.POINTER, 0x2000));
        this.define(new ConstantSymbol("KEYBOARD", DataType.POINTER, 0x4000));
        this.define(new ConstantSymbol("APU_PULSE", DataType.POINTER, 0x5000));

        // Intrinsic Functions
        this.define(new FunctionSymbol("print", DataType.VOID));
        this.define(new FunctionSymbol("delay", DataType.VOID));
    }

    /**
     * Registers a new symbol in the current scope.
     *
     * @param symbol The symbol to register.
     * @throws IllegalStateException if a symbol with the same name already exists.
     */
    public void define(Symbol symbol) {
        if (this.globalScope.containsKey(symbol.name())) {
            throw new IllegalStateException("Compilation Error: Symbol '" + symbol.name() + "' is already defined.");
        }
        this.globalScope.put(symbol.name(), symbol);
        LOG.debug("Defined symbol: {} [{}]", symbol.name(), symbol.getClass().getSimpleName());
    }

    /**
     * Resolves a symbol by its identifier name.
     *
     * @param name The name of the identifier to look up.
     * @return The resolved Symbol.
     * @throws IllegalArgumentException if the symbol is not found.
     */
    public Symbol resolve(String name) {
        Symbol symbol = this.globalScope.get(name);
        if (symbol == null) {
            throw new IllegalArgumentException("Compilation Error: Undefined symbol '" + name + "'.");
        }
        return symbol;
    }
}
