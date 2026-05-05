package it.lycoris.j6502.transpiler.data;

import java.util.List;
import java.util.Map;

public record ComponentDefinition(
        String type,
        String name,
        Map<String, String> inputs,
        Map<String, List<String>> outputs
) {
}
