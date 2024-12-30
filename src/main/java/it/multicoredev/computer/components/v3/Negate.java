package it.multicoredev.computer.components.v3;

import it.multicoredev.computer.components.v3.adder.Adder8Bit;
import it.multicoredev.computer.components.v3.gates.Xor;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Negate extends ChipComponent {
    private final Xor xor0 = new Xor();
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Xor xor4 = new Xor();
    private final Xor xor5 = new Xor();
    private final Xor xor6 = new Xor();
    private final Xor xor7 = new Xor();

    private final Adder8Bit adder = new Adder8Bit();

    public Negate() {
        super("Negate",
                new Pin[]{
                        new Pin("D7", Direction.INPUT),
                        new Pin("D6", Direction.INPUT),
                        new Pin("D5", Direction.INPUT),
                        new Pin("D4", Direction.INPUT),
                        new Pin("D3", Direction.INPUT),
                        new Pin("D2", Direction.INPUT),
                        new Pin("D1", Direction.INPUT),
                        new Pin("D0", Direction.INPUT),

                        new Pin("En", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("D7", Direction.OUTPUT),
                        new Pin("D6", Direction.OUTPUT),
                        new Pin("D5", Direction.OUTPUT),
                        new Pin("D4", Direction.OUTPUT),
                        new Pin("D3", Direction.OUTPUT),
                        new Pin("D2", Direction.OUTPUT),
                        new Pin("D1", Direction.OUTPUT),
                        new Pin("D0", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        xor7.in(inputState(0), inputState(8));
        xor6.in(inputState(1), inputState(8));
        xor5.in(inputState(2), inputState(8));
        xor4.in(inputState(3), inputState(8));
        xor3.in(inputState(4), inputState(8));
        xor2.in(inputState(5), inputState(8));
        xor1.in(inputState(6), inputState(8));
        xor0.in(inputState(7), inputState(8));

        adder.a(xor7.out(), xor6.out(), xor5.out(), xor4.out(), xor3.out(), xor2.out(), xor1.out(), xor0.out())
                .carryIn(inputState(8));

        setOutputs(0, adder.sum());
    }

    public Pin[] pinsDataIn() {
        return new Pin[]{inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5], inputs[6], inputs[7]};
    }

    public Pin pinEnable() {
        return inputs[8];
    }

    public Pin[] pinsDataOut() {
        return new Pin[]{outputs[0], outputs[1], outputs[2], outputs[3], outputs[4], outputs[5], outputs[6], outputs[7]};
    }

    public Negate dataIn(State d7, State d6, State d5, State d4, State d3, State d2, State d1, State d0) {
        inputs[0].state(d0);
        inputs[1].state(d1);
        inputs[2].state(d2);
        inputs[3].state(d3);
        inputs[4].state(d4);
        inputs[5].state(d5);
        inputs[6].state(d6);
        inputs[7].state(d7);

        return this;
    }

    public Negate enable(State en) {
        inputs[8].state(en);

        return this;
    }

    public State[] dataOut() {
        return new State[]{outputs[0].state(), outputs[1].state(), outputs[2].state(), outputs[3].state(), outputs[4].state(), outputs[5].state(), outputs[6].state(), outputs[7].state()};
    }
}
