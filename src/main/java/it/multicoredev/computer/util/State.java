package it.multicoredev.computer.util;

import it.multicoredev.computer.constants.Colors;

import java.awt.*;

public enum State {
    HIGH,
    LOW,
    FLOATING;

    public State toggle() {
        return switch (this) {
            case HIGH -> LOW;
            case LOW -> HIGH;
            default -> FLOATING;
        };
    }

    public Boolean toBoolean() {
        return switch (this) {
            case HIGH -> true;
            case LOW -> false;
            default -> null;
        };
    }

    public char toChar() {
        return switch (this) {
            case HIGH -> '1';
            case LOW -> '0';
            default -> 'f';
        };
    }

    public int toInt() {
        return switch (this) {
            case HIGH -> 1;
            case LOW -> 0;
            default -> 2;
        };
    }

    public String toString() {
        return switch (this) {
            case HIGH -> "1";
            case LOW -> "0";
            default -> "f";
        };
    }

    public Color toColor() {
        return switch (this) {
            case HIGH -> Colors.GREEN;
            case LOW -> Colors.RED;
            case FLOATING -> Colors.YELLOW;
        };
    }

    public static State fromBoolean(Boolean b) {
        if (b == null) return FLOATING;
        return b ? HIGH : LOW;
    }

    public static State fromChar(char c) {
        if (c != '1' && c != '0' && c != 'f') throw new IllegalArgumentException("Invalid character: " + c);
        return switch (c) {
            case '1' -> HIGH;
            case '0' -> LOW;
            default -> FLOATING;
        };
    }

    public static State fromInt(Integer i) {
        if (i == null) return FLOATING;
        return switch (i) {
            case 1 -> HIGH;
            case 0 -> LOW;
            default -> FLOATING;
        };
    }

    public static State[] fromString(String s) {
        State[] states = new State[s.length()];
        for (int i = 0; i < s.length(); i++) {
            states[i] = fromChar(s.charAt(i));
        }

        return states;
    }

    public static String toString(State... states) {
        StringBuilder sb = new StringBuilder();
        for (State state : states) {
            sb.append(state.toChar());
        }

        return sb.toString();
    }
}
