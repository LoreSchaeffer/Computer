package it.multicoredev.computer.v2.components;

import it.multicoredev.computer.constants.Colors;
import it.multicoredev.computer.v2.ui.Chip;

public abstract class ChipComponent extends Component {
    protected final Chip chip;

    public ChipComponent(int inSize, int outSize, String name, String[] inLabels, String[] outLabels) {
        super(inSize, outSize);

        this.chip = new Chip(Colors.getRandom(getClass()), name, this);
        if (inLabels != null) this.chip.setInLabels(inLabels);
        if (outLabels != null) this.chip.setOutLabels(outLabels);
    }

    public ChipComponent(int inSize, int outSize, String name) {
        this(inSize, outSize, name, null, null);
    }

    public final ChipComponent in(boolean... in) {
        super.in(in);
        chip.repaint();
        return this;
    }

    public final Chip getChip() {
        return chip;
    }
}
