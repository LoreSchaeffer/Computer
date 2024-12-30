package it.multicoredev.computer.components.v3.latches;

import it.multicoredev.computer.components.v3.gates.And;
import it.multicoredev.computer.components.v3.gates.Nor;
import it.multicoredev.computer.components.v3.gates.Not;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class DLatch extends ChipComponent {
    private final Not not = new Not();
    private final And and0 = new And();
    private final And and1 = new And();
    private final Nor nor0 = new Nor();
    private final Nor nor1 = new Nor();

    public DLatch() {
        super("DLatch",
                new Pin[]{
                        new Pin("D", Direction.INPUT),
                        new Pin("En", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("Q", Direction.OUTPUT),
                        new Pin("Qn", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        not.in(inputState(0));
        and0.in(not.out(), inputState(1));
        and1.in(inputState(0), inputState(1));
        nor0.in(and0.out(), nor1.out());
        nor1.in(and1.out(), nor0.out());

        nor0.in(and0.out(), nor1.out());
        nor1.in(and1.out(), nor0.out());

        outputs[0].state(nor0.out());
        outputs[1].state(nor1.out());
    }

    public Pin pinD() {
        return inputs[0];
    }

    public Pin pinEn() {
        return inputs[1];
    }

    public Pin pinQ() {
        return outputs[0];
    }

    public Pin pinQn() {
        return outputs[1];
    }

    public DLatch in(State d, State en) {
        pinD().state(d);
        pinEn().state(en);
        return this;
    }

    public DLatch d(State state) {
        pinD().state(state);
        return this;
    }

    public DLatch en(State state) {
        pinEn().state(state);
        return this;
    }

    public State q() {
        return pinQ().state();
    }

    public State qn() {
        return pinQn().state();
    }
}
