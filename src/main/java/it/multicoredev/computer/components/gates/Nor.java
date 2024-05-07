package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.DualInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Nor extends Component implements DualInputComponent, SingleOutputComponent {
    private final Or or = new Or();
    private final Not not = new Not();

    private boolean a;
    private boolean b;
    private boolean out;

    @Override
    protected void run() {
        or.in(a, b);
        not.in(or.out());
        out = not.out();
    }

    @Override
    public Nor in(boolean a, boolean b) {
        this.a = a;
        this.b = b;
        run();
        return this;
    }

    @Override
    public Nor inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    @Override
    public Nor inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
