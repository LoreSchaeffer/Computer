package it.multicoredev.computer.lcd;

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
public class LCD {
    private boolean on;
    private byte[][] screen;
    private int col = 0;
    private int row = 0;
    private boolean regSel;
    private boolean rw;
    private boolean enable;
    private boolean b0;
    private boolean b1;
    private boolean b2;
    private boolean b3;
    private boolean b4;
    private boolean b5;
    private boolean b6;
    private boolean b7;

    public LCD(int col, int row) {
        screen = new byte[row][col];
    }

    public void setRegSel(boolean b) {
        regSel = b;
    }

    public void setRw(boolean b) {
        rw = b;
    }

    public void enable(boolean b) {
        enable = b;
        if (enable) {
            if (regSel) {
                screen[row][col] = createByte();
            } else {
                switch (getString()) {
                    case "00000001":
                        clear();
                        break;
                    case "00000010":
                        home();
                        break;
                    case "00000100":
                        cursorLeft();
                        break;
                    case "00000110":
                        cursorRight();
                        break;
                    case "00000101":
                        moveLeft();
                        break;
                    case "00000111":
                        moveRight();
                        break;
                    case "00000000":
                        displayOff();
                        break;
                    case "00001000":
                        displayOn();
                        break;
                }
            }
        }
    }

    public void digitalWrite(boolean b0, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7) {
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.b4 = b4;
        this.b5 = b5;
        this.b6 = b6;
        this.b7 = b7;
    }

    public void digitalWrite(String s) {
        char[] chars = s.toCharArray();
        b0 = chars[0] == '1';
        b1 = chars[1] == '1';
        b2 = chars[2] == '1';
        b3 = chars[3] == '1';
        b4 = chars[4] == '1';
        b5 = chars[5] == '1';
        b6 = chars[6] == '1';
        b7 = chars[7] == '1';
    }

    private String getString() {
        return String.valueOf(b0 ? 1 : 0) +
                (b1 ? 1 : 0) +
                (b2 ? 1 : 0) +
                (b3 ? 1 : 0) +
                (b4 ? 1 : 0) +
                (b5 ? 1 : 0) +
                (b6 ? 1 : 0) +
                (b7 ? 1 : 0);
    }

    private byte createByte() {
        return (byte) Integer.parseInt(getString(), 2);
    }

    public byte getByte() {
        return screen[row][col];
    }

    public void print() {
        StringBuilder builder = new StringBuilder();
        for (byte[] row : screen) {
            for (byte b : row) {
                builder.append(b != 0 ? (char) b : "");
            }
            builder.append("\n");
        }

        String s = builder.toString();
        s = s.replaceAll("\\n\\n+", "");

        System.out.println(s + "\n");
    }

    private void clear() {
        row = 0;
        col = 0;
        screen = new byte[screen.length][screen[0].length];
    }

    private void home() {
        row = 0;
        col = 0;
    }

    private void moveLeft() {
        //TODO
    }

    private void moveRight() {
        //TODO
    }

    private void cursorLeft() {
        col--;
        if (col < 0) {
            if(row > 0) {
                row--;
                if(row < 0) {
                    row++;
                    col = 0;
                } else {
                    col = screen[0].length;
                }
            } else {
                col = 0;
            }
        }
    }

    private void cursorRight() {
        col++;
        if (col == screen[0].length) {
            row++;
            if (row == screen.length) {
                row--;
                col--;
            } else {
                col = 0;
            }
        }
    }

    private void displayOff() {
        on = false;
    }

    private void displayOn() {
        on = true;
    }
}
