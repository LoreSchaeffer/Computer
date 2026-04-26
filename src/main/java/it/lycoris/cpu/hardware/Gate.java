package it.lycoris.cpu.hardware;

public sealed interface Gate extends LogicComponent permits AndGate, NandGate, NotGate, OrGate, NorGate, XorGate, XnorGate, DLatch, VccGate, GndGate {
}
