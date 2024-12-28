package it.multicoredev.computer.elements;

import it.multicoredev.computer.ui.Chip;

public abstract class ChipComponent extends Component {
    protected final Chip chip;

    public ChipComponent(String name, Pin[] inputs, Pin[] outputs) {
        super(name, inputs, outputs);
        chip = new Chip(this);
    }

    @Override
    protected void update() {
        super.update();
        chip.repaint();
    }

    public final Chip getChip() {
        return chip;
    }

    public final ChipComponent showLabels(boolean showLabels) {
        chip.showLabels(showLabels);
        return this;
    }
}
