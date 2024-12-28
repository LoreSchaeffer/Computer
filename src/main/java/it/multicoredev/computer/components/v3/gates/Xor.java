package it.multicoredev.computer.components.v3.gates;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Xor extends ChipComponent {
    private final Or or = new Or();
    private final Nand nand = new Nand();
    private final And and = new And();

    public Xor() {
        super("XOR",
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
        or.in(input(0).state(), input(1).state());
        nand.in(input(0).state(), input(1).state());
        and.in(or.out(), nand.out());

        output(0).state(and.out());
    }

    public Pin pinA() {
        return input(0);
    }

    public Pin pinB() {
        return input(1);
    }

    public Pin pinOut() {
        return output(0);
    }

    public Xor in(State a, State b) {
        pinA().state(a);
        pinB().state(b);
        return this;
    }

    public Xor a(State state) {
        pinA().state(state);
        return this;
    }

    public Xor b(State state) {
        pinB().state(state);
        return this;
    }

    public State out() {
        return pinOut().state();
    }
}
