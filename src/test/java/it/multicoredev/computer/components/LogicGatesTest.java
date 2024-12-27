package it.multicoredev.computer.components;

import it.multicoredev.computer.components.gates.*;
import it.multicoredev.computer.components.gates.compound.MultiAnd;
import it.multicoredev.computer.components.gates.compound.MultiOr;
import org.junit.Test;

import static org.junit.Assert.*;

public class LogicGatesTest {
    private static final boolean[][] doubleInputs = {{false, false}, {false, true}, {true, false}, {true, true}};

    @Test
    public void and() {
        And and = new And();
        boolean[] expected = {false, false, false, true};

        for (int i = 0; i < doubleInputs.length; i++) {
            and.in(doubleInputs[i][0], doubleInputs[i][1]);
            assertEquals(expected[i], and.out()[0]);
        }
    }

    @Test
    public void nand() {
        Nand nand = new Nand();
        boolean[] expected = {true, true, true, false};

        for (int i = 0; i < doubleInputs.length; i++) {
            nand.in(doubleInputs[i][0], doubleInputs[i][1]);
            assertEquals(expected[i], nand.out()[0]);
        }
    }

    @Test
    public void nor() {
        Nor nor = new Nor();
        boolean[] expected = {true, false, false, false};

        for (int i = 0; i < doubleInputs.length; i++) {
            nor.in(doubleInputs[i][0], doubleInputs[i][1]);
            assertEquals(expected[i], nor.out()[0]);
        }
    }

    @Test
    public void not() {
        Not not = new Not();

        not.in(false);
        assertTrue(not.out()[0]);

        not.in(true);
        assertFalse(not.out()[0]);
    }

    @Test
    public void or() {
        Or or = new Or();
        boolean[] expected = {false, true, true, true};

        for (int i = 0; i < doubleInputs.length; i++) {
            or.in(doubleInputs[i][0], doubleInputs[i][1]);
            assertEquals(expected[i], or.out()[0]);
        }
    }

    @Test
    public void xnor() {
        Xnor xnor = new Xnor();
        boolean[] expected = {true, false, false, true};

        for (int i = 0; i < doubleInputs.length; i++) {
            xnor.in(doubleInputs[i][0], doubleInputs[i][1]);
            assertEquals(expected[i], xnor.out()[0]);
        }
    }

    @Test
    public void xor() {
        Xor xor = new Xor();
        boolean[] expected = {false, true, true, false};

        for (int i = 0; i < doubleInputs.length; i++) {
            xor.in(doubleInputs[i][0], doubleInputs[i][1]);
            assertEquals(expected[i], xor.out()[0]);
        }
    }

    @Test
    public void multiAnd1() {
        MultiAnd and = new MultiAnd(3);
        boolean[][] inputs = new boolean[][]{
                {false, false, false},
                {false, false, true},
                {false, true, false},
                {false, true, true},
                {true, false, false},
                {true, false, true},
                {true, true, false},
                {true, true, true}
        };
        boolean[] expected = {false, false, false, false, false, false, false, true};

        for (int i = 0; i < inputs.length; i++) {
            and.in(inputs[i]);
            assertEquals(expected[i], and.out()[0]);
        }
    }

    @Test
    public void MultiAnd2() {
        MultiAnd and = new MultiAnd(4);
        boolean[][] inputs = new boolean[][]{
                {false, false, false, false},
                {false, false, false, true},
                {false, false, true, false},
                {false, false, true, true},
                {false, true, false, false},
                {false, true, false, true},
                {false, true, true, false},
                {false, true, true, true},
                {true, false, false, false},
                {true, false, false, true},
                {true, false, true, false},
                {true, false, true, true},
                {true, true, false, false},
                {true, true, false, true},
                {true, true, true, false},
                {true, true, true, true}
        };
        boolean[] expected = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true};

        for (int i = 0; i < inputs.length; i++) {
            and.in(inputs[i]);
            assertEquals(expected[i], and.out()[0]);
        }
    }

    @Test
    public void multiOr1() {
        MultiOr or = new MultiOr(3);
        boolean[][] inputs = new boolean[][]{
                {false, false, false},
                {false, false, true},
                {false, true, false},
                {false, true, true},
                {true, false, false},
                {true, false, true},
                {true, true, false},
                {true, true, true}
        };
        boolean[] expected = {false, true, true, true, true, true, true, true};

        for (int i = 0; i < inputs.length; i++) {
            or.in(inputs[i]);
            assertEquals(expected[i], or.out()[0]);
        }
    }

    @Test
    public void multiOr2() {
        MultiOr or = new MultiOr(4);
        boolean[][] inputs = new boolean[][]{
                {false, false, false, false},
                {false, false, false, true},
                {false, false, true, false},
                {false, false, true, true},
                {false, true, false, false},
                {false, true, false, true},
                {false, true, true, false},
                {false, true, true, true},
                {true, false, false, false},
                {true, false, false, true},
                {true, false, true, false},
                {true, false, true, true},
                {true, true, false, false},
                {true, true, false, true},
                {true, true, true, false},
                {true, true, true, true}
        };
        boolean[] expected = {false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};

        for (int i = 0; i < inputs.length; i++) {
            or.in(inputs[i]);
            assertEquals(expected[i], or.out()[0]);
        }
    }
}
