package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.emulated.EmulationContext;

import java.util.ArrayList;
import java.util.List;

public class Wire {
    private boolean state;
    private final List<Listener> listeners = new ArrayList<>();

    public Wire(boolean state) {
        this.state = state;
    }

    public Wire() {
        this.state = false;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean newState, EmulationContext ctx) {
        if (this.state != newState) {
            this.state = newState;

            if (ctx != null) {
                for (Listener listener : listeners) {
                    listener.onStateChange(state, ctx);
                }
            }
        }
    }

    public void addListener(Listener comp) {
        listeners.add(comp);
    }

    public interface Listener {
        void onStateChange(boolean newState, EmulationContext ctx);
    }
}
