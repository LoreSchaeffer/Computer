package it.multicoredev.computer.components;

import it.multicoredev.computer.components.adders.AddSub8Bit;
import it.multicoredev.computer.components.adders.Adder;
import it.multicoredev.computer.components.adders.Adder4Bit;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class AddersTest {

    @Test
    public void adder() {
        Adder adder = new Adder();

        adder.in(false, false, false);
        assertFalse(adder.sum());
        assertFalse(adder.carry());

        adder.in(false, false, true);
        assertTrue(adder.sum());
        assertFalse(adder.carry());

        adder.in(false, true, false);
        assertTrue(adder.sum());
        assertFalse(adder.carry());

        adder.in(false, true, true);
        assertFalse(adder.sum());
        assertTrue(adder.carry());

        adder.in(true, false, false);
        assertTrue(adder.sum());
        assertFalse(adder.carry());

        adder.in(true, false, true);
        assertFalse(adder.sum());
        assertTrue(adder.carry());

        adder.in(true, true, false);
        assertFalse(adder.sum());
        assertTrue(adder.carry());

        adder.in(true, true, true);
        assertTrue(adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitAllZerosNoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{false, false, false, false}, new boolean[]{false, false, false, false}, false);

        assertArrayEquals(new boolean[]{false, false, false, false}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder4BitAllZerosCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{false, false, false, false}, new boolean[]{false, false, false, false}, true);

        assertArrayEquals(new boolean[]{true, false, false, false}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder4BitAllOnesNoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{true, true, true, true}, new boolean[]{true, true, true, true}, false);

        assertArrayEquals(new boolean[]{false, true, true, true}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitAllOnesCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{true, true, true, true}, new boolean[]{true, true, true, true}, true);

        assertArrayEquals(new boolean[]{true, true, true, true}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitRandom1NoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{true, false, true, false}, new boolean[]{false, true, false, true}, false);

        assertArrayEquals(new boolean[]{true, true, true, true}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder4BitRandom1Carry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{true, false, true, false}, new boolean[]{false, true, false, true}, true);

        assertArrayEquals(new boolean[]{false, false, false, false}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitRandom2NoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{true, false, true, true}, new boolean[]{false, true, true, false}, false);

        assertArrayEquals(new boolean[]{true, true, false, false}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitRandom2Carry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(new boolean[]{true, false, true, true}, new boolean[]{false, true, true, false}, true);

        assertArrayEquals(new boolean[]{false, false, true, false}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void addSubAddNoCarry() {
        AddSub8Bit addSub8Bit = new AddSub8Bit();
        addSub8Bit.in(
                new boolean[]{true, false, true, true, false, true, false, true},
                new boolean[]{false, true, true, false, true, false, true, false},
                false,
                false
        );

        assertArrayEquals(new boolean[]{true, true, false, false, false, false, false, false}, addSub8Bit.result());
        assertTrue(addSub8Bit.carryOut());
    }

    @Test
    public void addSubAddCarry() {
        AddSub8Bit addSub8Bit = new AddSub8Bit();
        addSub8Bit.in(
                new boolean[]{true, false, true, true, false, true, false, true},
                new boolean[]{false, true, true, false, true, false, true, false},
                true,
                false
        );

        assertArrayEquals(new boolean[]{false, false, true, false, false, false, false, false}, addSub8Bit.result());
        assertTrue(addSub8Bit.carryOut());
    }

    @Test
    public void addSubSubtractNoCarry() {
        AddSub8Bit addSub8Bit = new AddSub8Bit();
        addSub8Bit.in(
                new boolean[]{true, false, true, true, false, true, false, true},
                new boolean[]{false, true, true, false, true, false, true, false},
                false,
                true
        );

        assertArrayEquals(new boolean[]{true, true, true, false, true, false, true, false}, addSub8Bit.result());
        assertTrue(addSub8Bit.carryOut());
    }

    @Test
    public void addSubSubtractCarry() {
        AddSub8Bit addSub8Bit = new AddSub8Bit();
        addSub8Bit.in(
                new boolean[]{true, false, true, true, false, true, false, true},
                new boolean[]{false, true, true, false, true, false, true, false},
                true,
                true
        );

        assertArrayEquals(new boolean[]{true, true, true, false, true, false, true, false}, addSub8Bit.result());
        assertTrue(addSub8Bit.carryOut());
    }
}
