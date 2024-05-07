package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.DualInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Xnor extends Component implements DualInputComponent, SingleOutputComponent {
    private final Xor xor = new Xor();
    private final Not not = new Not();

    private boolean a;
    private boolean b;
    private boolean out;

    @Override
    protected void run() {
        xor.in(a, b);
        not.in(xor.out());
        out = not.out();
    }

    @Override
    public Xnor in(boolean a, boolean b) {
        this.a = a;
        this.b = b;
        run();
        return this;
    }

    @Override
    public Xnor inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    @Override
    public Xnor inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
