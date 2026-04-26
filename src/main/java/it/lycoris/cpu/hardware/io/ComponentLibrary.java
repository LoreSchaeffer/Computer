package it.lycoris.cpu.hardware.io;

import com.google.gson.Gson;
import it.lycoris.cpu.hardware.*;
import it.lycoris.cpu.hardware.io.dto.ChipDefinition;
import it.lycoris.cpu.hardware.io.dto.ComponentDefinition;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ComponentLibrary {
    private static final List<String> PRIMITIVES = List.of("AndGate", "NandGate", "NotGate", "OrGate", "NorGate", "XorGate", "XnorGate", "DLatch", "VCC", "GND");
    private static final Gson GSON = new Gson();
    private final Map<String, ChipDefinition> registry = new HashMap<>();

    private enum Mark {
        VISITING,
        VISITED
    }

    public void loadDirectory(Path dirPath) throws Exception {
        try (Stream<Path> paths = Files.walk(dirPath)) {
            List<Path> jsonFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                    .toList();

            for (Path path : jsonFiles) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    ChipDefinition def = GSON.fromJson(reader, ChipDefinition.class);
                    this.registry.put(def.chipName(), def);
                }
            }

            this.validateDependencyGraph();
        }
    }

    public ChipDefinition getDefinition(String type) {
        return this.registry.get(type);
    }

    public LogicComponent build(String type, String instanceName, Wire[] inputs, Wire[] outputs) {
        if (this.isPrimitive(type)) {
            return this.buildPrimitive(type, instanceName, inputs, outputs);
        }

        ChipDefinition def = this.registry.get(type);
        if (def == null) throw new IllegalArgumentException("Chip definition not found for type: " + type);

        Map<String, Wire> contextWires = new HashMap<>();

        for (int i = 0; i < inputs.length; i++) {
            contextWires.put(def.pins().inputs().get(i), inputs[i]);
        }

        for (int i = 0; i < outputs.length; i++) {
            contextWires.put(def.pins().outputs().get(i), outputs[i]);
        }

        for (String wireName : def.internalWires()) {
            contextWires.put(wireName, new Wire());
        }

        LogicComponent[] internalComponents = new LogicComponent[def.components().size()];

        for (int i = 0; i < internalComponents.length; i++) {
            ComponentDefinition compDef = def.components().get(i);

            Wire[] compInputs = new Wire[compDef.inputs().size()];
            for (int j = 0; j < compDef.inputs().size(); j++) {
                compInputs[j] = contextWires.get(compDef.inputs().get(j));
            }

            Wire[] compOutputs = new Wire[compDef.outputs().size()];
            for (int j = 0; j < compDef.outputs().size(); j++) {
                compOutputs[j] = contextWires.get(compDef.outputs().get(j));
            }

            internalComponents[i] = this.build(compDef.type(), compDef.name(), compInputs, compOutputs);
        }

        return new ComplexChip(instanceName, inputs, outputs, internalComponents);
    }

    private void validateDependencyGraph() {
        Map<String, Mark> marks = new HashMap<>();
        for (String chipName : this.registry.keySet()) {
            if (!marks.containsKey(chipName)) {
                this.checkCycles(chipName, marks);
            }
        }
    }

    private void checkCycles(String chipName, Map<String, Mark> marks) {
        if (this.isPrimitive(chipName)) return;

        Mark mark = marks.get(chipName);
        if (mark == Mark.VISITING) throw new IllegalStateException("Circular dependency detected involving: " + chipName);
        if (mark == Mark.VISITED) return;

        marks.put(chipName, Mark.VISITING);

        ChipDefinition def = this.registry.get(chipName);
        if (def == null) throw new IllegalArgumentException("Unkown component referenced: " + chipName);

        for (ComponentDefinition internal : def.components()) {
            this.checkCycles(internal.type(), marks);
        }

        marks.put(chipName, Mark.VISITED);
    }

    private boolean isPrimitive(String type) {
        return PRIMITIVES.contains(type);
    }

    private LogicComponent buildPrimitive(String type, String name, Wire[] inputs, Wire[] outputs) {
        return switch (type) {
            case "AndGate" -> new AndGate(name, inputs[0], inputs[1], outputs[0]);
            case "NandGate" -> new NandGate(name, inputs[0], inputs[1], outputs[0]);
            case "XorGate" -> new XorGate(name, inputs[0], inputs[1], outputs[0]);
            case "NotGate" -> new NotGate(name, inputs[0], outputs[0]);
            case "OrGate" -> new OrGate(name, inputs[0], inputs[1], outputs[0]);
            case "NorGate" -> new NorGate(name, inputs[0], inputs[1], outputs[0]);
            case "XnorGate" -> new XnorGate(name, inputs[0], inputs[1], outputs[0]);
            case "DLatch" -> new DLatch(name, inputs[0], inputs[1], outputs[0], outputs[1]);
            case "VCC" -> new VccGate(name, outputs[0]);
            case "GND" -> new GndGate(name, outputs[0]);
            default -> throw new IllegalArgumentException("Unsupported primitive: " + type);
        };
    }
}
