package it.lycoris.cpu.hardware;

import it.lycoris.cpu.hardware.io.ComponentLibrary;
import it.lycoris.cpu.hardware.io.dto.ChipDefinition;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HardwareTestBase {
    protected static ComponentLibrary lib;
    protected LogicComponent chip;

    protected Map<String, Wire> inputWires;
    protected Map<String, Wire> outputWires;

    static {
        lib = new ComponentLibrary();
        try {
            lib.loadDirectory(Paths.get("src/main/resources/hardware"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load component library", e);
        }
    }

    protected void load(String chipName) {
        ChipDefinition def = lib.getDefinition(chipName);
        inputWires = new HashMap<>();
        outputWires = new HashMap<>();

        def.pins().inputs().forEach(name -> inputWires.put(name, new Wire()));
        def.pins().outputs().forEach(name -> outputWires.put(name, new Wire()));

        this.chip = lib.build(chipName, "Test_" + chipName, inputWires, outputWires);
    }

    protected void setPin(String name, boolean state) {
        Wire w = inputWires.get(name);
        Assertions.assertNotNull(w, "Input pin not found: " + name);
        w.setState(state);
    }

    protected boolean getPin(String name) {
        Wire w = outputWires.get(name);
        Assertions.assertNotNull(w, "Output pin not found: " + name);
        return w.getState();
    }

    protected void setBus(String prefix, int value) {
        for (int i = 0; i < 8; i++) {
            Wire w = inputWires.get(prefix + i);
            if (w != null) {
                w.setState(((value >> i) & 1) == 1);
            }
        }
    }

    protected int getBus(String prefix) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            Wire w = outputWires.get(prefix + i);
            if (w != null && w.getState()) {
                result |= (1 << i);
            }
        }
        return result;
    }

    protected void update() {
        for (int i = 0; i < 10; i++) {
            chip.update();
        }
    }

    protected void pulseClock(String clockPin) {
        update();
        setPin(clockPin, true);
        update();
        setPin(clockPin, false);
        update();
    }

    protected void clearOperations() {
        if (inputWires.containsKey("OpADD")) setPin("OpADD", false);
        if (inputWires.containsKey("OpAND")) setPin("OpAND", false);
        if (inputWires.containsKey("OpOR")) setPin("OpOR", false);
        if (inputWires.containsKey("OpXOR")) setPin("OpXOR", false);
    }
}
