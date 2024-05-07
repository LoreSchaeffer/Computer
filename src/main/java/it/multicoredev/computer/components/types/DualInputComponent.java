package it.multicoredev.computer.components.types;

public interface DualInputComponent {

    DualInputComponent in(boolean a, boolean b);

    DualInputComponent inA(boolean a);

    DualInputComponent inB(boolean b);
}
