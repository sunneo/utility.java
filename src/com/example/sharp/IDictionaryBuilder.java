package com.example.sharp;

public interface IDictionaryBuilder<K,V> {
	public IDictionaryBuilder<K,V> map(K key,V val);
	public <T extends BaseDictionary<K,V>> T build();
}
