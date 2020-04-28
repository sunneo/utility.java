package com.example.sharp;

public class KeyValuePair<K extends Object, V extends Object> {
    public K Key;
    public V Value;

    public KeyValuePair() {
    }

    public KeyValuePair(K k, V v) {
        this.Key = k;
        this.Value = v;
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", Key, Value);
    }
}
