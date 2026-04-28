package it.lycoris.cpu.hardware.io.dto;

import java.util.List;
import java.util.Map;

public record ComponentDefinition(
        String type,
        String name,
        Map<String, String> inputs,
        Map<String, List<String>> outputs
) {
}
