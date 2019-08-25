package it.multicoredev.computer.cpu.decoder;

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
public class InstructionDecoder {
    private byte add;
    private byte load;
    private byte output;
    private byte input;
    private byte jumpz;
    private byte jumpnz;
    private byte jump;
    private byte jumpc;
    private byte jumpnc;
    private byte sub;
    private byte bitand;

    public void decode(String s) {
        switch (s) {
            case "0000":
                add = 0;
                load = 1;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "0100":
                add = 1;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "0001":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 1;
                break;
            case "0110":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 1;
                bitand = 0;
                break;
            case "1010":
                add = 0;
                load = 0;
                output = 0;
                input = 1;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "1110":
                add = 0;
                load = 0;
                output = 1;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "1000":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 1;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "100100":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 1;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "100110":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 1;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "100101":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 1;
                jump = 0;
                jumpc = 0;
                jumpnc = 0;
                sub = 0;
                bitand = 0;
                break;
            case "100111":
                add = 0;
                load = 0;
                output = 0;
                input = 0;
                jumpz = 0;
                jumpnz = 0;
                jump = 0;
                jumpc = 0;
                jumpnc = 1;
                sub = 0;
                bitand = 0;
                break;
            default:
                throw new IllegalArgumentException("Not recognized");
        }
    }

    public byte getAdd() {
        return add;
    }

    public byte getLoad() {
        return load;
    }

    public byte getOutput() {
        return output;
    }

    public byte getInput() {
        return input;
    }

    public byte getJumpz() {
        return jumpz;
    }

    public byte getJumpnz() {
        return jumpnz;
    }

    public byte getJump() {
        return jump;
    }

    public byte getJumpc() {
        return jumpc;
    }

    public byte getJumpnc() {
        return jumpnc;
    }

    public byte getSub() {
        return sub;
    }

    public byte getBitand() {
        return bitand;
    }
}
