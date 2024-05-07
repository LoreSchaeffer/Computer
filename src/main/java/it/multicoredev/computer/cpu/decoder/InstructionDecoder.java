package it.multicoredev.computer.cpu.decoder;

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
