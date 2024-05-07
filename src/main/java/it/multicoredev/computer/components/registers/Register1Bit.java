package it.multicoredev.computer.components.registers;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.DFlipFlop;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Not;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.types.SingleOutputComponent;
import it.multicoredev.computer.util.ClockListener;

public class Register1Bit extends Component implements SingleOutputComponent, ClockListener {
    private final Not not = new Not();
    private final And and1 = new And();
    private final And and2 = new And();
    private final Or or = new Or();
    private final DFlipFlop flipFlop = new DFlipFlop();

    private boolean data;
    private boolean enable;
    private boolean clock;
    private boolean out;

    @Override
    protected void run() {
        not.in(enable);

        and1.in(flipFlop.out(), not.out());
        and2.in(data, enable);

        or.in(and1.out(), and2.out());

        flipFlop.in(or.out(), clock);

        out = flipFlop.out();
    }

    public Register1Bit in(boolean data, boolean enable) {
        this.data = data;
        this.enable = enable;
        run();
        return this;
    }

    public Register1Bit inData(boolean data) {
        this.data = data;
        run();
        return this;
    }

    public Register1Bit inEnable(boolean enable) {
        this.enable = enable;
        run();
        return this;
    }

    @Override
    public void tick(boolean clock) {
        this.clock = clock;
        run();
    }

    @Override
    public boolean out() {
        return out;
    }
}
