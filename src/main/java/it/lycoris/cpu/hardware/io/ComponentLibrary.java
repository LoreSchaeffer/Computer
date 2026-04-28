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

    public LogicComponent build(String type, String instanceName, Map<String, Wire> externalInputs, Map<String, Wire> externalOutputs) {
        ChipDefinition def = registry.get(type);
        if (def == null) throw new IllegalArgumentException("Definizione chip non trovata: " + type);

        Map<String, Wire> contextWires = new HashMap<>();
        contextWires.putAll(externalInputs);
        contextWires.putAll(externalOutputs);

        if (def.internalWires() != null) {
            for (String wireName : def.internalWires()) {
                contextWires.putIfAbsent(wireName, new Wire());
            }
        }

        LogicComponent[] internalComponents = new LogicComponent[def.components().size()];
        for (int i = 0; i < def.components().size(); i++) {
            ComponentDefinition compDef = def.components().get(i);

            Map<String, Wire> compInputs = new HashMap<>();
            compDef.inputs().forEach((pinName, wireName) -> {
                contextWires.putIfAbsent(wireName, new Wire());
                compInputs.put(pinName, contextWires.get(wireName));
            });

            Map<String, Wire> compOutputs = new HashMap<>();
            compDef.outputs().forEach((pinName, wireNames) -> {
                if (wireNames.isEmpty()) {
                    compOutputs.put(pinName, new Wire());
                } else {
                    Wire masterWire = new Wire();
                    compOutputs.put(pinName, masterWire);

                    for (String wName : wireNames) {
                        contextWires.putIfAbsent(wName, new Wire());
                        Wire targetWire = contextWires.get(wName);
                        masterWire.addListener(targetWire::setState);
                    }
                }
            });

            internalComponents[i] = buildInternal(compDef.type(), compDef.name(), compInputs, compOutputs);
        }

        Wire[] inputsArray = def.pins().inputs().stream().map(name -> contextWires.getOrDefault(name, new Wire())).toArray(Wire[]::new);
        Wire[] outputsArray = def.pins().outputs().stream().map(name -> contextWires.getOrDefault(name, new Wire())).toArray(Wire[]::new);

        return new ComplexChip(instanceName, inputsArray, outputsArray, internalComponents);
    }

    private LogicComponent buildInternal(String type, String instanceName, Map<String, Wire> inputs, Map<String, Wire> outputs) {
        if (PRIMITIVES.contains(type)) {
            return buildPrimitive(type, instanceName, inputs, outputs);
        }
        return build(type, instanceName, inputs, outputs);
    }

    private LogicComponent buildPrimitive(String type, String name, Map<String, Wire> inputs, Map<String, Wire> outputs) {
        Wire inA = inputs.getOrDefault("A", inputs.getOrDefault("In0", new Wire()));
        Wire inB = inputs.getOrDefault("B", inputs.getOrDefault("In1", new Wire()));
        Wire inSingle = inputs.getOrDefault("In", inputs.getOrDefault("In0", new Wire()));
        Wire out = outputs.getOrDefault("Out", outputs.getOrDefault("Out0", new Wire()));

        return switch (type) {
            case "AndGate" -> new AndGate(name, inA, inB, out);
            case "NandGate" -> new NandGate(name, inA, inB, out);
            case "OrGate" -> new OrGate(name, inA, inB, out);
            case "NorGate" -> new NorGate(name, inA, inB, out);
            case "XorGate" -> new XorGate(name, inA, inB, out);
            case "XnorGate" -> new XnorGate(name, inA, inB, out);
            case "NotGate" -> new NotGate(name, inSingle, out);
            case "DLatch" -> new DLatch(name,
                    inputs.getOrDefault("D", inputs.getOrDefault("In0", new Wire())),
                    inputs.getOrDefault("En", inputs.getOrDefault("In1", new Wire())),
                    outputs.getOrDefault("Q", outputs.getOrDefault("Out0", new Wire())),
                    outputs.getOrDefault("NotQ", outputs.getOrDefault("Out1", new Wire()))
            );
            case "VCC" -> new VccGate(name, out);
            case "GND" -> new GndGate(name, out);
            default -> throw new IllegalStateException("Primitiva sconosciuta: " + type);
        };
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
}
