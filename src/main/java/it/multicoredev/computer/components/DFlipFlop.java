package it.multicoredev.computer.components;

import it.multicoredev.computer.components.gates.Not;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class DFlipFlop extends Component implements SingleOutputComponent {
    private final Not not = new Not();
    private final DLatch latch1 = new DLatch();
    private final DLatch latch2 = new DLatch();

    private boolean data;
    private boolean enable;
    private boolean out;

    @Override
    protected void run() {
        not.in(enable);

        latch1.in(data, not.out());

        latch2.in(latch1.out(), enable);

        out = latch2.out();
    }

    public DFlipFlop in(boolean data, boolean enable) {
        this.data = data;
        this.enable = enable;
        run();

        return this;
    }

    public DFlipFlop inData(boolean data) {
        this.data = data;
        run();

        return this;
    }

    public DFlipFlop inEnable(boolean enable) {
        this.enable = enable;
        run();

        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
