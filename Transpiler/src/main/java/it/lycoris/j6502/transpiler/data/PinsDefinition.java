package it.lycoris.j6502.transpiler.data;

import java.util.List;

public record PinsDefinition(
        List<String> inputs,
        List<String> outputs
) {
}
