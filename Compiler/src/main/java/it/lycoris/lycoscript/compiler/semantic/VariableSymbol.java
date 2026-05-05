package it.lycoris.lycoscript.compiler.semantic;

public record VariableSymbol(
        String name,
        DataType type,
        int memoryAddress
) implements Symbol {
}
