package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.display.dabble.DoubleDabble;

public class DoubleDabbleDriver extends Component {
    private final DoubleDabble doubleDabble = new DoubleDabble();
    private final BlankingNegativeDriver blankingNegDriver1 = new BlankingNegativeDriver();
    private final BlankingNegativeDriver blankingNegDriver2 = new BlankingNegativeDriver();
    private final BlankingNegativeDriver blankingNegDriver3 = new BlankingNegativeDriver();

    private boolean blank = false;
    private boolean[] data;

    @Override
    protected void run() {
        doubleDabble.in(data);

        blankingNegDriver1.in(new boolean[]{
                doubleDabble.out()[0],
                doubleDabble.out()[1],
                doubleDabble.out()[2],
                doubleDabble.out()[3],
        }, false, blank);
        blankingNegDriver2.in(new boolean[]{
                doubleDabble.out()[4],
                doubleDabble.out()[5],
                doubleDabble.out()[6],
                doubleDabble.out()[7],
        }, false, blankingNegDriver1.rippleBlankingOut());
        blankingNegDriver3.in(new boolean[]{
                doubleDabble.out()[8],
                doubleDabble.out()[9],
                doubleDabble.out()[10],
                doubleDabble.out()[11],
        }, false, blankingNegDriver2.rippleBlankingOut());
    }

    public DoubleDabbleDriver in(boolean[] data, boolean blank) {
        this.data = data;
        this.blank = blank;
        run();
        return this;
    }

    public DoubleDabbleDriver in(boolean[] data) {
        return in(data, false);
    }

    public SegmentDisplay getDisplay1() {
        return blankingNegDriver1.getDisplay();
    }

    public SegmentDisplay getDisplay2() {
        return blankingNegDriver2.getDisplay();
    }

    public SegmentDisplay getDisplay3() {
        return blankingNegDriver3.getDisplay();
    }
}
