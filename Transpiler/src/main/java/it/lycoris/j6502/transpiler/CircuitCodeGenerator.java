package it.lycoris.j6502.transpiler;

import com.squareup.javapoet.*;
import it.lycoris.j6502.transpiler.data.ChipDefinition;
import it.lycoris.j6502.transpiler.data.ComponentDefinition;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Code generator for transpiling JSON circuit definitions into Java classes.
 * It uses a composition-based approach for complex sub-chips and inline
 * generation for primitive logic gates to maximize JIT compiler efficiency.
 */
public class CircuitCodeGenerator {
    private static final String PACKAGE_NAME = "it.lycoris.j6502.hardware.generated";
    private static final String CLOCK_SIGNAL_NAME = "Clk";
    private static final String TYPE_VCC = "VCC";
    private static final String TYPE_GND = "GND";

    public void generateChipClass(ChipDefinition chipDefinition, List<ComponentDefinition> topologicalOrder, File outputDir) throws IOException {
        TypeSpec.Builder chipClassBuilder = TypeSpec.classBuilder(chipDefinition.chipName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Auto-generated hardware emulation class for $L.\n", chipDefinition.chipName())
                .addJavadoc("Do not modify manually.\n");

        Map<ComponentDefinition, String> uniqueFieldNames = buildUniqueFieldNames(chipDefinition);

        generateWireFields(chipClassBuilder, chipDefinition);
        generateSubChipFields(chipClassBuilder, chipDefinition, uniqueFieldNames);

        MethodSpec evaluateCombMethod = buildEvaluateCombinationalMethod(topologicalOrder, uniqueFieldNames, chipDefinition);
        MethodSpec tickClockMethod = buildTickClockMethod(chipDefinition, uniqueFieldNames);

        chipClassBuilder.addMethod(evaluateCombMethod);
        chipClassBuilder.addMethod(tickClockMethod);

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, chipClassBuilder.build())
                .indent("    ")
                .build();

        javaFile.writeTo(outputDir);
    }

    private Map<ComponentDefinition, String> buildUniqueFieldNames(ChipDefinition chipDefinition) {
        Map<ComponentDefinition, String> fieldNames = new IdentityHashMap<>();
        Map<String, Integer> nameCounts = new HashMap<>();

        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                String baseName = component.name();
                if (baseName == null || baseName.trim().isEmpty()) baseName = "unnamedComponent";

                String sanitizedBase = sanitizeName(baseName);
                sanitizedBase = Character.toLowerCase(sanitizedBase.charAt(0)) + sanitizedBase.substring(1);

                int count = nameCounts.getOrDefault(sanitizedBase, 0);
                String uniqueName = (count == 0) ? sanitizedBase : sanitizedBase + "_" + count;

                nameCounts.put(sanitizedBase, count + 1);
                fieldNames.put(component, uniqueName);
            }
        }
        return fieldNames;
    }

    private void generateWireFields(TypeSpec.Builder chipClassBuilder, ChipDefinition chipDefinition) {
        if (chipDefinition.pins().inputs() != null) {
            for (String input : chipDefinition.pins().inputs()) {
                chipClassBuilder.addField(
                        FieldSpec.builder(boolean.class, sanitizeName(input), Modifier.PUBLIC)
                                .addJavadoc("Input pin\n")
                                .build()
                );
            }
        }

        if (chipDefinition.pins().outputs() != null) {
            for (String output : chipDefinition.pins().outputs()) {
                chipClassBuilder.addField(
                        FieldSpec.builder(boolean.class, sanitizeName(output), Modifier.PUBLIC)
                                .addJavadoc("Output pin\n")
                                .build()
                );
            }
        }

        if (chipDefinition.internalWires() != null) {
            for (String wire : chipDefinition.internalWires()) {
                chipClassBuilder.addField(
                        FieldSpec.builder(boolean.class, sanitizeName(wire), Modifier.PRIVATE)
                                .addJavadoc("Internal routing wire\n")
                                .build()
                );
            }
        }
    }

    private void generateSubChipFields(TypeSpec.Builder chipClassBuilder, ChipDefinition chipDefinition, Map<ComponentDefinition, String> uniqueFieldNames) {
        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                if (!isLogicGate(component.type()) && !isNativeStateHolder(component.type()) && !isConstantProvider(component.type())) {
                    ClassName subChipClass = ClassName.get(PACKAGE_NAME, component.type());
                    String uniqueFieldName = uniqueFieldNames.get(component);

                    chipClassBuilder.addField(
                            FieldSpec.builder(subChipClass, uniqueFieldName, Modifier.PRIVATE, Modifier.FINAL)
                                    .initializer("new $T()", subChipClass)
                                    .addJavadoc("Sub-chip instance for $L of type $L\n", component.name(), component.type())
                                    .build()
                    );
                }
            }
        }
    }

    private MethodSpec buildEvaluateCombinationalMethod(List<ComponentDefinition> topologicalOrder, Map<ComponentDefinition, String> uniqueFieldNames, ChipDefinition chipDefinition) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("evaluateCombinational")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addJavadoc("Evaluates all combinational logic in topological order.\n");

        // 1. Evaluate purely combinational components
        for (ComponentDefinition component : topologicalOrder) {
            if (isSequential(component)) {
                continue;
            }

            builder.addComment("Evaluating component: $L ($L)", component.name(), component.type());

            if (this.isLogicGate(component.type())) this.inlineLogicGate(builder, component);
            else if (this.isConstantProvider(component.type())) this.inlineConstant(builder, component);
            else this.invokeSubChipCombinational(builder, component, uniqueFieldNames);
        }

        // 2. Propagate signals and evaluate internal combinational logic of sequential sub-chips
        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                if (isSequential(component) && !isNativeStateHolder(component.type())) {
                    builder.addComment("Propagating combinational signals to sequential sub-chip: $L", component.name());
                    this.invokeSubChipCombinational(builder, component, uniqueFieldNames);
                }
            }
        }

        return builder.build();
    }

    /**
     * Constructs the tickClock method ensuring proper clock-gating and enable-gating
     * to prevent runaway state changes in FlipFlops and Latches.
     */
    private MethodSpec buildTickClockMethod(ChipDefinition chipDefinition, Map<ComponentDefinition, String> uniqueFieldNames) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("tickClock")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addJavadoc("Updates sequential state elements on clock edge, then stabilizes outputs.\n");

        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                if (isSequential(component)) {
                    builder.addComment("Clocking sequential component: $L", component.name());

                    if ("DFlipFlop".equals(component.type())) {
                        String inputD = sanitizeName(component.inputs().get("D"));
                        String inputClk = sanitizeName(component.inputs().get("Clk"));
                        boolean hasEn = component.inputs().containsKey("En");

                        if (hasEn) {
                            String inputEn = sanitizeName(component.inputs().get("En"));
                            builder.addCode("if (this.$L && this.$L) {\n", inputClk, inputEn);
                        } else {
                            builder.addCode("if (this.$L) {\n", inputClk);
                        }

                        mapStateOutputs(builder, component, inputD);
                        builder.addCode("}\n");

                    } else if ("DLatch".equals(component.type())) {
                        String inputD = sanitizeName(component.inputs().get("D"));
                        String inputEn = sanitizeName(component.inputs().get("En"));

                        builder.addCode("if (this.$L) {\n", inputEn);

                        mapStateOutputs(builder, component, inputD);
                        builder.addCode("}\n");

                    } else {
                        invokeSubChipClock(builder, component, uniqueFieldNames);
                    }
                }
            }
        }

        builder.addComment("Stabilize combinational outputs based on new sequential state");
        builder.addStatement("this.evaluateCombinational()");

        return builder.build();
    }

    /**
     * Helper to safely map a state value to multiple output wires (Fan-Out).
     */
    private void mapStateOutputs(MethodSpec.Builder builder, ComponentDefinition component, String dataProvider) {
        if (component.outputs().containsKey("Q")) {
            for (String qWire : component.outputs().get("Q")) {
                builder.addStatement("    this.$L = this.$L", sanitizeName(qWire), dataProvider);
            }
        }
        if (component.outputs().containsKey("!Q")) {
            for (String notQWire : component.outputs().get("!Q")) {
                builder.addStatement("    this.$L = !this.$L", sanitizeName(notQWire), dataProvider);
            }
        }
    }

    private void invokeSubChipCombinational(MethodSpec.Builder builder, ComponentDefinition component, Map<ComponentDefinition, String> uniqueFieldNames) {
        String componentFieldName = uniqueFieldNames.get(component);

        if (component.inputs() != null) {
            for (Map.Entry<String, String> entry : component.inputs().entrySet()) {
                builder.addStatement("this.$L.$L = this.$L", componentFieldName, sanitizeName(entry.getKey()), sanitizeName(entry.getValue()));
            }
        }

        builder.addStatement("this.$L.evaluateCombinational()", componentFieldName);

        if (component.outputs() != null) {
            for (Map.Entry<String, List<String>> entry : component.outputs().entrySet()) {
                for (String outputWire : entry.getValue()) {
                    builder.addStatement("this.$L = this.$L.$L", sanitizeName(outputWire), componentFieldName, sanitizeName(entry.getKey()));
                }
            }
        }
    }

    private void invokeSubChipClock(MethodSpec.Builder builder, ComponentDefinition component, Map<ComponentDefinition, String> uniqueFieldNames) {
        String componentFieldName = uniqueFieldNames.get(component);

        if (component.inputs() != null) {
            for (Map.Entry<String, String> entry : component.inputs().entrySet()) {
                builder.addStatement("this.$L.$L = this.$L", componentFieldName, sanitizeName(entry.getKey()), sanitizeName(entry.getValue()));
            }
        }

        builder.addStatement("this.$L.tickClock()", componentFieldName);

        if (component.outputs() != null) {
            for (Map.Entry<String, List<String>> entry : component.outputs().entrySet()) {
                for (String outputWire : entry.getValue()) {
                    builder.addStatement("this.$L = this.$L.$L", sanitizeName(outputWire), componentFieldName, sanitizeName(entry.getKey()));
                }
            }
        }
    }

    private void inlineLogicGate(MethodSpec.Builder builder, ComponentDefinition component) {
        Map<String, String> inputs = component.inputs();
        String outputWire = sanitizeName(component.outputs().get("Out").getFirst());

        String inputA = inputs.containsKey("A") ? sanitizeName(inputs.get("A")) : "false";
        String inputB = inputs.containsKey("B") ? sanitizeName(inputs.get("B")) : "false";
        String inputIn = inputs.containsKey("In") ? sanitizeName(inputs.get("In")) : "false";

        switch (component.type()) {
            case "AndGate" -> builder.addStatement("this.$L = this.$L && this.$L", outputWire, inputA, inputB);
            case "OrGate" -> builder.addStatement("this.$L = this.$L || this.$L", outputWire, inputA, inputB);
            case "NotGate" -> builder.addStatement("this.$L = !this.$L", outputWire, inputIn);
            case "NorGate" -> builder.addStatement("this.$L = !(this.$L || this.$L)", outputWire, inputA, inputB);
            case "NandGate" -> builder.addStatement("this.$L = !(this.$L && this.$L)", outputWire, inputA, inputB);
            case "XorGate" -> builder.addStatement("this.$L = this.$L ^ this.$L", outputWire, inputA, inputB);
            default -> throw new IllegalArgumentException("Unknown logic gate: " + component.type());
        }
    }

    private void inlineConstant(MethodSpec.Builder builder, ComponentDefinition component) {
        if (component.outputs() == null || component.outputs().isEmpty()) return;
        boolean constantValue = TYPE_VCC.equals(component.type());

        for (List<String> outputWires : component.outputs().values()) {
            for (String outputWire : outputWires) {
                builder.addStatement("this.$L = $L", sanitizeName(outputWire), constantValue);
            }
        }
    }

    public static boolean isSequential(ComponentDefinition component) {
        if (component.inputs() == null) return false;
        if (isNativeStateHolder(component.type())) return true;
        return component.inputs().containsValue(CLOCK_SIGNAL_NAME);
    }

    private static boolean isNativeStateHolder(String type) {
        return "DFlipFlop".equals(type) || "DLatch".equals(type);
    }

    private boolean isLogicGate(String type) {
        return type != null && type.endsWith("Gate");
    }

    private boolean isConstantProvider(String type) {
        return TYPE_VCC.equals(type) || TYPE_GND.equals(type);
    }

    private String sanitizeName(String rawName) {
        if (rawName == null) return "Unconnected";
        return rawName.replace("!", "_NOT_").replace("-", "_");
    }
}