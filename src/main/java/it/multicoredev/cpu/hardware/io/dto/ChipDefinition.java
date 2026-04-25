package it.multicoredev.cpu.hardware.io.dto;

import java.util.List;

public record ChipDefinition(
        String chipName,
        PinsDefinition pins,
        List<String> internalWires,
        List<ComponentDefinition> components
) {
}
