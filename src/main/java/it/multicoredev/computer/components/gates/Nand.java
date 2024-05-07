package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.DualInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Nand extends Component implements DualInputComponent, SingleOutputComponent {
    private final And and = new And();
    private final Not not = new Not();

    private boolean a;
    private boolean b;
    private boolean out;

    @Override
    protected void run() {
        and.in(a, b);
        not.in(and.out());
        out = not.out();
    }

    @Override
    public Nand in(boolean a, boolean b) {
        this.a = a;
        this.b = b;
        run();
        return this;
    }

    @Override
    public Nand inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    @Override
    public Nand inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
