package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.DualInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Xor extends Component implements DualInputComponent, SingleOutputComponent {
    private final Or or = new Or();
    private final Nand nand = new Nand();
    private final And and = new And();

    private boolean a;
    private boolean b;
    private boolean out;

    @Override
    protected void run() {
        or.in(a, b);
        nand.in(a, b);
        and.in(or.out(), nand.out());
        out = and.out();
    }

    @Override
    public Xor in(boolean a, boolean b) {
        this.a = a;
        this.b = b;
        run();
        return this;
    }

    @Override
    public Xor inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    @Override
    public Xor inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
