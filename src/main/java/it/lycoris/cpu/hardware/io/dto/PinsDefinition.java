package it.lycoris.cpu.hardware.io.dto;

import java.util.List;

public record PinsDefinition(
        List<String> inputs,
        List<String> outputs
) {
}
