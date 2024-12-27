package it.multicoredev.computer.components.latches;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.gates.And;
import it.multicoredev.computer.components.gates.Nor;
import it.multicoredev.computer.components.gates.Not;

public class DLatch extends ChipComponent {
    private final Not not = new Not();
    private final And and1 = new And();
    private final And and2 = new And();
    private final Nor nor1 = new Nor();
    private final Nor nor2 = new Nor();

    public DLatch() {
        super(
                2,
                2,
                "DLatch",
                new String[]{"D", "En"},
                new String[]{"Q", "Qn"}
        );
        run();
    }

    @Override
    protected void update() {
        not.in(in[0]);

        and1.in(not.out()[0], in[1]);
        and2.in(in[0], in[1]);

        nor1.in(and1.out()[0], nor2.out()[0]);
        nor2.in(and2.out()[0], nor1.out()[0]);

        out[0] = nor1.out()[0];
        out[1] = nor2.out()[0];
    }

    public boolean q() {
        return out[0];
    }

    public boolean qn() {
        return out[1];
    }
}
