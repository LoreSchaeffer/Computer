package it.multicoredev.computer.cpu.registers;

import it.multicoredev.computer.util.components.DFlipFlop;
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
public class Register4 implements ClockListener {
    private DFlipFlop ff0 = new DFlipFlop();
    private DFlipFlop ff1 = new DFlipFlop();
    private DFlipFlop ff2 = new DFlipFlop();
    private DFlipFlop ff3 = new DFlipFlop();

    public void setD0(byte d) {
        ff0.setD(d);
    }

    public void setD1(byte d) {
        ff1.setD(d);
    }

    public void setD2(byte d) {
        ff2.setD(d);
    }

    public void setD3(byte d) {
        ff3.setD(d);
    }

    public void setCe(byte ce) {
        ff0.setCe(ce);
        ff1.setCe(ce);
        ff2.setCe(ce);
        ff3.setCe(ce);
    }

    public void clr() {
        ff0.clr();
        ff1.clr();
        ff2.clr();
        ff3.clr();
    }

    public byte getOut0() {
        return ff0.getOut();
    }

    public byte getOut1() {
        return ff1.getOut();
    }

    public byte getOut2() {
        return ff2.getOut();
    }

    public byte getOut3() {
        return ff3.getOut();
    }

    @Override
    public void clock(boolean clock) {
        ff0.clock(clock);
        ff1.clock(clock);
        ff2.clock(clock);
        ff3.clock(clock);
    }
}
