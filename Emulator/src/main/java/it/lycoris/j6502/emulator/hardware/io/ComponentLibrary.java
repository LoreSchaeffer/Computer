package it.lycoris.j6502.emulator.hardware.io;

import com.google.gson.Gson;
import it.lycoris.j6502.emulator.hardware.*;
import it.lycoris.j6502.emulator.hardware.*;
import it.lycoris.j6502.emulator.hardware.io.dto.ChipDefinition;
import it.lycoris.j6502.emulator.hardware.io.dto.ComponentDefinition;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ComponentLibrary acts as a central registry and factory for all hardware components.
 * It is responsible for loading JSON chip definitions from system resources,
 * validating the integrity of the hardware graph (preventing circular dependencies),
 * and recursively building the physical circuit from its definitions.
 */
public final class ComponentLibrary {
    private static final List<String> PRIMITIVES = List.of("AndGate", "NandGate", "NotGate", "OrGate", "NorGate", "XorGate", "XnorGate", "DLatch", "VCC", "GND");
    private static final Gson GSON = new Gson();
    private final Map<String, ChipDefinition> registry = new HashMap<>();

    /**
     * Enumeration used to track traversal state during cycle detection.
     */
    private enum Mark {
        VISITING,
        VISITED
    }

    /**
     * Scans a specific folder within the application's resources for JSON hardware definitions.
     * Unlike standard file-system loading, this method works within JAR files by
     * accessing the ClassLoader's resource stream.
     *
     * @param resourceFolder The path to the hardware folder in resources (e.g., "hardware").
     * @throws Exception If the folder is missing or if JSON parsing fails.
     */
    public void loadFromResources(String resourceFolder) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream in = loader.getResourceAsStream(resourceFolder)) {
            if (in == null) throw new IllegalArgumentException("Resource folder not found: " + resourceFolder);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                List<String> jsonFiles = reader.lines()
                        .filter(line -> line.toLowerCase().endsWith(".json"))
                        .toList();

                for (String fileName : jsonFiles) {
                    String fullPath = resourceFolder + "/" + fileName;
                    try (InputStream jsonStream = loader.getResourceAsStream(fullPath)) {
                        if (jsonStream == null) throw new FileNotFoundException("Resource not found: " + fullPath);

                        try (Reader jsonReader = new InputStreamReader(jsonStream, StandardCharsets.UTF_8)) {
                            ChipDefinition def = GSON.fromJson(jsonReader, ChipDefinition.class);
                            registry.put(def.chipName(), def);
                        }
                    }
                }
            }
        }

        validateDependencyGraph();
    }

    /**
     * Instantiates a high-level chip and connects its top-level pins.
     * * @param type The type of the chip (must exist in the registry).
     *
     * @param name    The instance name for the chip.
     * @param inputs  Map of top-level input wires.
     * @param outputs Map of top-level output wires.
     * @return A fully wired LogicComponent ready for simulation.
     */
    public LogicComponent build(String type, String name, Map<String, Wire> inputs, Map<String, Wire> outputs) {
        if (isPrimitive(type)) {
            return buildPrimitive(type, name, inputs, outputs);
        }

        ChipDefinition def = registry.get(type);
        if (def == null) {
            throw new IllegalArgumentException("Unknown component type: " + type);
        }

        return buildInternal(def, name, inputs, outputs);
    }

    /**
     * Recursively builds the internal structure of a complex chip.
     * It instantiates all internal components and bundles them into the final ComplexChip.
     *
     * @param def             The definition blueprint of the chip.
     * @param name            The unique instance name.
     * @param externalInputs  Wires connected to the chip's input pins.
     * @param externalOutputs Wires connected to the chip's output pins.
     * @return An instance of ComplexChip with all internal components wired.
     */
    private ComplexChip buildInternal(ChipDefinition def, String name, Map<String, Wire> externalInputs, Map<String, Wire> externalOutputs) {
        Map<String, Wire> internalWires = new HashMap<>();

        // 1. Create internal wire signals defined in the JSON
        if (def.internalWires() != null) {
            for (String wireName : def.internalWires()) {
                internalWires.put(wireName, new Wire());
            }
        }

        // List to hold the instantiated sub-components
        List<LogicComponent> internalComponentsList = new ArrayList<>();

        // 2. Instantiate and wire internal sub-components
        for (ComponentDefinition compDef : def.components()) {
            Map<String, Wire> compInputs = new HashMap<>();
            Map<String, Wire> compOutputs = new HashMap<>();

            // Map sub-component inputs to existing wires
            compDef.inputs().forEach((pin, wireName) -> {
                Wire w = externalInputs.get(wireName);
                if (w == null) w = internalWires.get(wireName);
                if (w == null) w = externalOutputs.get(wireName);

                if (w == null) throw new IllegalStateException(
                        "Unconnected input pin '" + pin + "' in component " + name + " (Wire: " + wireName + ")"
                );
                compInputs.put(pin, w);
            });

            // Map sub-component outputs to existing wires
            compDef.outputs().forEach((pin, wireList) -> {
                if (wireList == null || wireList.isEmpty()) return;

                // Since the DTO guarantees it's a List, we safely extract the first wire
                String wireName = String.valueOf(wireList.getFirst()).trim();

                if (wireName.isEmpty()) return;

                Wire w = externalOutputs.get(wireName);
                if (w == null) w = internalWires.get(wireName);
                if (w == null) w = externalInputs.get(wireName);

                // Auto-vivification: Create a floating wire if it wasn't officially declared
                if (w == null) {
                    w = new Wire();
                    internalWires.put(wireName, w);
                }

                compOutputs.put(pin, w);
            });

            // Recursive call to build the sub-component
            LogicComponent subComp = build(compDef.type(), compDef.name(), compInputs, compOutputs);
            internalComponentsList.add(subComp);
        }

        // 3. Convert maps and lists to arrays required by ComplexChip constructor
        Wire[] inputsArray = externalInputs.values().toArray(new Wire[0]);
        Wire[] outputsArray = externalOutputs.values().toArray(new Wire[0]);
        LogicComponent[] componentsArray = internalComponentsList.toArray(new LogicComponent[0]);

        return new ComplexChip(name, inputsArray, outputsArray, componentsArray);
    }

    /**
     * Factory method for atomic logic gates (Primitives).
     * Maps the flexible Map representations into the strict discrete Wire arguments expected by the gate constructors.
     */
    private LogicComponent buildPrimitive(String type, String name, Map<String, Wire> inputs, Map<String, Wire> outputs) {
        Wire out = outputs.getOrDefault("Out", outputs.getOrDefault("Out0", new Wire()));
        Wire[] inArr = inputs.values().toArray(new Wire[0]);

        Wire in0 = inArr.length > 0 ? inArr[0] : new Wire();
        Wire in1 = inArr.length > 1 ? inArr[1] : new Wire();

        return switch (type) {
            case "AndGate" -> new AndGate(name, in0, in1, out);
            case "NandGate" -> new NandGate(name, in0, in1, out);
            case "NotGate" -> new NotGate(name, in0, out);
            case "OrGate" -> new OrGate(name, in0, in1, out);
            case "NorGate" -> new NorGate(name, in0, in1, out);
            case "XorGate" -> new XorGate(name, in0, in1, out);
            case "XnorGate" -> new XnorGate(name, in0, in1, out);
            case "DLatch" -> new DLatch(
                    name,
                    inputs.getOrDefault("D", in0),
                    inputs.getOrDefault("Clk", in1),
                    outputs.getOrDefault("Q", outputs.getOrDefault("Out0", new Wire())),
                    outputs.getOrDefault("NotQ", outputs.getOrDefault("Out1", new Wire()))
            );
            case "VCC" -> new VccGate(name, out);
            case "GND" -> new GndGate(name, out);
            default -> throw new IllegalStateException("Unsupported primitive type: " + type);
        };
    }

    /**
     * Triggers a graph validation to prevent infinite recursion during chip instantiation.
     */
    private void validateDependencyGraph() {
        Map<String, Mark> marks = new HashMap<>();
        for (String chipName : registry.keySet()) {
            if (!marks.containsKey(chipName)) {
                checkCycles(chipName, marks);
            }
        }
    }

    /**
     * Depth-First Search (DFS) algorithm to detect cycles in the component hierarchy.
     */
    private void checkCycles(String chipName, Map<String, Mark> marks) {
        if (isPrimitive(chipName)) return;

        Mark mark = marks.get(chipName);
        if (mark == Mark.VISITING) throw new IllegalStateException("Circular hardware dependency detected: " + chipName);
        if (mark == Mark.VISITED) return;

        marks.put(chipName, Mark.VISITING);

        ChipDefinition def = registry.get(chipName);
        if (def == null) throw new IllegalArgumentException("Registry missing component: " + chipName);

        for (ComponentDefinition internal : def.components()) {
            checkCycles(internal.type(), marks);
        }

        marks.put(chipName, Mark.VISITED);
    }

    /**
     * Checks if a type is a hardcoded primitive gate.
     */
    private boolean isPrimitive(String type) {
        return PRIMITIVES.contains(type);
    }

    /**
     * Retrieves the loaded JSON blueprint definition for a specific chip type.
     *
     * @param type The name/type of the chip (e.g., "MOS6502", "ALU8Bit").
     * @return The ChipDefinition record containing its internal structure, or null if not found.
     */
    public ChipDefinition getDefinition(String type) {
        return registry.get(type);
    }
}