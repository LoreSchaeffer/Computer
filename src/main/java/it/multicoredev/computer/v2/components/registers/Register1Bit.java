package it.multicoredev.computer.v2.components.registers;

import it.multicoredev.computer.v2.components.ChipComponent;
import it.multicoredev.computer.v2.components.TickingComponent;
import it.multicoredev.computer.v2.components.gates.And;
import it.multicoredev.computer.v2.components.gates.Not;
import it.multicoredev.computer.v2.components.gates.Or;
import it.multicoredev.computer.v2.components.latches.DFlipFlop;

public class Register1Bit extends ChipComponent implements TickingComponent {
    private final Not not = new Not();
    private final And and1 = new And();
    private final And and2 = new And();
    private final Or or = new Or();
    private final DFlipFlop flipFlop = new DFlipFlop();

    public Register1Bit() {
        super(
                3,
                1,
                "Register 1B",
                new String[]{"D", "En", "Clk"},
                new String[]{"Q"}
        );
        run();
    }

    @Override
    protected void update() {
        not.in(in[1]);

        and1.in(flipFlop.q(), not.out()[0]);
        and2.in(in[0], in[1]);

        or.in(and1.out()[0], and2.out()[0]);

        flipFlop.in(or.out()[0], in[2]);

        out[0] = flipFlop.q();
    }

    public Register1Bit data(boolean data) {
        in[0] = data;
        run();
        return this;
    }

    public Register1Bit enable(boolean enable) {
        in[1] = enable;
        run();
        return this;
    }

    @Override
    public Register1Bit clock(boolean clock) {
        in[2] = clock;
        run();
        return this;
    }

    public boolean q() {
        return out[0];
    }
}
