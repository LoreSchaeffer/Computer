package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.listeners.ClockListener;

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
public class Register8 implements ClockListener {
    private Register4 reg0 = new Register4();
    private Register4 reg1 = new Register4();

    public void setD(String d) {
        char[] chars = d.toCharArray();
        reg0.setD0(Byte.parseByte("" + chars[0]));
        reg0.setD1(Byte.parseByte("" + chars[1]));
        reg0.setD2(Byte.parseByte("" + chars[2]));
        reg0.setD3(Byte.parseByte("" + chars[3]));
        reg1.setD0(Byte.parseByte("" + chars[4]));
        reg1.setD1(Byte.parseByte("" + chars[5]));
        reg1.setD2(Byte.parseByte("" + chars[6]));
        reg1.setD3(Byte.parseByte("" + chars[7]));
    }

    public void setCe(byte ce) {
        reg0.setCe(ce);
        reg1.setCe(ce);
    }

    public void clr() {
        reg0.clr();
        reg1.clr();
    }

    public String getOut() {
        StringBuilder builder = new StringBuilder();
        builder.append(reg0.getOut0());
        builder.append(reg0.getOut1());
        builder.append(reg0.getOut2());
        builder.append(reg0.getOut3());
        builder.append(reg1.getOut0());
        builder.append(reg1.getOut1());
        builder.append(reg1.getOut2());
        builder.append(reg1.getOut3());

        return builder.toString();
    }

    @Override
    public void clock(boolean clock) {
        reg0.clock(clock);
        reg1.clock(clock);
    }
}
