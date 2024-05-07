package it.multicoredev.computer.components.gates;

import it.multicoredev.computer.components.Component;
import it.multicoredev.computer.components.types.SingleInputComponent;
import it.multicoredev.computer.components.types.SingleOutputComponent;

public class Not extends Component implements SingleInputComponent, SingleOutputComponent {
    private boolean in;
    private boolean out;

    @Override
    protected void run() {
        out = !in;
    }

    @Override
    public Not in(boolean in) {
        this.in = in;
        run();
        return this;
    }

    @Override
    public boolean out() {
        return out;
    }
}
