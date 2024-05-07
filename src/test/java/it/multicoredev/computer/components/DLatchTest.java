package it.multicoredev.computer.components;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DLatchTest {

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
}
