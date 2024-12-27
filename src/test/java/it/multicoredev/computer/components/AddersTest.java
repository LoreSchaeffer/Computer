package it.multicoredev.computer.components;

import it.multicoredev.computer.components.adders.Adder;
import it.multicoredev.computer.components.adders.Adder4Bit;
import it.multicoredev.computer.components.adders.AdderSub8Bit;
import org.junit.Test;

import static org.junit.Assert.*;

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
        adder.in(
                false, false, false, false,
                false, false, false, false,
                false
        );

        assertArrayEquals(new boolean[]{false, false, false, false}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder4BitAllZerosCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                false, false, false, false,
                false, false, false, false,
                true
        );

        assertArrayEquals(new boolean[]{true, false, false, false}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder4BitAllOnesNoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, true, true, true,
                true, true, true, true,
                false
        );

        assertArrayEquals(new boolean[]{false, true, true, true}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitAllOnesCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, true, true, true,
                true, true, true, true,
                true
        );

        assertArrayEquals(new boolean[]{true, true, true, true}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitRandom1NoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, false, true, false,
                false, true, false, true,
                false
        );

        assertArrayEquals(new boolean[]{true, true, true, true}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder4BitRandom1Carry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, false, true, false,
                false, true, false, true,
                true
        );

        assertArrayEquals(new boolean[]{false, false, false, false}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitRandom2NoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, false, true, true,
                false, true, true, false,
                false
        );

        assertArrayEquals(new boolean[]{true, true, false, false}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder4BitRandom2Carry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, false, true, true,
                false, true, true, false,
                true
        );

        assertArrayEquals(new boolean[]{false, false, true, false}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder8BitAllZerosNoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                false, false, false, false,
                false, false, false, false,
                false
        );

        assertArrayEquals(new boolean[]{false, false, false, false}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder8BitAllZerosCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                false, false, false, false,
                false, false, false, false,
                true
        );

        assertArrayEquals(new boolean[]{true, false, false, false}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adder8BitAllOnesNoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, true, true, true,
                true, true, true, true,
                false
        );

        assertArrayEquals(new boolean[]{false, true, true, true}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder8BitAllOnesCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, true, true, true,
                true, true, true, true,
                true
        );

        assertArrayEquals(new boolean[]{true, true, true, true}, adder.sum());
        assertTrue(adder.carry());
    }

    @Test
    public void adder8BitRandom1NoCarry() {
        Adder4Bit adder = new Adder4Bit();
        adder.in(
                true, false, true, false,
                false, true, false, true,
                false
        );

        assertArrayEquals(new boolean[]{true, true, true, true}, adder.sum());
        assertFalse(adder.carry());
    }

    @Test
    public void adderSub8BitAllZerosNoCarryNoSub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(false, false, false, false, false, false, false, false);
        adder.inB(false, false, false, false, false, false, false, false);
        adder.inCarry(false);
        adder.inSub(false);

        assertArrayEquals(new boolean[]{false, false, false, false, false, false, false, false}, adder.result());
    }

    @Test
    public void adderSub8BitAllZerosCarryNoSub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(false, false, false, false, false, false, false, false);
        adder.inB(false, false, false, false, false, false, false, false);
        adder.inCarry(true);
        adder.inSub(false);

        assertArrayEquals(new boolean[]{true, false, false, false, false, false, false, false}, adder.result());
    }

    @Test
    public void adderSub8BitAllZerosNoCarrySub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(false, false, false, false, false, false, false, false);
        adder.inB(false, false, false, false, false, false, false, false);
        adder.inCarry(false);
        adder.inSub(true);

        assertArrayEquals(new boolean[]{false, false, false, false, false, false, false, false}, adder.result());
    }

    @Test
    public void adderSub8BitAllZerosCarrySub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(false, false, false, false, false, false, false, false);
        adder.inB(false, false, false, false, false, false, false, false);
        adder.inCarry(true);
        adder.inSub(true);

        assertArrayEquals(new boolean[]{false, false, false, false, false, false, false, false}, adder.result());
    }

    @Test
    public void adderSub8BitAllOnesNoCarryNoSub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(true, true, true, true, true, true, true, true);
        adder.inB(true, true, true, true, true, true, true, true);
        adder.inCarry(false);
        adder.inSub(false);

        assertArrayEquals(new boolean[]{false, true, true, true, true, true, true, true}, adder.result());
    }

    @Test
    public void adderSub8BitAllOnesCarryNoSub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(true, true, true, true, true, true, true, true);
        adder.inB(true, true, true, true, true, true, true, true);
        adder.inCarry(true);
        adder.inSub(false);

        assertArrayEquals(new boolean[]{true, true, true, true, true, true, true, true}, adder.result());
    }

    @Test
    public void adderSub8BitAllOnesNoCarrySub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(true, true, true, true, true, true, true, true);
        adder.inB(true, true, true, true, true, true, true, true);
        adder.inCarry(false);
        adder.inSub(true);

        assertArrayEquals(new boolean[]{false, false, false, false, false, false, false, false}, adder.result());
    }

    @Test
    public void adderSub8BitAllOnesCarrySub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(true, true, true, true, true, true, true, true);
        adder.inB(true, true, true, true, true, true, true, true);
        adder.inCarry(true);
        adder.inSub(true);

        assertArrayEquals(new boolean[]{true, true, true, true, true, true, true, true}, adder.result());
    }

    @Test
    public void adderSub8BitRandom1NoCarryNoSub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(true, false, true, false, false, true, false, true);
        adder.inB(false, true, false, true, true, false, true, false);
        adder.inCarry(false);
        adder.inSub(false);

        assertArrayEquals(new boolean[]{true, true, true, false, false, false, false, false}, adder.result());
    }

    @Test
    public void adderSub8BitRandom1CarryNoSub() {
        AdderSub8Bit adder = new AdderSub8Bit();
        adder.inA(true, false, true, false, false, true, false, true);
        adder.inB(false, true, false, true, true, false, true, false);
        adder.inCarry(true);
        adder.inSub(false);

        assertArrayEquals(new boolean[]{false, false, false, false, false, false, false, false}, adder.result());
    }
}
