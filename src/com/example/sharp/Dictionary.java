package com.example.sharp;

import java.util.Hashtable;
import java.util.Vector;

public class Dictionary<K, V> {
    public Dictionary() {
    }

    Hashtable<K, V> instance = new Hashtable<>();

    public V get(K key) {
        return instance.get(key);
    }

    public void set(K key, V value) {
        if (instance.containsKey(key)) {
            instance.remove(key);
        }
        instance.put(key, value);
    }

    public Vector<K> Keys() {
        return new Vector<>(instance.keySet());
    }

    public Vector<V> Values() {
        return new Vector<>(instance.values());
    }

    public boolean ContainsKey(K key) {
        return instance.containsKey(key);
    }

    public void Clear() {
        instance.clear();
    }

    public void Add(K key, V value) {
        instance.put(key, value);
    }

    public boolean Remove(K key) {
        if (instance.containsKey(key)) {
            instance.remove(key);
            return true;
        }
        return false;
    }

    public Property<Integer> Count = new Property<Integer>() {
        @Override
        public Integer get() {
            return instance.size();
        }
    };
}
