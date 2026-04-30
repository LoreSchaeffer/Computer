package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.hardware.LogicComponent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class EmulationContext {
    private final ArrayDeque<LogicComponent> queue = new ArrayDeque<>();
    private final Set<LogicComponent> scheduled = new HashSet<>();

    public void schedule(LogicComponent comp) {
        if (scheduled.add(comp)) {
            queue.add(comp);
        }
    }

    public void run() {
        while (!queue.isEmpty()) {
            LogicComponent comp = queue.poll();
            scheduled.remove(comp);
            comp.update(this);
        }
    }
}
