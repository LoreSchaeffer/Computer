package it.lycoris.j6502.emulator.control;

import java.util.Map;

public interface InstructionGroup {
    void install(Map<Integer, OpcodeMetadata> registry);
}
