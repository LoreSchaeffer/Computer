package it.multicoredev.computer.cpu.mux;

public class MUX {
    private MUXBlock[] blocks;

    public MUX(int size) {
        blocks = new MUXBlock[size];

        for (int i = 0; i < size; i++) {
            blocks[i] = new MUXBlock();
        }
    }

    public void setA(String a) {
        char[] chars = a.toCharArray();
        for (int i = 0; i < blocks.length; i++) {
            blocks[i].setA(Byte.parseByte("" + chars[i]));
        }
    }

    public void setB(String b) {
        char[] chars = b.toCharArray();
        for (int i = 0; i < blocks.length; i++) {
            blocks[i].setB(Byte.parseByte("" + chars[i]));
        }
    }

    public void setSel(boolean sel) {
        for (MUXBlock block : blocks) {
            block.setSel(sel);
        }
    }

    public String getOut() {
        StringBuilder builder = new StringBuilder();
        for (MUXBlock block : blocks) {
            builder.append(block.getOut() ? "1" : "0");
        }

        return builder.toString();
    }
}
