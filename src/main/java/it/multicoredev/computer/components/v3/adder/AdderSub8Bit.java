package it.multicoredev.computer.components.v3.adder;

import it.multicoredev.computer.components.v3.gates.Or;
import it.multicoredev.computer.components.v3.gates.Xor;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class AdderSub8Bit extends ChipComponent {
    private final Adder8Bit adder = new Adder8Bit();
    private final Or or = new Or();
    private final Xor xor0 = new Xor();
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Xor xor4 = new Xor();
    private final Xor xor5 = new Xor();
    private final Xor xor6 = new Xor();
    private final Xor xor7 = new Xor();

    public AdderSub8Bit() {
        super("Adder Sub 8B",
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

                        new Pin("Cin", Direction.INPUT),
                        new Pin("Sub", Direction.INPUT)
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

                        new Pin("Cout", Direction.OUTPUT),
                }
        );

        update();
    }

    @Override
    public void run() {
        xor7.in(inputState(8), inputState(17));
        xor6.in(inputState(9), inputState(17));
        xor5.in(inputState(10), inputState(17));
        xor4.in(inputState(11), inputState(17));
        xor3.in(inputState(12), inputState(17));
        xor2.in(inputState(13), inputState(17));
        xor1.in(inputState(14), inputState(17));
        xor0.in(inputState(15), inputState(17));

        or.in(inputState(16), inputState(17));

        adder.a(inputState(0), inputState(1), inputState(2), inputState(3), inputState(4), inputState(5), inputState(6), inputState(7))
                .b(xor7.out(), xor6.out(), xor5.out(), xor4.out(), xor3.out(), xor2.out(), xor1.out(), xor0.out())
                .carryIn(or.out());

        setOutputs(0, adder.sum());
        outputs[8].state(adder.carryOut());
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

    public Pin pinSub() {
        return inputs[17];
    }

    public Pin[] pinsSum() {
        return new Pin[]{outputs[7], outputs[6], outputs[5], outputs[4], outputs[3], outputs[2], outputs[1], outputs[0]};
    }

    public Pin pinCarryOut() {
        return outputs[8];
    }

    public AdderSub8Bit inA(State a7, State a6, State a5, State a4, State a3, State a2, State a1, State a0) {
        inputs[0].state(a7);
        inputs[1].state(a6);
        inputs[2].state(a5);
        inputs[3].state(a4);
        inputs[4].state(a3);
        inputs[5].state(a2);
        inputs[6].state(a1);
        inputs[7].state(a0);

        return this;
    }

    public AdderSub8Bit inB(State b7, State b6, State b5, State b4, State b3, State b2, State b1, State b0) {
        inputs[8].state(b7);
        inputs[9].state(b6);
        inputs[10].state(b5);
        inputs[11].state(b4);
        inputs[12].state(b3);
        inputs[13].state(b2);
        inputs[14].state(b1);
        inputs[15].state(b0);

        return this;
    }

    public AdderSub8Bit inCarry(State carry) {
        inputs[16].state(carry);

        return this;
    }

    public AdderSub8Bit inSub(State sub) {
        inputs[17].state(sub);

        return this;
    }

    public State[] result() {
        return new State[]{outputs[0].state(), outputs[1].state(), outputs[2].state(), outputs[3].state(), outputs[4].state(), outputs[5].state(), outputs[6].state(), outputs[7].state()};
    }

    public State carryOut() {
        return outputs[8].state();
    }
}
