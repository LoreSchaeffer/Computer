package it.multicoredev.computer;

import it.multicoredev.computer.util.gates.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class LogicGatesTest {
    private static final int[][] doubleInputInputs = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};

    @Test
    public void testAnd() {
        And and = new And();
        int[] expectedOutputs = {0, 0, 0, 1};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            int[] input = doubleInputInputs[i];
            assertEquals(expectedOutputs[i], and.out((byte) input[0], (byte) input[1]));
        }
    }

    @Test
    public void testNand() {
        Nand nand = new Nand();
        int[] expectedOutputs = {1, 1, 1, 0};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            int[] input = doubleInputInputs[i];
            assertEquals(expectedOutputs[i], nand.out((byte) input[0], (byte) input[1]));
        }
    }

    @Test
    public void testNor() {
        Nor nor = new Nor();
        int[] expectedOutputs = {1, 0, 0, 0};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            int[] input = doubleInputInputs[i];
            assertEquals(expectedOutputs[i], nor.out((byte) input[0], (byte) input[1]));
        }
    }

    @Test
    public void testNot() {
        Not not = new Not();
        assertEquals(1, not.out((byte) 0));
        assertEquals(0, not.out((byte) 1));
    }

    @Test
    public void testOr() {
        Or or = new Or();
        int[] expectedOutputs = {0, 1, 1, 1};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            int[] input = doubleInputInputs[i];
            assertEquals(expectedOutputs[i], or.out((byte) input[0], (byte) input[1]));
        }
    }

    @Test
    public void testXnor() {
        Xor xor = new Xor();
        int[] expectedOutputs = {0, 1, 1, 0};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            int[] input = doubleInputInputs[i];
            assertEquals(expectedOutputs[i], xor.out((byte) input[0], (byte) input[1]));
        }
    }

    @Test
    public void testXor() {
        Xnor xnor = new Xnor();
        int[] expectedOutputs = {1, 0, 0, 1};

        for (int i = 0; i < doubleInputInputs.length; i++) {
            int[] input = doubleInputInputs[i];
            assertEquals(expectedOutputs[i], xnor.out((byte) input[0], (byte) input[1]));
        }
    }
}
