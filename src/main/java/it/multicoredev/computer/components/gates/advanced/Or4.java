package it.multicoredev.computer.components.gates.advanced;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Or4 extends Component implements SingleOutputComponent {
    private final Or or1 = new Or();
    private final Or or2 = new Or();
    private final Or or3 = new Or();

    private boolean a;
    private boolean b;
    private boolean c;
    private boolean d;
    private boolean out;

    @Override
    protected void run() {
        or1.in(a, b);
        or2.in(c, d);

        or3.in(or1.out(), or2.out());

        out = or3.out();
    }

    public Or4 in(boolean a, boolean b, boolean c, boolean d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        run();
        return this;
    }

    public Or4 in(boolean[] in) {
        if (in.length != 4) throw new IllegalArgumentException("Input length must be 4");
        return in(in[0], in[1], in[2], in[3]);
    }

    public Or4 inA(boolean a) {
        this.a = a;
        run();
        return this;
    }

    public Or4 inB(boolean b) {
        this.b = b;
        run();
        return this;
    }

    public Or4 inC(boolean c) {
        this.c = c;
        run();
        return this;
    }

    public Or4 inD(boolean d) {
        this.d = d;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
