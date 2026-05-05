package it.lycoris.lycoscript.compiler.semantic;

public sealed interface Symbol permits VariableSymbol, ConstantSymbol, FunctionSymbol {
    String name();
    DataType type();
}
