package it.lycoris.cpu.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Wire {
    private boolean state;
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();

    public Wire(boolean state) {
        this.state = state;
    }

    public Wire() {
        this.state = false;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        if (this.state != state) {
            this.state = state;
            for (Consumer<Boolean> listener : listeners) {
                listener.accept(state);
            }
        }
    }

    public void addListener(Consumer<Boolean> listener) {
        this.listeners.add(listener);
        listener.accept(this.state);
    }
}
