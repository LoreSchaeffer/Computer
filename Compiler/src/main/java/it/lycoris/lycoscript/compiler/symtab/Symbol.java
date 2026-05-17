package it.lycoris.lycoscript.compiler.symtab;

public class Symbol {
    private final String name;
    private final SymbolType type;
    private final int address;
    private final boolean isConst;
    private final int constValue;
    private final String structName;
    private final int arrayLength;

    public Symbol(String name, SymbolType type, int address, boolean isConst, int constValue, String structName, int arrayLength) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.isConst = isConst;
        this.constValue = constValue;
        this.structName = structName;
        this.arrayLength = arrayLength;
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public int getAddress() {
        return address;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getConstValue() {
        return constValue;
    }

    public String getStructName() {
        return structName;
    }

    public int getArrayLength() {
        return arrayLength;
    }
}