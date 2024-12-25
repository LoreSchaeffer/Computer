package it.multicoredev.computer.components.display.dabble;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Nor;
import it.multicoredev.computer.components.gates.Or;
import it.multicoredev.computer.components.gates.Xor;

public class Dabble extends Component {
    private final Xor xor1 = new Xor();
    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();
    private final Nor nor1 = new Nor();
    private final Nor nor2 = new Nor();
    private final Nor nor3 = new Nor();
    private final Nor nor4 = new Nor();
    private final Or or = new Or();
    private final And and = new And();

    private boolean[] in = new boolean[4];
    private boolean[] out = new boolean[4];

    @Override
    protected void run() {
        xor1.in(in[0], in[3]);
        nor1.in(in[0], in[1]);
        xor2.in(in[0], in[2]);

        nor2.in(xor1.out(), xor2.out());

        nor3.in(nor2.out(), nor1.out());
        or.in(nor1.out(), xor1.out());

        nor4.in(or.out(), in[2]);
        and.in(or.out(), xor2.out());

        xor3.in(nor3.out(), in[3]);

        out = new boolean[]{
                nor3.out(),
                nor4.out(),
                and.out(),
                xor3.out()
        };
    }

    public void in(boolean[] in) {
        this.in = in;
        run();
    }

    public boolean[] out() {
        return out;
    }
}
