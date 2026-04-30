package it.lycoris.lycoscript.compiler.semantic;

public record ConstantSymbol(
        String name,
        DataType type,
        int value
) implements Symbol {
}
