package it.multicoredev.computer.components.v3.display;

import it.multicoredev.computer.components.v3.gates.*;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class SegDisplayDriver extends ChipComponent {
    private final Xor xor0 = new Xor();
    private final Not not = new Not();

    private final Nand nand0 = new Nand();

    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Or or0 = new Or();

    private final Or or1 = new Or();
    private final Or or2 = new Or();
    private final Or or3 = new Or();

    private final Nand nand1 = new Nand();
    private final Or or4 = new Or();
    private final And and0 = new And();
    private final And and1 = new And();
    private final And and2 = new And();
    private final And and3 = new And();

    public SegDisplayDriver() {
        super("7 Segment Display Driver",
                new Pin[]{
                        new Pin("D3", Direction.INPUT),
                        new Pin("D2", Direction.INPUT),
                        new Pin("D1", Direction.INPUT),
                        new Pin("D0", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("T", Direction.OUTPUT),
                        new Pin("TR", Direction.OUTPUT),
                        new Pin("BR", Direction.OUTPUT),
                        new Pin("B", Direction.OUTPUT),
                        new Pin("BL", Direction.OUTPUT),
                        new Pin("TL", Direction.OUTPUT),
                        new Pin("M", Direction.OUTPUT)
                }
        );

        update();
    }

    @Override
    public void run() {
        xor0.in(inputState(0), inputState(2));
        not.in(inputState(2));

        nand0.in(inputState(1), not.out());

        xor1.in(nand0.out(), inputState(3));
        xor2.in(inputState(1), xor0.out());
        or0.in(inputState(1), xor0.out());

        or1.in(xor0.out(), xor1.out());
        or2.in(inputState(1), not.out());
        or3.in(xor1.out(), xor2.out());

        nand1.in(xor1.out(), inputState(1));
        or4.in(or2.out(), inputState(3));
        and0.in(or1.out(), or3.out());
        and1.in(xor1.out(), nand0.out());
        and2.in(or2.out(), or3.out());
        and3.in(or3.out(), or0.out());

        outputs[0].state(or1.out());
        outputs[1].state(nand1.out());
        outputs[2].state(or4.out());
        outputs[3].state(and0.out());
        outputs[4].state(and1.out());
        outputs[5].state(and2.out());
        outputs[6].state(and3.out());
    }

    public State[] dataOut() {
        return new State[]{
                outputs[0].state(),
                outputs[1].state(),
                outputs[2].state(),
                outputs[3].state(),
                outputs[4].state(),
                outputs[5].state(),
                outputs[6].state()
        };
    }
}
