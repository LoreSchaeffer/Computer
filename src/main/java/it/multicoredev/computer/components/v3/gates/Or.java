package it.multicoredev.computer.components.v3.gates;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Or extends ChipComponent {
    private final Not not0 = new Not();
    private final Not not1 = new Not();
    private final Nand nand = new Nand();

    public Or() {
        super("OR",
                new Pin[]{
                        new Pin("A", Direction.INPUT),
                        new Pin("B", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("Out", Direction.OUTPUT)
                });

        update();
    }

    @Override
    public void run() {
        not0.in(input(0).state());
        not1.in(input(1).state());
        nand.in(not0.out(), not1.out());

        outputs[0].state(nand.out());
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

    public Or in(State a, State b) {
        pinA().state(a);
        pinB().state(b);
        return this;
    }

    public Or a(State state) {
        pinA().state(state);
        return this;
    }

    public Or b(State state) {
        pinB().state(state);
        return this;
    }

    public State out() {
        return pinOut().state();
    }
}
