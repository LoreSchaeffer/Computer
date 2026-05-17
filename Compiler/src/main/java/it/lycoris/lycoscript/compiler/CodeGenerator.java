package it.lycoris.lycoscript.compiler;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    private final List<String> instructions = new ArrayList<>();
    private int labelCounter = 0;
    private final List<String> rodata = new ArrayList<>();
    private int stringCounter = 0;

    public void emit(String instruction) {
        instructions.add("    " + instruction);
    }

    public void emitComment(String comment) {
        instructions.add("; " + comment);
    }

    public void emitLabel(String label) {
        instructions.add(label + ":");
    }

    public String createLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    public String addStringLiteral(String value) {
        String label = "STR_" + (stringCounter++);
        rodata.add(label + ": .asciiz \"" + value + "\"");
        return label;
    }

    // --- Subroutines (Functions) ---

    public void emitJumpToSubroutine(String label) {
        emit("JSR " + label);
    }

    public void emitReturnFromSubroutine() {
        emit("RTS");
    }

    // --- Expression Stack Macros ---

    public void storeToTemp(int zeroPageAddress) {
        emit(String.format("STA $%02X", zeroPageAddress));
    }

    public void loadFromTemp(int zeroPageAddress) {
        emit(String.format("LDA $%02X", zeroPageAddress));
    }

    // --- Load and Store Operations ---

    public void loadAccumulatorImmediate(int value) {
        emit(String.format("LDA #$%02X", value & 0xFF));
    }

    public void loadAccumulatorAbsolute(int address) {
        emit(String.format("LDA $%04X", address & 0xFFFF));
    }

    public void storeAccumulatorAbsolute(int address) {
        emit(String.format("STA $%04X", address & 0xFFFF));
    }

    // --- Math Operations ---

    public void addTempToAccumulator(int zeroPageAddress) {
        emit("CLC");
        emit(String.format("ADC $%02X", zeroPageAddress));
    }

    public void subtractTempFromAccumulator(int zeroPageAddress) {
        emit("SEC");
        emit(String.format("SBC $%02X", zeroPageAddress));
    }

    // --- Bitwise Operations ---

    public void bitwiseAndTemp(int zeroPageAddress) {
        emit(String.format("AND $%02X", zeroPageAddress));
    }

    public void bitwiseOrTemp(int zeroPageAddress) {
        emit(String.format("ORA $%02X", zeroPageAddress));
    }

    public void bitwiseXorTemp(int zeroPageAddress) {
        emit(String.format("EOR $%02X", zeroPageAddress));
    }

    public void bitwiseNotAccumulator() {
        emit("EOR #$FF");
    }

    public void shiftLeftAccumulator() {
        emit("ASL A");
    }

    public void shiftRightAccumulator() {
        emit("LSR A");
    }

    public void pushAccumulatorToHardwareStack() {
        emit("PHA");
    }

    public void pullAccumulatorFromHardwareStack() {
        emit("PLA");
    }

    // --- Control Flow & Comparisons ---

    public void compareAccumulatorImmediate(int value) {
        emit(String.format("CMP #$%02X", value & 0xFF));
    }

    public void branchIfEqual(String label) {
        emit("BEQ " + label);
    }

    public void branchIfNotEqual(String label) {
        emit("BNE " + label);
    }

    public void branchIfCarryClear(String label) {
        emit("BCC " + label);
    }

    public void branchIfCarrySet(String label) {
        emit("BCS " + label);
    }

    public void jump(String label) {
        emit("JMP " + label);
    }

    // --- Array Indexing and Register Y ---

    public void transferAccumulatorToY() {
        emit("TAY");
    }

    public void loadAccumulatorAbsoluteIndexedY(int address) {
        emit(String.format("LDA $%04X, Y", address & 0xFFFF));
    }

    public void storeAccumulatorAbsoluteIndexedY(int address) {
        emit(String.format("STA $%04X, Y", address & 0xFFFF));
    }

    // --- Pointer Indirection (Zero Page $10-$11 is reserved for pointers) ---

    public void loadIndirectY(int zeroPagePtr) {
        emit(String.format("LDA ($%02X), Y", zeroPagePtr & 0xFF));
    }

    public void storeIndirectY(int zeroPagePtr) {
        emit(String.format("STA ($%02X), Y", zeroPagePtr & 0xFF));
    }

    // --- Final Assembly Output ---

    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\n", instructions)).append("\n\n");

        if (!rodata.isEmpty()) {
            sb.append(".segment \"RODATA\"\n");
            for (String s : rodata) {
                sb.append("    ").append(s).append("\n");
            }
        }
        return sb.toString();
    }
}
