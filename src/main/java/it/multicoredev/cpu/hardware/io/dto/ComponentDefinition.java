package it.multicoredev.cpu.hardware.io.dto;

import java.util.List;

public record ComponentDefinition(
        String type,
        String name,
        List<String> inputs,
        List<String> outputs
) {
}
