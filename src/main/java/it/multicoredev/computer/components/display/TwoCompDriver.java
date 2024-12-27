//package it.multicoredev.computer.components.display;
//
//import it.multicoredev.computer.components.Component;
//import it.multicoredev.computer.components.Negate;
//import it.multicoredev.computer.components.display.dabble.DoubleDabble;
//import it.multicoredev.computer.components.gates.And;
//
//import java.util.Arrays;
//
//public class TwoCompDriver extends Component {
//    private final And and = new And();
//    private final Negate negate = new Negate();
//    private final DoubleDabble doubleDabble = new DoubleDabble();
//    private final BlankingNegativeDriver blankingNegDriver1 = new BlankingNegativeDriver();
//    private final BlankingNegativeDriver blankingNegDriver2 = new BlankingNegativeDriver();
//    private final BlankingNegativeDriver blankingNegDriver3 = new BlankingNegativeDriver();
//
//    private boolean twosComplimen = false;
//    private boolean[] in = new boolean[8];
//    private boolean blanking = false;
//    private boolean[] out = new boolean[24];
//
//    @Override
//    protected void run() {
//        and.in(in[0], twosComplimen);
//
//        negate.in(in, and.out());
//
//        doubleDabble.in(negate.out());
//
//        blankingNegDriver1.in(Arrays.copyOfRange(doubleDabble.out(), 0, 4), and.out(), blanking);
//        blankingNegDriver2.in(Arrays.copyOfRange(doubleDabble.out(), 4, 8), blankingNegDriver1.rippleNegativeOut(), blankingNegDriver1.rippleBlankingOut());
//        blankingNegDriver3.in(Arrays.copyOfRange(doubleDabble.out(), 8, 12), blankingNegDriver2.rippleNegativeOut(), blankingNegDriver2.rippleBlankingOut());
//    }
//
//    public void in(boolean[] in, boolean twosComplimen, boolean blanking) {
//        this.in = in;
//        this.twosComplimen = twosComplimen;
//        this.blanking = blanking;
//        run();
//    }
//
//    public boolean[] out() {
//        return out;
//    }
//
//    public SegmentDisplay getDisplay1() {
//        return blankingNegDriver1.getDisplay();
//    }
//
//    public SegmentDisplay getDisplay2() {
//        return blankingNegDriver2.getDisplay();
//    }
//
//    public SegmentDisplay getDisplay3() {
//        return blankingNegDriver3.getDisplay();
//    }
//}
