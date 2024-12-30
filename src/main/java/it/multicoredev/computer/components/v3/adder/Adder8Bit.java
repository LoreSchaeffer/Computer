package it.multicoredev.computer.components.v3.adder;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Adder8Bit extends ChipComponent {
    private final Adder4Bit lowAdder = new Adder4Bit();
    private final Adder4Bit highAdder = new Adder4Bit();

    public Adder8Bit() {
        super("Adder 8B",
                new Pin[]{
                        new Pin("A7", Direction.INPUT),
                        new Pin("A6", Direction.INPUT),
                        new Pin("A5", Direction.INPUT),
                        new Pin("A4", Direction.INPUT),
                        new Pin("A3", Direction.INPUT),
                        new Pin("A2", Direction.INPUT),
                        new Pin("A1", Direction.INPUT),
                        new Pin("A0", Direction.INPUT),

                        new Pin("B7", Direction.INPUT),
                        new Pin("B6", Direction.INPUT),
                        new Pin("B5", Direction.INPUT),
                        new Pin("B4", Direction.INPUT),
                        new Pin("B3", Direction.INPUT),
                        new Pin("B2", Direction.INPUT),
                        new Pin("B1", Direction.INPUT),
                        new Pin("B0", Direction.INPUT),

                        new Pin("Cin", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("D7", Direction.OUTPUT),
                        new Pin("D6", Direction.OUTPUT),
                        new Pin("D5", Direction.OUTPUT),
                        new Pin("D4", Direction.OUTPUT),
                        new Pin("D3", Direction.OUTPUT),
                        new Pin("D2", Direction.OUTPUT),
                        new Pin("D1", Direction.OUTPUT),
                        new Pin("D0", Direction.OUTPUT),

                        new Pin("Cout", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        lowAdder.a(inputState(7), inputState(6), inputState(5), inputState(4))
                .b(inputState(15), inputState(14), inputState(13), inputState(12))
                .carryIn(inputState(16));
        highAdder.a(inputState(3), inputState(2), inputState(1), inputState(0))
                .b(inputState(11), inputState(10), inputState(9), inputState(8))
                .carryIn(lowAdder.carryOut());

        setOutputs(4, lowAdder.sum());
        setOutputs(0, highAdder.sum());
        outputs[8].state(highAdder.carryOut());
    }

    public Pin[] pinsA() {
        return new Pin[]{inputs[7], inputs[6], inputs[5], inputs[4], inputs[3], inputs[2], inputs[1], inputs[0]};
    }

    public Pin[] pinsB() {
        return new Pin[]{inputs[15], inputs[14], inputs[13], inputs[12], inputs[11], inputs[10], inputs[9], inputs[8]};
    }

    public Pin pinCarryIn() {
        return inputs[16];
    }

    public Pin[] pinsSum() {
        return new Pin[]{outputs[7], outputs[6], outputs[5], outputs[4], outputs[3], outputs[2], outputs[1], outputs[0]};
    }

    public Pin pinCarryOut() {
        return outputs[8];
    }

    public Adder8Bit a(State a7, State a6, State a5, State a4, State a3, State a2, State a1, State a0) {
        inputs[7].state(a0);
        inputs[6].state(a1);
        inputs[5].state(a2);
        inputs[4].state(a3);
        inputs[3].state(a4);
        inputs[2].state(a5);
        inputs[1].state(a6);
        inputs[0].state(a7);

        return this;
    }

    public Adder8Bit b(State b7, State b6, State b5, State b4, State b3, State b2, State b1, State b0) {
        inputs[15].state(b0);
        inputs[14].state(b1);
        inputs[13].state(b2);
        inputs[12].state(b3);
        inputs[11].state(b4);
        inputs[10].state(b5);
        inputs[9].state(b6);
        inputs[8].state(b7);

        return this;
    }

    public Adder8Bit carryIn(State carryIn) {
        inputs[16].state(carryIn);
        return this;
    }

    public State[] sum() {
        return new State[]{outputs[0].state(), outputs[1].state(), outputs[2].state(), outputs[3].state(), outputs[4].state(), outputs[5].state(), outputs[6].state(), outputs[7].state()};
    }

    public State carryOut() {
        return outputs[8].state();
    }
}
