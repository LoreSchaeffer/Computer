package it.multicoredev.computer.components;

import it.multicoredev.computer.components.registers.Register1Bit;
import it.multicoredev.computer.components.registers.Register4Bit;
import org.junit.Test;

import static org.junit.Assert.*;

public class RegistersTest {

    @Test
    public void register1BitFF() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(false, false);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitFT() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(false, true);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitTF() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(true, false);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitTT() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(true, true);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitFFClock() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(false, false);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitFTClock() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(false, true);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitTFClock() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(true, false);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitTTClock() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(true, true);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertTrue(register1Bit.out());
    }

    @Test
    public void register1BitFFClock2() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(false, false);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertFalse(register1Bit.out());

        register1Bit.clk(false);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitFTClock2() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(false, true);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertFalse(register1Bit.out());

        register1Bit.clk(false);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitTFClock2() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(true, false);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertFalse(register1Bit.out());

        register1Bit.clk(false);
        assertFalse(register1Bit.out());
    }

    @Test
    public void register1BitTTClock2() {
        Register1Bit register1Bit = new Register1Bit();

        register1Bit.in(true, true);
        assertFalse(register1Bit.out());

        register1Bit.clk(true);
        assertTrue(register1Bit.out());

        register1Bit.clk(false);
        assertTrue(register1Bit.out());
    }

    @Test
    public void register4BitNoEnable() {
        Register4Bit register4Bit = new Register4Bit();

        register4Bit.in(new boolean[]{false, true, false, true}, false);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());
    }

    @Test
    public void register4BitEnable() {
        Register4Bit register4Bit = new Register4Bit();

        register4Bit.in(new boolean[]{false, true, false, true}, true);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());
    }

    @Test
    public void register4BitNoEnableClock() {
        Register4Bit register4Bit = new Register4Bit();

        register4Bit.in(new boolean[]{false, true, false, true}, false);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());

        register4Bit.clk(true);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());
    }

    @Test
    public void register4BitEnableClock() {
        Register4Bit register4Bit = new Register4Bit();

        register4Bit.in(new boolean[]{false, true, false, true}, true);
        assertArrayEquals(new boolean[]{false, false, false, false }, register4Bit.out());

        register4Bit.clk(true);
        assertArrayEquals(new boolean[]{false, true, false, true}, register4Bit.out());
    }

    @Test
    public void register4BitNoEnableClock2() {
        Register4Bit register4Bit = new Register4Bit();

        register4Bit.in(new boolean[]{false, true, false, true}, false);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());

        register4Bit.clk(true);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());

        register4Bit.clk(false);
        assertArrayEquals(new boolean[]{false, false, false, false}, register4Bit.out());
    }

    @Test
    public void register4BitEnableClock2() {
        Register4Bit register4Bit = new Register4Bit();

        register4Bit.in(new boolean[]{false, true, false, true}, true);
        assertArrayEquals(new boolean[]{false, false, false, false }, register4Bit.out());

        register4Bit.clk(true);
        assertArrayEquals(new boolean[]{false, true, false, true}, register4Bit.out());

        register4Bit.clk(false);
        assertArrayEquals(new boolean[]{false, true, false, true}, register4Bit.out());
    }
}
