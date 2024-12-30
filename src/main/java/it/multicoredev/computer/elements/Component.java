package it.multicoredev.computer.elements;

import it.multicoredev.computer.util.State;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {
    protected final String name;
    protected final Pin[] inputs;
    protected final Pin[] outputs;

    public Component(String name, Pin[] inputs, Pin[] outputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;

        for (Pin input : this.inputs) {
            input.component(this);
        }

        for (Pin output : this.outputs) {
            output.component(this);
        }
    }

    protected void update() {
        run();
    }

    public abstract void run();

    public String name() {
        return name;
    }

    // WARNING: Do not change values in the returned array
    public Pin[] inputs() {
        return inputs;
    }

    // WARNING: Do not change values in the returned array
    public Pin[] outputs() {
        return outputs;
    }

    public Pin input(int index) {
        return inputs[index];
    }

    public Pin output(int index) {
        return outputs[index];
    }

    public State inputState(int index) {
        return inputs[index].state();
    }

    public State outputState(int index) {
        return outputs[index].state();
    }

    public List<Pin> getConnections() {
        List<Pin> connections = new ArrayList<>();
        for (Pin pin : outputs) {
            if (!pin.outboundConnections().isEmpty()) connections.addAll(pin.outboundConnections());
        }

        return connections;
    }

    public Component connect(int index, Pin pin) {
        outputs[index].connect(pin);
        return this;
    }

    public Component connect(Pin... pins) {
        for (int i = 0; i < pins.length; i++) {
            outputs[i].connect(pins[i]);
        }

        return this;
    }

    protected void setOutputs(int startIndex, State... states) {
        for (int i = 0; i < states.length; i++) {
            outputs[startIndex + i].state(states[i]);
        }
    }
}
