package it.lycoris.j6502.transpiler;

import it.lycoris.j6502.transpiler.data.ChipDefinition;
import it.lycoris.j6502.transpiler.data.ComponentDefinition;

import java.util.*;

import static it.lycoris.j6502.transpiler.CircuitCodeGenerator.isSequential;

public class CircuitGraphBuilder {
    private final ChipDefinition chipDefinition;

    public CircuitGraphBuilder(ChipDefinition chipDefinition) {
        this.chipDefinition = chipDefinition;
    }

    public List<ComponentDefinition> getTopologicalSort() {
        Map<ComponentDefinition, Integer> inDegree = new HashMap<>();
        Map<ComponentDefinition, List<ComponentDefinition>> adjacencyList = new HashMap<>();
        Map<String, ComponentDefinition> wireDriverMap = buildWireDriverMap();

        List<ComponentDefinition> combinationalComponents = new ArrayList<>();

        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                if (!isSequential(component)) {
                    combinationalComponents.add(component);
                    inDegree.put(component, 0);
                    adjacencyList.put(component, new ArrayList<>());
                }
            }
        }

        for (ComponentDefinition currentComponent : combinationalComponents) {
            if (currentComponent.inputs() == null) continue;

            for (String inputWire : currentComponent.inputs().values()) {
                ComponentDefinition driverComponent = wireDriverMap.get(inputWire);

                if (driverComponent != null && !isSequential(driverComponent)) {
                    adjacencyList.get(driverComponent).add(currentComponent);
                    inDegree.put(currentComponent, inDegree.get(currentComponent) + 1);
                }
            }
        }

        Queue<ComponentDefinition> queue = new LinkedList<>();
        for (Map.Entry<ComponentDefinition, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        List<ComponentDefinition> sortedOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            ComponentDefinition current = queue.poll();
            sortedOrder.add(current);

            for (ComponentDefinition neighbor : adjacencyList.get(current)) {
                int updatedInDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, updatedInDegree);

                if (updatedInDegree == 0) queue.add(neighbor);
            }
        }

        if (sortedOrder.size() != combinationalComponents.size()) {
            throw new IllegalStateException("Combinational loop detected in the circuit graph: " + chipDefinition.chipName());
        }

        return sortedOrder;
    }

    private Map<String, ComponentDefinition> buildWireDriverMap() {
        Map<String, ComponentDefinition> wireDriverMap = new HashMap<>();

        if (chipDefinition.components() != null) {
            for (ComponentDefinition component : chipDefinition.components()) {
                if (component.outputs() == null) continue;

                for (List<String> outputWires : component.outputs().values()) {
                    for (String wire : outputWires) {
                        wireDriverMap.put(wire, component);
                    }
                }
            }
        }

        return wireDriverMap;
    }
}
