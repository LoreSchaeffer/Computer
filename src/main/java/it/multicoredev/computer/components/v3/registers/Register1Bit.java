package it.multicoredev.computer.components.v3.registers;

import it.multicoredev.computer.components.v3.gates.And;
import it.multicoredev.computer.components.v3.gates.Not;
import it.multicoredev.computer.components.v3.gates.Or;
import it.multicoredev.computer.components.v3.latches.DFlipFlop;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class Register1Bit extends ChipComponent {
    private final Not not = new Not();
    private final And and0 = new And();
    private final And and1 = new And();
    private final Or or = new Or();
    private final DFlipFlop flipFlop = new DFlipFlop();

    public Register1Bit() {
        super("Register 1B",
                new Pin[]{
                        new Pin("D", Direction.INPUT),
                        new Pin("En", Direction.INPUT),
                        new Pin("Clk", Direction.INPUT)
                },
                new Pin[]{
                        new Pin("D", Direction.OUTPUT)
                }
                );

        update();
    }

    @Override
    public void run() {
        not.in(inputState(1));

        and0.in(flipFlop.q(), not.out());
        and1.in(inputState(0), inputState(1));

        or.in(and0.out(), and1.out());

        flipFlop.in(or.out(), inputState(2));

        outputs[0].state(flipFlop.q());
    }

    public Pin pinDIn() {
        return input(0);
    }

    public Pin pinEn() {
        return input(1);
    }

    public Pin pinClk() {
        return input(2);
    }

    public Pin pinDOut() {
        return output(0);
    }

    public Register1Bit in(State d, State en, State clk) {
        input(0).state(d);
        input(1).state(en);
        input(2).state(clk);
        return this;
    }

    public Register1Bit d(State d) {
        input(0).state(d);
        return this;
    }

    public Register1Bit en(State en) {
        input(1).state(en);
        return this;
    }

    public Register1Bit clk(State clk) {
        input(2).state(clk);
        return this;
    }

    public State d() {
        return output(0).state();
    }
}
