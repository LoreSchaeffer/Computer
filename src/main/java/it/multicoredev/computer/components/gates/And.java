package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.DualInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class And extends Component implements DualInputComponent, SingleOutputComponent {
    private boolean a;
    private boolean b;
    private boolean out;

    @Override
    protected void run() {
        out = a && b;
    }

    @Override
    public And in(boolean a, boolean b) {
        this.a = a;
        this.b = b;
        run();
        return this;
    }

    @Override
    public And inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    @Override
    public And inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
