package it.multicoredev.computer.elements;

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

    public List<Pin> getConnections() {
        List<Pin> connections = new ArrayList<>();
        for (Pin pin : outputs) {
            if (!pin.outboundConnections().isEmpty()) connections.addAll(pin.outboundConnections());
        }

        return connections;
    }
}
