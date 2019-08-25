package it.multicoredev.computer.cpu.decoder;

import it.multicoredev.computer.util.gates.And;
import it.multicoredev.computer.util.gates.Or;
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
public class Decoder implements ClockListener {
    private SequenceGenerator sg = new SequenceGenerator();
    private InstructionDecoder id = new InstructionDecoder();

    private String instruction;

    private byte add;
    private byte load;
    private byte output;
    private byte input;
    private byte jumpz;
    private byte jump;
    private byte jumpnz;
    private byte jumpc;
    private byte jumpnc;
    private byte sub;
    private byte bitand;

    @Override
    public void clock(boolean clock) {
        if(clock) {
            sg.clock(clock);
            id.decode(instruction);

            byte state = Or.getOut(sg.decode(), sg.execute());

            add = And.getOut(state, id.getAdd());
            load = And.getOut(state, id.getLoad());
            output = And.getOut(state, id.getOutput());
            input = And.getOut(state, id.getInput());
            jumpz = And.getOut(state, id.getJumpz());
            jump = And.getOut(state, id.getJump());
            jumpnz = And.getOut(state, id.getJumpnz());
            jumpc = And.getOut(state, id.getJumpc());
            jumpnc = And.getOut(state, id.getJumpnc());
            sub = And.getOut(state, id.getSub());
            bitand = And.getOut(state, id.getBitand());
        }
    }
}
