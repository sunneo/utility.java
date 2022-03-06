package com.example.sharp;

import java.util.Map;

public class KeyValuePair<K extends Object,V extends Object> implements Map.Entry<K, V>{
    public K Key;
    public V Value;
    /**
     * to tuple2
     * @return
     */
    public Tuples.Tuple2<K,V> tuple(){
    	return Tuples.tuple(Key, Value);
    }
    public static <K,V> KeyValuePair<K,V> newInstance(K k, V v){
    	return new KeyValuePair<K,V>(k,v);
    }
    public static <K,V> KeyValuePair<K,V> pair(K k, V v){
    	return newInstance(k,v);
    }
    public KeyValuePair() {
    }

    public KeyValuePair(K k,V v) {
        this.Key = k;
        this.Value = v;
    }

    @Override
	public String toString() {
        return String.format("[%s,%s]", Key,Value);
    }

	@Override
	public K getKey() {
		// TODO Auto-generated method stub
		return this.Key;
	}

	@Override
	public V getValue() {
		return this.Value;
	}

	@Override
	public V setValue(V arg0) {
		this.Value = arg0;
		return this.Value;
	}
}
