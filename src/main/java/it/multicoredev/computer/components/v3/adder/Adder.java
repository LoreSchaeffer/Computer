package it.multicoredev.computer.components.v3.adder;

import it.multicoredev.computer.components.v3.gates.And;
import it.multicoredev.computer.components.v3.gates.Or;
import it.multicoredev.computer.components.v3.gates.Xor;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Adder extends ChipComponent {
    private final Xor xor0 = new Xor();
    private final Xor xor1 = new Xor();
    private final And and0 = new And();
    private final And and1 = new And();
    private final Or or = new Or();

    public Adder() {
        super("Adder",
                new Pin[]{
                        new Pin("A", Direction.INPUT),
                        new Pin("B", Direction.INPUT),
                        new Pin("Cin", Direction.INPUT),
                },
                new Pin[]{
                        new Pin("D", Direction.OUTPUT),
                        new Pin("Cout", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        xor0.in(inputState(0), inputState(1));
        and0.in(inputState(0), inputState(1));

        xor1.in(xor0.out(), inputState(2));
        and1.in(xor0.out(), inputState(2));

        or.in(and0.out(), and1.out());

        outputs[0].state(xor1.out());
        outputs[1].state(or.out());
    }

    public Pin a() {
        return inputs[0];
    }

    public Pin b() {
        return inputs[1];
    }

    public Pin carryIn() {
        return inputs[2];
    }

    public Pin pinSum() {
        return outputs[0];
    }

    public Pin pinCarryOut() {
        return outputs[1];
    }

    public Adder a(State state) {
        input(0).state(state);
        return this;
    }

    public Adder b(State state) {
        input(1).state(state);
        return this;
    }

    public Adder carryIn(State state) {
        input(2).state(state);
        return this;
    }

    public State sum() {
        return output(0).state();
    }

    public State carryOut() {
        return output(1).state();
    }
}
