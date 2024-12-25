package it.multicoredev.computer.components.display;

import it.multicoredev.computer.components.Component;

public abstract class DisplayComponent extends Component {
    protected final SegmentDisplay display = new SegmentDisplay();

    public SegmentDisplay getDisplay() {
        return display;
    }
}
