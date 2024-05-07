package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.DualInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Or extends Component implements DualInputComponent, SingleOutputComponent {
    private final Not not1 = new Not();
    private final Not not2 = new Not();
    private final Nand nand = new Nand();

    private boolean a;
    private boolean b;
    private boolean out;

    @Override
    protected void run() {
        not1.in(a);
        not2.in(b);
        nand.in(not1.out(), not2.out());
        out = nand.out();
    }

    @Override
    public Or in(boolean a, boolean b) {
        this.a = a;
        this.b = b;
        run();
        return this;
    }

    @Override
    public Or inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    @Override
    public Or inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
