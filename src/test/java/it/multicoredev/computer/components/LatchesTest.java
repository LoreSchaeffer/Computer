package it.multicoredev.computer.components;

import it.multicoredev.computer.v2.components.latches.*;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LatchesTest {

    @Test
    public void dLatchFF() {
        DLatch dLatch = new DLatch();

        dLatch.in(false, false);
        assertFalse(dLatch.out());
    }

    @Test
    public void dLatchFT() {
        DLatch dLatch = new DLatch();

        dLatch.in(false, true);
        assertFalse(dLatch.out());
    }

    @Test
    public void dLatchTF() {
        DLatch dLatch = new DLatch();

        dLatch.in(true, false);
        assertFalse(dLatch.out());
    }

    @Test
    public void dLatchTT() {
        DLatch dLatch = new DLatch();

        dLatch.in(true, true);
        assertTrue(dLatch.out());
    }

    @Test
    public void dFlipFlopFF() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(false, false);
        assertFalse(dFlipFlop.out());
    }

    @Test
    public void dFlipFlopFT() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(false, true);
        assertFalse(dFlipFlop.out());
    }

    @Test
    public void dFlipFlopTF() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(true, false);
        assertFalse(dFlipFlop.out());
    }

    @Test
    public void dFlipFlopTT() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(true, true);
        assertFalse(dFlipFlop.out());
    }

    @Test
    public void dFlipFlopFTPulse() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(false, true);
        assertFalse(dFlipFlop.out());

        dFlipFlop.inEnable(false);
        assertFalse(dFlipFlop.out());

        dFlipFlop.inEnable(true);
        assertFalse(dFlipFlop.out());
    }

    @Test
    public void dFlipFlopTFPulse() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(true, false);
        assertFalse(dFlipFlop.out());

        dFlipFlop.inEnable(true);
        assertTrue(dFlipFlop.out());

        dFlipFlop.inEnable(false);
        assertTrue(dFlipFlop.out());
    }

    @Test
    public void dFlipFlopTTPulse() {
        DFlipFlop dFlipFlop = new DFlipFlop();

        dFlipFlop.in(true, true);
        assertFalse(dFlipFlop.out());

        dFlipFlop.inEnable(false);
        assertFalse(dFlipFlop.out());

        dFlipFlop.inEnable(true);
        assertTrue(dFlipFlop.out());
    }
}
