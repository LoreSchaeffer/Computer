package it.multicoredev.computer.components;

import it.multicoredev.computer.components.display.SegmentDisplayDriver;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DisplayTest {

    @Test
    public void displayDriver() {
        SegmentDisplayDriver driver = new SegmentDisplayDriver();

        boolean[][] data = new boolean[][]{
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
        };

        boolean[][] expected = new boolean[][]{
                {true, true, true, true, true, true, false}, // 0
                {false, true, true, false, false, false, false}, // 1
                {true, true, false, true, true, false, true}, // 2
                {true, true, true, true, false, false, true}, // 3
                {false, true, true, false, false, true, true}, // 4
                {true, false, true, true, false, true, true}, // 5
                {true, false, true, true, true, true, true}, // 6
                {true, true, true, false, false, false, false}, // 7
                {true, true, true, true, true, true, true}, // 8
                {true, true, true, true, false, true, true} // 9
        };

        for (int i = 0; i < data.length; i++) {
            driver.in(data[i]);
            assertArrayEquals(expected[i], driver.out());
        }
    }
}
