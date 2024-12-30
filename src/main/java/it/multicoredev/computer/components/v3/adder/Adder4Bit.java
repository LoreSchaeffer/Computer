package it.multicoredev.computer.components.v3.adder;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Adder4Bit extends ChipComponent {
    private final Adder adder0 = new Adder();
    private final Adder adder1 = new Adder();
    private final Adder adder2 = new Adder();
    private final Adder adder3 = new Adder();

    public Adder4Bit() {
        super("Adder 4B",
                new Pin[]{
                        new Pin("A3", Direction.INPUT),
                        new Pin("A2", Direction.INPUT),
                        new Pin("A1", Direction.INPUT),
                        new Pin("A0", Direction.INPUT),

                        new Pin("B3", Direction.INPUT),
                        new Pin("B2", Direction.INPUT),
                        new Pin("B1", Direction.INPUT),
                        new Pin("B0", Direction.INPUT),

                        new Pin("Cin", Direction.INPUT)
                },
                new Pin[]{
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
        adder0.a(inputState(3)).b(inputState(7)).carryIn(inputState(8));
        adder1.a(inputState(2)).b(inputState(6)).carryIn(adder0.carryOut());
        adder2.a(inputState(1)).b(inputState(5)).carryIn(adder1.carryOut());
        adder3.a(inputState(0)).b(inputState(4)).carryIn(adder2.carryOut());

        outputs[0].state(adder3.sum());
        outputs[1].state(adder2.sum());
        outputs[2].state(adder1.sum());
        outputs[3].state(adder0.sum());
        outputs[4].state(adder3.carryOut());
    }

    public Pin[] pinsA() {
        return new Pin[]{inputs[3], inputs[2], inputs[1], inputs[0]};
    }

    public Pin[] pinsB() {
        return new Pin[]{inputs[7], inputs[6], inputs[5], inputs[4]};
    }

    public Pin pinCarryIn() {
        return inputs[8];
    }

    public Pin[] pinsSum() {
        return new Pin[]{outputs[3], outputs[2], outputs[1], outputs[0]};
    }

    public Pin pinCarryOut() {
        return outputs[4];
    }

    public Adder4Bit a(State a0, State a1, State a2, State a3) {
        input(3).state(a0);
        input(2).state(a1);
        input(1).state(a2);
        input(0).state(a3);

        return this;
    }

    public Adder4Bit b(State b0, State b1, State b2, State b3) {
        input(7).state(b0);
        input(6).state(b1);
        input(5).state(b2);
        input(4).state(b3);

        return this;
    }

    public Adder4Bit carryIn(State carryIn) {
        inputs[8].state(carryIn);

        return this;
    }

    public State[] sum() {
        return new State[]{outputs[0].state(), outputs[1].state(), outputs[2].state(), outputs[3].state()};
    }

    public State carryOut() {
        return outputs[4].state();
    }
}
