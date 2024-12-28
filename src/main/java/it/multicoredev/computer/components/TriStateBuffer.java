package it.multicoredev.computer.components;

public class TriStateBuffer extends ChipComponent {

    public TriStateBuffer() {
        super(2, 1, "3-State Buffer");
        run();
    }

    @Override
    protected void update() {
        out[0] = in[0] ? in[1] : null; //TODO Floating value is needed -> Switch from boolean to Boolean/Enum
    }
}
