package it.multicoredev.computer.components.v3.registers;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Register8Bit extends ChipComponent {
    private final Register4Bit lowBites = new Register4Bit();
    private final Register4Bit highBites = new Register4Bit();

    public Register8Bit() {
        super("Register 8B",
                new Pin[]{
                        new Pin("D7", Direction.INPUT),
                        new Pin("D6", Direction.INPUT),
                        new Pin("D5", Direction.INPUT),
                        new Pin("D4", Direction.INPUT),
                        new Pin("D3", Direction.INPUT),
                        new Pin("D2", Direction.INPUT),
                        new Pin("D1", Direction.INPUT),
                        new Pin("D0", Direction.INPUT),
                        new Pin("En", Direction.INPUT),
                        new Pin("Clk", Direction.INPUT)
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
        lowBites.inData(inputState(0), inputState(1), inputState(2), inputState(3));
        lowBites.inEnable(inputState(8));
        lowBites.inClock(input(9).state());

        highBites.inData(inputState(4), inputState(5), inputState(6), inputState(7));
        highBites.inEnable(inputState(8));
        highBites.inClock(input(9).state());

        outputs[0].state(lowBites.output(0).state());
        outputs[1].state(lowBites.output(1).state());
        outputs[2].state(lowBites.output(2).state());
        outputs[3].state(lowBites.output(3).state());
        outputs[4].state(highBites.output(0).state());
        outputs[5].state(highBites.output(1).state());
        outputs[6].state(highBites.output(2).state());
        outputs[7].state(highBites.output(3).state());
    }

    public Pin[] pinDataIn() {
        return new Pin[]{input(0), input(1), input(2), input(3), input(4), input(5), input(6), input(7)};
    }

    public Pin pinEn() {
        return input(8);
    }

    public Pin pinClk() {
        return input(9);
    }

    public Pin[] pinDataOut() {
        return new Pin[]{output(0), output(1), output(2), output(3), output(4), output(5), output(6), output(7)};
    }

    public Register8Bit inData(State... data) {
        if (data.length != 8) throw new IllegalArgumentException("Data must be 8 bits long");

        input(0).state(data[0]);
        input(1).state(data[1]);
        input(2).state(data[2]);
        input(3).state(data[3]);
        input(4).state(data[4]);
        input(5).state(data[5]);
        input(6).state(data[6]);
        input(7).state(data[7]);
        return this;
    }

    public Register8Bit inEnable(State enable) {
        input(8).state(enable);
        return this;
    }

    public Register8Bit inClock(State clock) {
        input(9).state(clock);
        return this;
    }

    public State[] outData() {
        return new State[]{output(0).state(), output(1).state(), output(2).state(), output(3).state(), output(4).state(), output(5).state(), output(6).state(), output(7).state()};
    }
}
