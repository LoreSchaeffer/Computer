package it.multicoredev.computer.components.adders;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.gates.Xor;

public class Adder extends Component {
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final And and1 = new And();
    private final And and2 = new And();
    private final Or or = new Or();

    private boolean a;
    private boolean b;
    private boolean carryIn;
    private boolean sum;
    private boolean carry;

    @Override
    protected void run() {
        xor1.in(a, b);
        and1.in(a, b);

        xor2.in(xor1.out(), carryIn);
        and2.in(xor1.out(), carryIn);

        or.in(and1.out(), and2.out());

        sum = xor2.out();
        carry = or.out();
    }

    public boolean sum() {
        return sum;
    }

    public boolean carry() {
        return carry;
    }

    public Adder in(boolean a, boolean b, boolean carryIn) {
        this.a = a;
        this.b = b;
        this.carryIn = carryIn;
run();
        return this;
    }

    public Adder inA(boolean a) {
        this.a = a;
run();
        return this;
    }

    public Adder inB(boolean b) {
        this.b = b;
run();
        return this;
    }

    public Adder carryIn(boolean carryIn) {
        this.carryIn = carryIn;
run();
        return this;
    }
}
