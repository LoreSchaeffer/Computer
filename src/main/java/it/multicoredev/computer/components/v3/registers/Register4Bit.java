package it.multicoredev.computer.components.v3.registers;

import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Register4Bit extends ChipComponent {
    private final Register1Bit bit0 = new Register1Bit();
    private final Register1Bit bit1 = new Register1Bit();
    private final Register1Bit bit2 = new Register1Bit();
    private final Register1Bit bit3 = new Register1Bit();

    public Register4Bit() {
        super("Register 4B",
                new Pin[]{
                        new Pin("D3", Direction.INPUT),
                        new Pin("D2", Direction.INPUT),
                        new Pin("D1", Direction.INPUT),
                        new Pin("D0", Direction.INPUT),
                        new Pin("En", Direction.INPUT),
                        new Pin("Clk", Direction.INPUT)
                },
                new Pin[]{
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
        bit0.in(inputState(0), inputState(4), inputState(5));
        bit1.in(inputState(1), inputState(4), inputState(5));
        bit2.in(inputState(2), inputState(4), inputState(5));
        bit3.in(inputState(3), inputState(4), inputState(5));

        outputs[0].state(bit0.d());
        outputs[1].state(bit1.d());
        outputs[2].state(bit2.d());
        outputs[3].state(bit3.d());
    }

    public Pin[] pinDataIn() {
        return new Pin[]{input(0), input(1), input(2), input(3)};
    }

    public Pin pinEn() {
        return input(4);
    }

    public Pin pinClk() {
        return input(5);
    }

    public Pin[] pinDataOut() {
        return new Pin[]{output(0), output(1), output(2), output(3)};
    }

    public Register4Bit inData(State d0, State d1, State d2, State d3) {
        input(3).state(d0);
        input(2).state(d1);
        input(1).state(d2);
        input(0).state(d3);
        return this;
    }

    public Register4Bit inEnable(State enable) {
        input(4).state(enable);
        return this;
    }

    public Register4Bit inClock(State clock) {
        input(5).state(clock);
        return this;
    }

    public State[] outData() {
        return new State[]{output(0).state(), output(1).state(), output(2).state(), output(3).state()};
    }
}
