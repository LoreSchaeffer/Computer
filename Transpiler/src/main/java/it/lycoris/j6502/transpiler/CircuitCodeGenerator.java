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

public class CircuitCodeGenerator {
    public static final String CLOCK_SIGNAL_NAME = "Clk";
    private static final String TYPE_VCC = "VCC";
    private static final String TYPE_GND = "GND";
    private static final String PACKAGE_NAME = "it.lycoris.j6502.hardware.generated";

    public void generateChipClass(ChipDefinition chipDefinition, List<ComponentDefinition> topologicalOrder, File outputDir) throws IOException {
        TypeSpec.Builder chipClassBuilder = TypeSpec.classBuilder(chipDefinition.chipName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Auto-generated hardware emulation class for $L.\n", chipDefinition.chipName())
                .addJavadoc("Do not modify manually.\n");

        Map<ComponentDefinition, String> uniqueFieldNames = buildUniqueFieldNames(chipDefinition);

        generateWireFields(chipClassBuilder, chipDefinition);
        generateSubChipFields(chipClassBuilder, chipDefinition, uniqueFieldNames);

        MethodSpec evaluateCombMethod = buildEvaluateCombinationalMethod(topologicalOrder, uniqueFieldNames);
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
                if (baseName == null || baseName.trim().isEmpty()) baseName = "UnnamedComponent";

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

    private MethodSpec buildEvaluateCombinationalMethod(List<ComponentDefinition> topologicalOrder, Map<ComponentDefinition, String> uniqueFieldNames) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("evaluateCombinational")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addJavadoc("Evaluates all combinational logic in topological order.\n");

        for (ComponentDefinition component : topologicalOrder) {
            if (isSequential(component)) continue;

            builder.addComment("Evaluating component: $L ($L)", component.name(), component.type());

            if (isLogicGate(component.type())) inlineLogicGate(builder, component);
            else if (isConstantProvider(component.type())) inlineConstant(builder, component);
            else invokeSubChipCombinational(builder, component, uniqueFieldNames);
        }

        return builder.build();
    }

    private MethodSpec buildTickClockMethod(ChipDefinition chipDefinition, Map<ComponentDefinition, String> uniqueFieldNames) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("tickClock")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addJavadoc("Updates sequential state elements on clock edge, then stabilizes outputs.\n");

        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                if (isSequential(component)) {
                    builder.addComment("Clocking sequential component: $L", component.name());

                    if (isNativeStateHolder(component.type())) {
                        String inputD = sanitizeName(component.inputs().get("D"));
                        String outputQ = sanitizeName(component.outputs().get("Q").getFirst());

                        builder.addStatement("this.$L = this.$L", outputQ, inputD);
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

    private void invokeSubChipCombinational(MethodSpec.Builder builder, ComponentDefinition component, Map<ComponentDefinition, String> uniqueFieldNames) {
        String componentFieldName = uniqueFieldNames.get(component);

        if (component.inputs() != null) {
            for (Map.Entry<String, String> entry : component.inputs().entrySet()) {
                String subPin = sanitizeName(entry.getKey());
                String internalWire = sanitizeName(entry.getValue());
                builder.addStatement("this.$L.$L = this.$L", componentFieldName, subPin, internalWire);
            }
        }

        builder.addStatement("this.$L.evaluateCombinational()", componentFieldName);

        if (component.outputs() != null) {
            for (Map.Entry<String, List<String>> entry : component.outputs().entrySet()) {
                String subPin = sanitizeName(entry.getKey());
                for (String outputWire : entry.getValue()) {
                    builder.addStatement("this.$L = this.$L.$L", sanitizeName(outputWire), componentFieldName, subPin);
                }
            }
        }
    }

    private void invokeSubChipCombinational(MethodSpec.Builder builder, ComponentDefinition component) {
        String componentFieldName = sanitizeName(component.name());

        if (component.inputs() != null) {
            for (Map.Entry<String, String> entry : component.inputs().entrySet()) {
                String subPin = sanitizeName(entry.getKey());
                String internalWire = sanitizeName(entry.getValue());
                builder.addStatement("this.$L.$L = this.$L", componentFieldName, subPin, internalWire);
            }
        }

        builder.addStatement("this.$L.evaluateCombinational()", componentFieldName);

        if (component.outputs() != null) {
            for (Map.Entry<String, List<String>> entry : component.outputs().entrySet()) {
                String subPin = sanitizeName(entry.getKey());
                for (String outputWire : entry.getValue()) {
                    builder.addStatement("this.$L = this.$L.$L", sanitizeName(outputWire), componentFieldName, subPin);
                }
            }
        }
    }

    private void invokeSubChipClock(MethodSpec.Builder builder, ComponentDefinition component, Map<ComponentDefinition, String> uniqueFieldNames) {
        String componentFieldName = uniqueFieldNames.get(component);

        if (component.inputs() != null) {
            for (Map.Entry<String, String> entry : component.inputs().entrySet()) {
                String subPin = sanitizeName(entry.getKey());
                String internalWire = sanitizeName(entry.getValue());
                builder.addStatement("this.$L.$L = this.$L", componentFieldName, subPin, internalWire);
            }
        }

        builder.addStatement("this.$L.tickClock()", componentFieldName);

        if (component.outputs() != null) {
            for (Map.Entry<String, List<String>> entry : component.outputs().entrySet()) {
                String subPin = sanitizeName(entry.getKey());
                for (String outputWire : entry.getValue()) {
                    builder.addStatement("this.$L = this.$L.$L", sanitizeName(outputWire), componentFieldName, subPin);
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
            case "AndGate":
                builder.addStatement("this.$L = this.$L && this.$L", outputWire, inputA, inputB);
                break;
            case "OrGate":
                builder.addStatement("this.$L = this.$L || this.$L", outputWire, inputA, inputB);
                break;
            case "NotGate":
                builder.addStatement("this.$L = !this.$L", outputWire, inputIn);
                break;
            case "NorGate":
                builder.addStatement("this.$L = !(this.$L || this.$L)", outputWire, inputA, inputB);
                break;
            case "NandGate":
                builder.addStatement("this.$L = !(this.$L && this.$L)", outputWire, inputA, inputB);
                break;
            case "XorGate":
                builder.addStatement("this.$L = this.$L ^ this.$L", outputWire, inputA, inputB);
                break;
            default:
                throw new IllegalArgumentException("Unknown logic gate: " + component.type());
        }
    }

    public static boolean isSequential(ComponentDefinition component) {
        if (component.inputs() == null) return false;
        if (isNativeStateHolder(component.type())) return true;
        return component.inputs().containsValue(CLOCK_SIGNAL_NAME);
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

    private static boolean isNativeStateHolder(String type) {
        return "DFlipFlop".equals(type) || "DLatch".equals(type);
    }

    private boolean isLogicGate(String type) {
        return type.endsWith("Gate");
    }

    private boolean isConstantProvider(String type) {
        return TYPE_VCC.equals(type) || TYPE_GND.equals(type);
    }

    private String sanitizeName(String rawName) {
        if (rawName == null) return "Unconnected";
        return rawName.replace("!", "_NOT_").replace("-", "_");
    }
}
