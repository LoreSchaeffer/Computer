package it.multicoredev.computer.components.v3.display;

import it.multicoredev.computer.components.v3.gates.And;
import it.multicoredev.computer.components.v3.gates.Nor;
import it.multicoredev.computer.components.v3.gates.Or;
import it.multicoredev.computer.components.v3.gates.Xor;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Dabble extends ChipComponent {
    private final Xor xor0 = new Xor();
    private final Nor nor0 = new Nor();
    private final Xor xor1 = new Xor();

    private final Nor nor1 = new Nor();

    private final Nor nor2 = new Nor();
    private final Or or = new Or();

    private final Nor nor3 = new Nor();
    private final And and = new And();
    private final Xor xor2 = new Xor();

    public Dabble() {
        super("Dabble",
                new Pin[]{
                        new Pin("D3", Direction.INPUT),
                        new Pin("D2", Direction.INPUT),
                        new Pin("D1", Direction.INPUT),
                        new Pin("D0", Direction.INPUT),
                },
                new Pin[]{
                        new Pin("D3", Direction.OUTPUT),
                        new Pin("D2", Direction.OUTPUT),
                        new Pin("D1", Direction.OUTPUT),
                        new Pin("D0", Direction.OUTPUT),
                }
        );

        update();
    }

    @Override
    public void run() {
        xor0.in(inputState(3), inputState(0));
        nor0.in(inputState(0), inputState(1));
        xor1.in(inputState(0), inputState(2));

        nor1.in(xor0.out(), xor1.out());

        nor2.in(nor1.out(), nor0.out());
        or.in(nor0.out(), xor0.out());

        nor3.in(or.out(), inputState(2));
        and.in(or.out(), xor1.out());
        xor2.in(nor2.out(), inputState(3));

        outputs[0].state(nor2.out());
        outputs[1].state(nor3.out());
        outputs[2].state(and.out());
        outputs[3].state(xor2.out());
    }

    public Dabble in(State d3, State d2, State d1, State d0) {
        inputs[0].state(d3);
        inputs[1].state(d2);
        inputs[2].state(d1);
        inputs[3].state(d0);

        return this;
    }

    public State[] out() {
        return new State[]{outputs[0].state(), outputs[1].state(), outputs[2].state(), outputs[3].state()};
    }
}
