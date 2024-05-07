package it.multicoredev.computer.util;

public class BiVal<K, V> {
    private K val1;
    private V val2;

    public BiVal(K val1, V val2) {
        this.val1 = val1;
        this.val2 = val2;
    }

    public K getVal1() {
        return val1;
    }

    public V getVal2() {
        return val2;
    }

    @Override
    public String toString() {
        return "BiVal{" +
                "val1=" + val1.toString() +
                ", val2=" + val2.toString() +
                '}';
    }
}
