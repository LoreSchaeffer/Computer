package it.multicoredev.computer.components.v3.gates.multi;

import it.multicoredev.computer.components.v3.gates.And;
import it.multicoredev.computer.elements.ChipComponent;
import it.multicoredev.computer.elements.Pin;
import it.multicoredev.computer.util.Direction;
import it.multicoredev.computer.util.State;

public class MultiAnd extends ChipComponent {
    private final And[] ands;

    public MultiAnd(int inputs) {
        super("mAND",
                generateInputPins(inputs),
                new Pin[]{
                        new Pin("Out", Direction.OUTPUT)
                }
        );

        if (inputs < 3) throw new IllegalArgumentException("Inputs must be at least 3");

        ands = new And[inputs - 1];
        for (int i = 0; i < ands.length; i++) {
            ands[i] = new And();
        }

        update();
    }

    @Override
    public void run() {
        for (int i = 0; i < ands.length; i++) {
            ands[i].in(inputs[i].state(), inputs[i + 1].state());
        }

        outputs[0].state(ands[ands.length - 1].out());
    }

    public Pin pinIn(int index) {
        return inputs[index];
    }

    public Pin pinOut() {
        return outputs[0];
    }

    public MultiAnd in(State... states) {
        if (states.length != inputs.length) throw new IllegalArgumentException("States length must be equal to inputs length");

        for (int i = 0; i < states.length; i++) {
            inputs[i].state(states[i]);
        }
        return this;
    }

    public MultiAnd in(int index, State state) {
        inputs[index].state(state);
        return this;
    }

    public State out() {
        return outputs[0].state();
    }

    private static Pin[] generateInputPins(int inputs) {
        Pin[] pins = new Pin[inputs];
        for (int i = 0; i < inputs; i++) {
            pins[i] = new Pin(String.valueOf((char) (65 + i)), Direction.INPUT);
        }

        return pins;
    }
}
