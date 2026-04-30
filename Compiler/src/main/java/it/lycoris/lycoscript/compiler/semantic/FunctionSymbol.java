package it.lycoris.lycoscript.compiler.semantic;

public record FunctionSymbol(
        String name,
        DataType type
) implements Symbol {
}
