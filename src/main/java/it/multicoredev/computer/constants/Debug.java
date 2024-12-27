package it.multicoredev.computer.constants;

import it.multicoredev.computer.v2.components.Component;
import it.multicoredev.computer.v2.components.gates.*;
import it.multicoredev.computer.v2.components.gates.compound.MultiAnd;
import it.multicoredev.computer.v2.components.gates.compound.MultiOr;
import it.multicoredev.computer.v2.components.latches.DFlipFlop;
import it.multicoredev.computer.v2.components.latches.DLatch;
import it.multicoredev.computer.v2.components.registers.Register1Bit;

import java.util.List;

public class Debug {
    public static final List<Class<? extends Component>> LOG_DISABLED = List.of(
            And.class,
            Nand.class,
            Nor.class,
            Not.class,
            Or.class,
            Xnor.class,
            Xor.class,
            MultiAnd.class,
            MultiOr.class,
            DLatch.class,
            DFlipFlop.class,
            Register1Bit.class
    );
}
