package it.lycoris.j6502.emulator.hardware.io.dto;

import java.util.List;

public record PinsDefinition(
        List<String> inputs,
        List<String> outputs
) {
}
