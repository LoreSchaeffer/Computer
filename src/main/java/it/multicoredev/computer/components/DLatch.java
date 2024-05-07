package it.multicoredev.computer.components;

import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Nor;
import it.multicoredev.computer.components.gates.Not;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class DLatch extends Component implements SingleOutputComponent {
    private final And and1 = new And();
    private final And and2 = new And();
    private final Not not = new Not();
    private final Nor nor1 = new Nor();
    private final Nor nor2 = new Nor();

    private boolean data;
    private boolean enable;
    private boolean out;

    @Override
    protected void run() {
        not.in(data);

        and1.in(data, enable);
        and2.in(enable, not.out());

        nor1.in(and1.out(), nor2.out());
        nor2.in(and2.out(), nor1.out());

        out = nor2.out();
    }

    public DLatch in(boolean data, boolean enable) {
        this.data = data;
        this.enable = enable;
        run();

        return this;
    }

    public DLatch inData(boolean data) {
        this.data = data;
        run();

        return this;
    }

    public DLatch inEnable(boolean enable) {
        this.enable = enable;
        run();

        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
