package it.multicoredev.computer.elements;

import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

import java.util.ArrayList;
import java.util.List;

public class Pin {
    private String name;
    private final Direction direction;
    private State state;
    private Component component;
    private final List<Pin> outboundConnections = new ArrayList<>();

    public Pin(String name, Direction direction, State state) {
        this.name = name;
        this.direction = direction;
        this.state = state;
    }

    public Pin(String name, Direction direction) {
        this(name, direction, State.LOW);
    }

    public String name() {
        return name;
    }

    public Pin name(String name) {
        this.name = name;
        return this;
    }

    public Direction direction() {
        return direction;
    }

    public Pin state(State state) {
        this.state = state;
        if (direction.equals(Direction.OUTPUT)) outboundConnections.forEach(pin -> pin.state(state));
        else component.update();
        return this;
    }

    public Pin toggleState() {
        return state(state.toggle());
    }

    public State state() {
        return state;
    }

    public Pin component(Component component) {
        if (this.component != null) throw new IllegalArgumentException("Pin already connected to a component");
        this.component = component;
        return this;
    }

    public Component component() {
        return component;
    }

    public List<Pin> outboundConnections() {
        return outboundConnections;
    }

    public Pin connect(Pin pin) {
        if (pin.direction().equals(Direction.OUTPUT)) throw new IllegalArgumentException("Cannot connect an output pin to another output pin");
        outboundConnections.add(pin);
        return this;
    }

    public Pin connect(Pin... pins) {
        for (Pin pin : pins) connect(pin);
        return this;
    }

    public Pin disconnect(Pin pin) {
        outboundConnections.remove(pin);
        return this;
    }

    public Pin disconnect(Pin... pins) {
        for (Pin pin : pins) disconnect(pin);
        return this;
    }

    public Pin disconnectAll() {
        outboundConnections.clear();
        return this;
    }
}
