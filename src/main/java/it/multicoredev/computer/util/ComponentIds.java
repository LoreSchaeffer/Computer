package it.multicoredev.computer.util;

public class ComponentIds {
    private static int id = 0;

    private ComponentIds() {
        throw new IllegalStateException("Utility class");
    }

    public static int nextId() {
        return id++;
    }
}
