package it.multicoredev.computer.cpu.alu.adder;

import it.multicoredev.computer.util.BiVal;

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
public class Adder {
    private FullAdder[] adders;

    public Adder(int size) {
        adders = new FullAdder[size];

        for (int i = 0; i < size; i++) {
            adders[i] = new FullAdder();
        }
    }

    public void setA(String a) {
        char[] chars = a.toCharArray();
        int len = chars.length - 1;
        for (int i = 0; i < adders.length; i++) {
            adders[i].setA(Byte.parseByte("" + chars[len - i]));
        }
    }

    public void setB(String b) {
        char[] chars = b.toCharArray();
        int len = chars.length - 1;
        for (int i = 0; i < adders.length; i++) {
            adders[i].setB(Byte.parseByte("" + chars[len - i]));
        }
    }

    public void setC(byte c) {
        adders[0].setC(c);
    }

    public BiVal<String, Byte> getOut() {
        StringBuilder builder = new StringBuilder();

        byte c = -1;

        for (int i = 0; i < adders.length; i++) {
            if (i != 0) adders[i].setC(c);
            byte[] result = adders[i].getOut();
            builder.append(result[0]);
            c = result[1];
        }

        StringBuilder sum = new StringBuilder();
        for(int i = builder.length() - 1; i >= 0; i--) {
            sum.append(builder.charAt(i));
        }

        return new BiVal<>(sum.toString(), c);
    }
}
