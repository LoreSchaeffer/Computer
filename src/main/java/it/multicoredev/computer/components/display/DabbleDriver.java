package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.ChipComponent;
import it.multicoredev.computer.ui.SegmentDisplay;

import java.util.Arrays;

public class DabbleDriver extends ChipComponent {
    private final DoubleDabble dabble = new DoubleDabble();

    private final BlankingNegDriver bnDriver1 = new BlankingNegDriver();
    private final BlankingNegDriver bnDriver2 = new BlankingNegDriver();
    private final BlankingNegDriver bnDriver3 = new BlankingNegDriver();

    public DabbleDriver() {
        super(
                9,
                24,
                "Dabble Driver",
                new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "Blk"},
                new String[]{
                        "A1", "A2", "A3", "A4", "A5", "A6", "A7", "AS",
                        "B1", "B2", "B3", "B4", "B5", "B6", "B7", "BS",
                        "C1", "C2", "C3", "C4", "C5", "C6", "C7", "CS"
                }
        );
        run();
    }

    @Override
    protected void update() {
        dabble.in(Arrays.copyOfRange(in, 0, 8));

        bnDriver1.in(false, false, dabble.out()[0], dabble.out()[1], false, in[8]);
        bnDriver2.in(dabble.out()[2], dabble.out()[3], dabble.out()[4], dabble.out()[5], false, bnDriver1.outRippleBlanking());
        bnDriver3.in(dabble.out()[6], dabble.out()[7], dabble.out()[8], dabble.out()[9], false, bnDriver2.outRippleBlanking());

        System.arraycopy(bnDriver1.out(), 0, out, 0, 7);
        out[7] = bnDriver1.out()[9];
        System.arraycopy(bnDriver2.out(), 0, out, 8, 7);
        out[15] = bnDriver2.out()[9];
        System.arraycopy(bnDriver3.out(), 0, out, 16, 7);
        out[23] = bnDriver3.out()[9];
    }

    public void connect(SegmentDisplay display1, SegmentDisplay display2, SegmentDisplay display3) {
//        connect(display1, (out) -> {
//            display1.setSegments(Arrays.copyOfRange(out, 0, 7));
//            display1.setMinus(out[7]);
//        });
//        connect(display2, (out) -> {
//            display2.setSegments(Arrays.copyOfRange(out, 8, 15));
//            display2.setMinus(out[15]);
//        });
//        connect(display3, (out) -> {
//            display3.setSegments(Arrays.copyOfRange(out, 16, 23));
//            display3.setMinus(out[23]);
//        });
    }
}
