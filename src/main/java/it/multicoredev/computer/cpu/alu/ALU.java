package it.multicoredev.computer.cpu.alu;

import it.multicoredev.computer.cpu.alu.adder.Adder;
import it.multicoredev.computer.cpu.mux.MUX41;
import it.multicoredev.computer.util.BiVal;
import it.multicoredev.computer.util.bitwise.BitwiseAnd;
import it.multicoredev.computer.util.bitwise.BitwiseInverter;
import it.multicoredev.computer.util.bitwise.Replicate;
import it.multicoredev.computer.util.gates.Not;

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
public class ALU {
    private Adder adder = new Adder(8);
    private BitwiseAnd and0 = new BitwiseAnd();
    private BitwiseAnd and1 = new BitwiseAnd();
    private BitwiseInverter inverter = new BitwiseInverter();
    private Replicate replicate = new Replicate(8);
    private MUX41 mux = new MUX41();

    public void setA(String a) {
        adder.setA(a);
        and1.setA(a);
        mux.setC(a);
    }

    public void setB(String b) {
        inverter.setA(b);
        and1.setB(b);
        mux.setD(b);
    }

    public void setS0(byte s) {
        mux.setSel0(s == 1);
    }

    public void setS1(byte s) {
        mux.setSel1(s == 1);
    }

    public void setS2(byte s) {
        adder.setC(s);
    }

    public void setS3(byte s) {
        inverter.setEn(s);
    }

    public void setS4(byte s) {
        replicate.setA(Not.getOut(s));
    }

    public BiVal<String, Byte> getOut() {
        and0.setA(replicate.getOut());
        and0.setB(inverter.getOut());
        adder.setB(and0.getOut());
        BiVal<String, Byte> adderResult = adder.getOut();
        mux.setA(adderResult.getVal1());
        mux.setB(and1.getOut());

        return new BiVal<>(mux.getOut(), adderResult.getVal2());
    }

    /*
     *  S4  S3  S2   S1  S0  Z
     *  0   0   0   0   0   ADD (A+B)
     *  0   0   0   0   1   BITWISE AND (A&B)
     *  0   0   0   1   0   INPUT A
     *  0   0   0   1   1   INPUT B
     *  0   1   1   0   0   SUBTRACT (A-B)
     *  1   0   1   0   0   INCREMENT (A+1)
     *  1   0   0   0   0   INPUT A
     *  0   0   1   0   0   ADD (A+B)+1
     *  0   1   0   0   0   SUBTRACT (A-B)-1
     */
}
