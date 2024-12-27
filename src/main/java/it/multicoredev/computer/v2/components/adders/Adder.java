package it.multicoredev.computer.v2.components.adders;

import it.multicoredev.computer.v2.components.ChipComponent;
import it.multicoredev.computer.v2.components.gates.And;
import it.multicoredev.computer.v2.components.gates.Or;
import it.multicoredev.computer.v2.components.gates.Xor;

public class Adder extends ChipComponent {
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final And and1 = new And();
    private final And and2 = new And();
    private final Or or = new Or();

    public Adder() {
        super(3, 2, "Adder", new String[]{"A", "B", "Cin"}, new String[]{"Sum", "Cout"});
        run();
    }

    @Override
    protected void update() {
        xor1.in(in[0], in[1]);
        and1.in(in[0], in[1]);

        xor2.in(xor1.out()[0], in[2]);
        and2.in(xor1.out()[0], in[2]);

        or.in(and1.out()[0], and2.out()[0]);

        out[0] = xor2.out()[0];
        out[1] = or.out()[0];
    }

    public Adder inA(boolean a) {
        in[0] = a;
        run();
        return this;
    }

    public Adder inB(boolean b) {
        in[1] = b;
        run();
        return this;
    }

    public Adder inCarry(boolean carry) {
        in[2] = carry;
        run();
        return this;
    }

    public boolean sum() {
        return out[0];
    }

    public boolean carry() {
        return out[1];
    }
}
