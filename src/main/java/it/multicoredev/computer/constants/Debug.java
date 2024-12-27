package it.multicoredev.computer.constants;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.*;
import it.multicoredev.computer.components.gates.compound.MultiAnd;
import it.multicoredev.computer.components.gates.compound.MultiOr;
import it.multicoredev.computer.components.latches.DFlipFlop;
import it.multicoredev.computer.components.latches.DLatch;
import it.multicoredev.computer.components.registers.Register1Bit;

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
