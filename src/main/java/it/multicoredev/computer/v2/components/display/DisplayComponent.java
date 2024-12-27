package it.multicoredev.computer.v2.components.display;

import it.multicoredev.computer.v2.components.Component;
import it.multicoredev.computer.v2.ui.SegmentDisplay;

public abstract class DisplayComponent extends Component {
    protected final SegmentDisplay display;

    public DisplayComponent(int inSize, int outSize) {
        super(inSize, outSize);
        this.display = new SegmentDisplay();
    }

    public DisplayComponent() {
        this(4, 0);
    }

    public DisplayComponent in(boolean... in) {
        super.in(in);
        display.repaint();
        return this;
    }

    public final SegmentDisplay getDisplay() {
        return display;
    }
}
