package it.lycoris.lycoscript.compiler.symtab;

import java.util.LinkedHashMap;
import java.util.Map;

public class StructInfo {
    private final String name;
    private final Map<String, Integer> fieldOffsets = new LinkedHashMap<>();
    private final Map<String, SymbolType> fieldTypes = new LinkedHashMap<>();
    private int totalSize = 0;

    public StructInfo(String name) {
        this.name = name;
    }

    public void addField(String fieldName, SymbolType type, int arrayLength) {
        fieldOffsets.put(fieldName, totalSize);
        fieldTypes.put(fieldName, type);

        int typeSize = (type == SymbolType.INT || type == SymbolType.POINTER) ? 2 : 1;
        totalSize += (typeSize * Math.max(1, arrayLength));
    }

    public int getFieldOffset(String fieldName) {
        return fieldOffsets.getOrDefault(fieldName, -1);
    }

    public SymbolType getFieldType(String fieldName) {
        return fieldTypes.get(fieldName);
    }

    public int getTotalSize() {
        return totalSize;
    }

    public String getName() {
        return name;
    }
}
