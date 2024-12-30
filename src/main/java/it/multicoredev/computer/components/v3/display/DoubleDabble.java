package it.multicoredev.computer.components.v3.display;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class DoubleDabble extends ChipComponent {
    private final Dabble dabble0 = new Dabble();
    private final Dabble dabble1 = new Dabble();
    private final Dabble dabble2 = new Dabble();

    private final Dabble dabble3 = new Dabble();
    private final Dabble dabble4 = new Dabble();

    private final Dabble dabble5 = new Dabble();
    private final Dabble dabble6 = new Dabble();

    public DoubleDabble() {
        super("Double Dabble",
                new Pin[]{
                        new Pin("D7", Direction.INPUT),
                        new Pin("D6", Direction.INPUT),
                        new Pin("D5", Direction.INPUT),
                        new Pin("D4", Direction.INPUT),

                        new Pin("D3", Direction.INPUT),
                        new Pin("D2", Direction.INPUT),
                        new Pin("D1", Direction.INPUT),
                        new Pin("D0", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("D9", Direction.OUTPUT),
                        new Pin("D8", Direction.OUTPUT),

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
        dabble0.in(State.LOW, inputState(0), inputState(1), inputState(2));
        dabble1.in(dabble0.out()[1], dabble0.out()[2], dabble0.out()[3], inputState(3));
        dabble2.in(dabble1.out()[1], dabble1.out()[2], dabble1.out()[3], inputState(4));

        dabble3.in(State.LOW, dabble0.out()[0], dabble1.out()[0], dabble2.out()[0]);
        dabble4.in(dabble2.out()[1], dabble2.out()[2], dabble2.out()[3], inputState(5));

        dabble5.in(dabble3.out()[1], dabble3.out()[2], dabble3.out()[3], dabble4.out()[0]);
        dabble6.in(dabble4.out()[1], dabble4.out()[2], dabble4.out()[3], inputState(6));

        outputs[0].state(dabble3.out()[0]);
        setOutputs(1, dabble5.out());
        setOutputs(5, dabble6.out());
        outputs[9].state(inputState(7));
    }

    public DoubleDabble dataIn(State d7, State d6, State d5, State d4, State d3, State d2, State d1, State d0) {
        inputs[0].state(d7);
        inputs[1].state(d6);
        inputs[2].state(d5);
        inputs[3].state(d4);
        inputs[4].state(d3);
        inputs[5].state(d2);
        inputs[6].state(d1);
        inputs[7].state(d0);

        return this;
    }

    public State[] dataOut() {
        return new State[]{
            outputs[9].state(),
            outputs[8].state(),
            outputs[7].state(),
            outputs[6].state(),
            outputs[5].state(),
            outputs[4].state(),
            outputs[3].state(),
            outputs[2].state(),
            outputs[1].state(),
            outputs[0].state()
        };
    }
}
