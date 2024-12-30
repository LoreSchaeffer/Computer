package it.multicoredev.computer.components.v3;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class TriStateBuffer extends ChipComponent {

    public TriStateBuffer() {
        super ("3-State Buffer",
                new Pin[]{
                        new Pin("En", Direction.INPUT),
                        new Pin("D", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("Out", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        outputs[0].state(inputState(0).equals(State.HIGH) ? inputState(1) : State.FLOATING);
    }
}
