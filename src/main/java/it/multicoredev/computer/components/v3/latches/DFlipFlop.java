package it.multicoredev.computer.components.v3.latches;

import it.multicoredev.computer.components.v3.gates.Not;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class DFlipFlop extends ChipComponent {
    private final Not not = new Not();
    private final DLatch latch0 = new DLatch();
    private final DLatch latch1 = new DLatch();

    public DFlipFlop() {
        super("D Flip-Flop",
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
        not.in(inputState(1));
        latch0.in(inputState(0), not.out());
        latch1.in(latch0.pinQ().state(), inputState(1));

        output(0).state(latch1.pinQ().state());
        output(1).state(latch1.pinQn().state());
    }

    public Pin pinD() {
        return input(0);
    }

    public Pin pinEn() {
        return input(1);
    }

    public Pin pinQ() {
        return output(0);
    }

    public Pin pinQn() {
        return output(1);
    }

    public DFlipFlop in(State d, State en) {
        input(0).state(d);
        input(1).state(en);
        return this;
    }

    public DFlipFlop d(State d) {
        input(0).state(d);
        return this;
    }

    public DFlipFlop en(State en) {
        input(1).state(en);
        return this;
    }

    public State q() {
        return output(0).state();
    }

    public State qn() {
        return output(1).state();
    }
}
