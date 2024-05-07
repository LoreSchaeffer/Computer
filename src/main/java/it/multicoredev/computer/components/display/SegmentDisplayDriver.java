package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.gates.*;
import it.multicoredev.computer.util.Utils;

public class SegmentDisplayDriver extends Component {
    private final Xor xor1 = new Xor();
    private final Not not = new Not();

    private final Nand nand1 = new Nand();
    private final Or or1 = new Or();

    private final Xor xor2 = new Xor();
    private final Xor xor3 = new Xor();

    private final Or or2 = new Or();
    private final Or or3 = new Or();
    private final Or or4 = new Or();

    private final Nand nand2 = new Nand();
    private final Or or5 = new Or();
    private final And and1 = new And();
    private final And and2 = new And();
    private final And and3 = new And();
    private final And and4 = new And();

    private boolean[] data;
    private boolean[] displayData;

    @Override
    protected void run() {
        xor1.in(data[3], data[1]);
        not.in(data[1]);

        nand1.in(data[2], not.out());
        or1.in(data[2], xor1.out());

        xor2.in(nand1.out(), data[0]);
        xor3.in(data[2], xor1.out());

        or2.in(xor1.out(), xor2.out());
        or3.in(data[2], not.out());
        or4.in(xor2.out(), xor3.out());

        nand2.in(xor2.out(), data[2]);
        or5.in(or3.out(), data[0]);
        and1.in(or2.out(), or4.out());
        and2.in(xor2.out(), nand1.out());
        and3.in(or3.out(), or4.out());
        and4.in(or4.out(), or1.out());

        displayData = new boolean[]{or2.out(), nand2.out(), or5.out(), and1.out(), and2.out(), and3.out(), and4.out()};
    }

    public SegmentDisplayDriver in(boolean[] data) {
        if (data.length != 4) throw new IllegalArgumentException("Data must be 4 bits long");

        this.data = Utils.flip(data);
        run();
        return this;
    }

    public boolean[] out() {
        return displayData;
    }
}
