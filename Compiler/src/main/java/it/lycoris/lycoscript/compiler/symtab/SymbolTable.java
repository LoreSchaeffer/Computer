package it.lycoris.lycoscript.compiler.symtab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> globals = new HashMap<>();
    private final Map<String, Map<String, Symbol>> localScopes = new HashMap<>();
    private final Map<String, String> functionLabels = new HashMap<>();
    private final Map<String, List<Symbol>> functionParameters = new HashMap<>();
    private final Map<String, StructInfo> definedStructs = new HashMap<>();
    private String currentFunctionScope = null;
    private int expressionStackPointer = 0x00;              // Virtual stack pointer for expression evaluation (0x00 to 0x0F)
    private int nextGlobalAddress = 0x0200;                 // General purpose global RAM allocator (Starts after the hardware stack)

    public Symbol defineGlobal(String name, SymbolType type, boolean isConst, int constValue, String structName) {
        if (globals.containsKey(name)) throw new RuntimeException("Compilation Error: Duplicate global symbol '" + name + "'");

        int size = (type == SymbolType.INT || type == SymbolType.POINTER) ? 2 : 1;
        int address = -1;

        if (!isConst) {
            address = nextGlobalAddress;
            nextGlobalAddress += size;

            // Bounds checking based on MemoryMap.RAM_SIZE (0x2000)
            if (nextGlobalAddress >= 0x2000) throw new RuntimeException("Compilation Error: Out of RAM! Cannot allocate variable '" + name + "'");
        }

        Symbol symbol = new Symbol(name, type, address, isConst, constValue, structName, 1);
        globals.put(name, symbol);
        return symbol;
    }

    public Symbol defineConstant(String name, SymbolType type, int value) {
        return defineGlobal(name, type, true, value, null);
    }

    public Symbol defineLocal(String name, SymbolType type) {
        if (currentFunctionScope == null) throw new RuntimeException("Compilation Error: Cannot define local variable outside a function.");

        Map<String, Symbol> currentScope = localScopes.get(currentFunctionScope);
        if (currentScope.containsKey(name) || globals.containsKey(name)) throw new RuntimeException("Compilation Error: Variable '" + name + "' already defined in this scope.");

        int size = (type == SymbolType.INT || type == SymbolType.POINTER) ? 2 : 1;
        int address = nextGlobalAddress;
        nextGlobalAddress += size;

        if (nextGlobalAddress >= 0x2000) throw new RuntimeException("Compilation Error: Out of RAM!");

        Symbol symbol = new Symbol(name, type, address, false, 0, null, 1);
        currentScope.put(name, symbol);
        return symbol;
    }

    public void defineFunction(String functionName, String assemblyLabel) {
        if (functionLabels.containsKey(functionName)) throw new RuntimeException("Compilation Error: Function '" + functionName + "' already defined.");

        functionLabels.put(functionName, assemblyLabel);
        localScopes.put(functionName, new HashMap<>());
        functionParameters.put(functionName, new java.util.ArrayList<>());
    }

    public Symbol defineParameter(String functionName, String paramName, SymbolType type) {
        Map<String, Symbol> scope = localScopes.get(functionName);
        if (scope.containsKey(paramName)) throw new RuntimeException("Compilation Error: Parameter '" + paramName + "' already defined.");

        int size = (type == SymbolType.INT || type == SymbolType.POINTER) ? 2 : 1;
        int address = nextGlobalAddress;
        nextGlobalAddress += size;

        Symbol symbol = new Symbol(paramName, type, address, false, 0, null, 1);
        scope.put(paramName, symbol);
        functionParameters.get(functionName).add(symbol);
        return symbol;
    }

    public void defineStruct(StructInfo structInfo) {
        if (definedStructs.containsKey(structInfo.getName())) throw new RuntimeException("Compilation Error: Struct '" + structInfo.getName() + "' already defined.");

        definedStructs.put(structInfo.getName(), structInfo);
    }

    public Symbol defineGlobalArray(String name, SymbolType type, int length) {
        int typeSize = (type == SymbolType.INT || type == SymbolType.POINTER) ? 2 : 1;
        int totalSize = typeSize * length;

        int address = nextGlobalAddress;
        nextGlobalAddress += totalSize;

        if (nextGlobalAddress >= 0x2000) throw new RuntimeException("Compilation Error: Out of RAM!");

        Symbol symbol = new Symbol(name, type, address, false, 0, null, length);
        globals.put(name, symbol);
        return symbol;
    }

    public Symbol defineLocalArray(String name, SymbolType type, int length) {
        if (currentFunctionScope == null) throw new RuntimeException("Compilation Error: Cannot define local outside function.");
        Map<String, Symbol> currentScope = localScopes.get(currentFunctionScope);

        int typeSize = (type == SymbolType.INT || type == SymbolType.POINTER) ? 2 : 1;
        int totalSize = typeSize * length;

        int address = nextGlobalAddress;
        nextGlobalAddress += totalSize;

        if (nextGlobalAddress >= 0x2000) throw new RuntimeException("Compilation Error: Out of RAM!");

        Symbol symbol = new Symbol(name, type, address, false, 0, null, length);
        currentScope.put(name, symbol);
        return symbol;
    }

    public void enterFunctionScope(String functionName) {
        this.currentFunctionScope = functionName;
    }

    public void exitFunctionScope() {
        this.currentFunctionScope = null;
    }

    public String getFunctionLabel(String functionName) {
        return functionLabels.get(functionName);
    }

    public List<Symbol> getFunctionParameters(String functionName) {
        return functionParameters.get(functionName);
    }

    public StructInfo getStruct(String name) {
        return definedStructs.get(name);
    }

    public Symbol getSymbol(String name) {
        if (currentFunctionScope != null) {
            Map<String, Symbol> currentScope = localScopes.get(currentFunctionScope);
            if (currentScope.containsKey(name)) return currentScope.get(name);
        }

        return globals.get(name);
    }

    // --- Virtual Expression Stack Management ---

    public int pushExpressionTemp() {
        if (expressionStackPointer > 0x0F) throw new RuntimeException("Compilation Error: Expression too complex. Expression stack overflow.");

        int current = expressionStackPointer;
        expressionStackPointer++;
        return current;
    }

    public void popExpressionTemp() {
        if (expressionStackPointer > 0) expressionStackPointer--;
    }
}
