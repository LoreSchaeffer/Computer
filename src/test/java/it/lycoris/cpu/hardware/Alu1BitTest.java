package it.lycoris.cpu.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Alu1BitTest extends HardwareTestBase {

    @BeforeEach
    void setup() {
        load("ALU1Bit");
    }

    @Test
    void testAdditionLogic() {
        clearOperations();
        setPin("OpADD", true);

        // Case 1: 0 + 0 + 0 = 0 (Carry 0)
        setPin("A", false);
        setPin("B", false);
        setPin("CIn", false);
        update();
        assertFalse(getPin("Res"), "0+0+0 should yield Res=0");
        assertFalse(getPin("COut"), "0+0+0 should yield COut=0");

        // Case 2: 1 + 0 + 0 = 1 (Carry 0)
        setPin("A", true);
        setPin("B", false);
        setPin("CIn", false);
        update();
        assertTrue(getPin("Res"), "1+0+0 should yield Res=1");
        assertFalse(getPin("COut"), "1+0+0 should yield COut=0");

        // Case 3: 1 + 1 + 0 = 0 (Carry 1)
        setPin("A", true);
        setPin("B", true);
        setPin("CIn", false);
        update();
        assertFalse(getPin("Res"), "1+1+0 should yield Res=0");
        assertTrue(getPin("COut"), "1+1+0 should yield COut=1");

        // Case 4: 1 + 1 + 1 = 1 (Carry 1)
        setPin("A", true);
        setPin("B", true);
        setPin("CIn", true);
        update();
        assertTrue(getPin("Res"), "1+1+1 should yield Res=1");
        assertTrue(getPin("COut"), "1+1+1 should yield COut=1");
    }

    @Test
    void testLogicalAnd() {
        clearOperations();
        setPin("OpAND", true);

        // Logical operations shouldn't generate Carry
        setPin("CIn", false);

        // 1 AND 1 = 1
        setPin("A", true);
        setPin("B", true);
        update();
        assertTrue(getPin("Res"), "1 AND 1 should be 1");
        assertFalse(getPin("COut"), "AND operation should not generate COut");

        // 1 AND 0 = 0
        setPin("A", true);
        setPin("B", false);
        update();
        assertFalse(getPin("Res"), "1 AND 0 should be 0");
    }

    @Test
    void testLogicalOr() {
        clearOperations();
        setPin("OpOR", true);
        setPin("CIn", false);

        // 1 OR 0 = 1
        setPin("A", true);
        setPin("B", false);
        update();
        assertTrue(getPin("Res"), "1 OR 0 should be 1");

        // 0 OR 0 = 0
        setPin("A", false);
        setPin("B", false);
        update();
        assertFalse(getPin("Res"), "0 OR 0 should be 0");
    }

    @Test
    void testLogicalXor() {
        clearOperations();
        setPin("OpXOR", true);
        setPin("CIn", false);

        // 1 XOR 0 = 1
        setPin("A", true);
        setPin("B", false);
        update();
        assertTrue(getPin("Res"), "1 XOR 0 should be 1");

        // 1 XOR 1 = 0
        setPin("A", true);
        setPin("B", true);
        update();
        assertFalse(getPin("Res"), "1 XOR 1 should be 0");
    }

    @Test
    void testNoOperationSelectedYieldsZero() {
        clearOperations(); // All Op signals are false

        // Even if inputs are high, if no operation is selected, output should be 0
        setPin("A", true);
        setPin("B", true);
        setPin("CIn", true);
        update();

        assertFalse(getPin("Res"), "Res should be 0 if no operation is selected");
    }
}
