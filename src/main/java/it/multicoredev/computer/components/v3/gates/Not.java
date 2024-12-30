package it.multicoredev.computer.components.v3.gates;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Not extends ChipComponent {

    public Not() {
        super("NOT",
                new Pin[]{
                        new Pin("In", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("Out", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        outputs[0].state(inputState(0).toggle());
    }

    public Pin pinIn() {
        return inputs[0];
    }

    public Pin pinOut() {
        return outputs[0];
    }

    public Not in(State state) {
        pinIn().state(state);
        return this;
    }

    public State out() {
        return pinOut().state();
    }
}
