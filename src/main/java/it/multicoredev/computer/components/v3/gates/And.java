package it.multicoredev.computer.components.v3.gates;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class And extends ChipComponent {

    public And() {
        super("AND",
                new Pin[]{
                        new Pin("A", Direction.INPUT),
                        new Pin("B", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("Out", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        outputs[0].state(input(0).state().equals(input(1).state()) && input(0).state().equals(State.HIGH) ? State.HIGH : State.LOW);
    }

    public Pin pinA() {
        return inputs[0];
    }

    public Pin pinB() {
        return inputs[1];
    }

    public Pin pinOut() {
        return outputs[0];
    }

    public And in(State a, State b) {
        pinA().state(a);
        pinB().state(b);
        return this;
    }

    public And a(State state) {
        pinA().state(state);
        return this;
    }

    public And b(State state) {
        pinB().state(state);
        return this;
    }

    public State out() {
        return pinOut().state();
    }
}
