package it.multicoredev.computer.components;

public abstract class ChipComponent extends Component {

    public ChipComponent(int inSize, int outSize, String name, String[] inLabels, String[] outLabels) {
        super(inSize, outSize);
    }

    public ChipComponent(int inSize, int outSize, String name) {
        this(inSize, outSize, name, null, null);
    }

    public void in(boolean... in) {
        super.in(in);
    }
}
