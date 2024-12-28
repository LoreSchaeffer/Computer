package it.multicoredev.computer.components.v3.gates;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Nand extends ChipComponent {
    private final And and = new And();
    private final Not not = new Not();

    public Nand() {
        super("NAND",
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
        and.in(input(0).state(), input(1).state());
        not.in(and.out());

        outputs[0].state(not.out());
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

    public Nand in(State a, State b) {
        pinA().state(a);
        pinB().state(b);
        return this;
    }

    public Nand a(State state) {
        pinA().state(state);
        return this;
    }

    public Nand b(State state) {
        pinB().state(state);
        return this;
    }

    public State out() {
        return pinOut().state();
    }
}
