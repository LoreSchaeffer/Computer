package it.multicoredev.computer.components.latches;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.components.gates.Not;

public class DFlipFlop extends ChipComponent {
    private final Not not = new Not();
    private final DLatch latch1 = new DLatch();
    private final DLatch latch2 = new DLatch();

    public DFlipFlop() {
        super(
                2,
                2,
                "D Flip-Flop",
                new String[]{"D", "En"},
                new String[]{"Q", "Qn"}
        );
        run();
    }

    @Override
    protected void update() {
        not.in(in[1]);

        latch1.in(in[0], not.out()[0]);
        latch2.in(latch1.q(), in[1]);

        out[0] = latch2.q();
        out[1] = latch2.qn();
    }

    public boolean q() {
        return out[0];
    }

    public boolean qn() {
        return out[1];
    }
}
