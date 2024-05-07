package it.multicoredev.computer.components;

import it.multicoredev.computer.components.gates.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class LogicGatesTest {
    private static final boolean[][] doubleInputInputs = {{false, false}, {false, true}, {true, false}, {true, true}};

    @Test
    public void and() {
        And and = new And();
        boolean[] expected = {false, false, false, true};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            and.in(doubleInputInputs[i][0], doubleInputInputs[i][1]);
            assertEquals(expected[i], and.out());
        }
    }

    @Test
    public void nand() {
        Nand nand = new Nand();
        boolean[] expected = {true, true, true, false};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            nand.in(doubleInputInputs[i][0], doubleInputInputs[i][1]);
            assertEquals(expected[i], nand.out());
        }
    }

    @Test
    public void nor() {
        Nor nor = new Nor();
        boolean[] expected = {true, false, false, false};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            nor.in(doubleInputInputs[i][0], doubleInputInputs[i][1]);
            assertEquals(expected[i], nor.out());
        }
    }

    @Test
    public void not() {
        Not not = new Not();

        not.in(false);
        assertTrue(not.out());

        not.in(true);
        assertFalse(not.out());
    }

    @Test
    public void or() {
        Or or = new Or();
        boolean[] expected = {false, true, true, true};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            or.in(doubleInputInputs[i][0], doubleInputInputs[i][1]);
            assertEquals(expected[i], or.out());
        }
    }

    @Test
    public void xnor() {
        Xnor xnor = new Xnor();
        boolean[] expected = {true, false, false, true};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            xnor.in(doubleInputInputs[i][0], doubleInputInputs[i][1]);
            assertEquals(expected[i], xnor.out());
        }
    }

    @Test
    public void xor() {
        Xor xor = new Xor();
        boolean[] expected = {false, true, true, false};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            xor.in(doubleInputInputs[i][0], doubleInputInputs[i][1]);
            assertEquals(expected[i], xor.out());
        }
    }
}
