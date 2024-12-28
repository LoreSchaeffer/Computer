package it.multicoredev.computer.util;

import it.multicoredev.computer.ui.Pin;

import java.util.List;

public record ConnectionList(Pin fromPin, List<it.multicoredev.computer.elements.Pin> toPins) {
}
