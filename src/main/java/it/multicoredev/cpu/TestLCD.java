package it.multicoredev.cpu;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright © 2019 by Lorenzo Magni
 * This file is part of Computer.
 * Computer is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class TestLCD {
    private static final String[] chars = new String[] {"00100000", "01000001", "01000010", "01000011", "01000100", "01000101",
            "01000110", "01000111", "01001000", "01001001", "01001010", "01001011", "01001100", "01001101", "01001110",
            "01001111", "01010000", "01010001", "01010010", "01010011", "01010100", "01010101", "01010110", "01010111",
            "01011000", "01011001", "01011010"
    };
    private static final int[] hello = new int[] {8, 5, 12, 12, 15, 0, 23, 15, 18, 12, 4};

    public static void main(String[] args) {
        LCD lcd = new LCD(16, 4);
        ScheduledExecutorService clock = Executors.newSingleThreadScheduledExecutor();
        clock.scheduleAtFixedRate(() -> {
            for(int i : hello) {
                lcd.setRegSel(true);
                lcd.digitalWrite(chars[i]);
                lcd.enable(true);
                lcd.enable(false);
                lcd.setRegSel(false);
                lcd.digitalWrite("00000110");
                lcd.enable(true);
                lcd.enable(false);
                lcd.print();
            }
            return;
            /*lcd.setRegSel(true);
            //lcd.digitalWrite("01000010");
            //lcd.digitalWrite(randomChar());
            lcd.enable(true);
            lcd.enable(false);
            lcd.setRegSel(false);
            lcd.digitalWrite("00000110");
            lcd.enable(true);
            lcd.enable(false);
            lcd.print();*/
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }

    private static boolean randomBit() {
        return new Random().nextInt(2) == 1;
    }

    public static String randomChar() {
        return chars[new Random().nextInt(chars.length)];
    }
}
