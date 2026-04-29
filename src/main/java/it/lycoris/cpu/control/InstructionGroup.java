package it.lycoris.cpu.control;

import java.util.Map;

public interface InstructionGroup {
    void install(Map<Integer, OpcodeMetadata> registry);
}
