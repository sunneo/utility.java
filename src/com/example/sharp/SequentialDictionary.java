package com.example.sharp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.example.sharp.Delegates.IterableEx;

/**
 * a dictionary which stores key in sequential
 * 
 * @author user
 *
 */
public class SequentialDictionary<K, V> extends Dictionary<K, V> {
	BaseLinkedList<K> seqKey = new BaseLinkedList<>();
	LinkedHashMap<K, BaseLinkedListNode<K>> keyMap = new LinkedHashMap<>();
	/**
	 * drop callback support 
	 */
	public SequentialDictionary<K,V> dropCallbacks() {
	    super.dropCallbacks();
	    return this;
	}
	public SequentialDictionary() {
		
	}
	public SequentialDictionary(Map<K, V> map) {
		for(K k:map.keySet()) {
			this.set(k,map.get(k));
		}
	}
	public IterableEx<K> getSequentialKey() {
		return Delegates.forall(this.seqKey);
		
	}
	
	@Override
	public void set(K key, V value) {
		set(key,value,false);
	}
	

	public void set(K key, V value, boolean reorderOnConflict) {
		super.set(key, value);
		// reorder
		if(reorderOnConflict) {
			if (keyMap.containsKey(key)) {
			    BaseLinkedListNode<K> node = keyMap.get(key);
				node.Remove();
			}
			keyMap.put(key, seqKey.AddLast(key));
		} else {
			if (!keyMap.containsKey(key)) {
				keyMap.put(key, seqKey.AddLast(key));
			}
			
		}
	}
	

	@Override
	public V put(K arg0, V arg1) {
		set(arg0,arg1);
		return arg1;
	}

	@Override
	public boolean Remove(Object key) {
		boolean ret = super.Remove(key);
		if (ret) {
		    BaseLinkedListNode<K> node = keyMap.get(key);
			node.Remove();
			keyMap.remove(key);
		}
		return ret;
	}

	@Override
	public void Clear() {
		super.Clear();
		seqKey.Clear();
		this.keyMap.clear();
	}
	public Vector<V> getSequentialValues() {
	    return Delegates.forall(getSequentialKey()).translate((x)->get(x)).toVector();
    }
	
	public ArrayList<KeyValuePair<K, V>> getSequentialEntrySet() {
		return Delegates.forall(getSequentialKey()).translate((x)->KeyValuePair.pair(x, get(x))).toArrayList();
	}
	
	
	@Override
	public Vector<V> Values() {
		return getSequentialValues();
	}
	@Override
	public Collection<V> values() {
		return getSequentialValues();
	}
	
	@Override
	public Vector<K> Keys() {
		return Delegates.forall(getSequentialKey()).toVector();
	}
	@Override
	public Set<K> keySet() {
		return this.instance.keySet();
	}
	@Override
	public void dispose() {
		if(seqKey != null) {
			seqKey.Clear();
			seqKey.dispose();
			seqKey = null;
		}
		if(keyMap != null) {
			keyMap.clear();
			keyMap = null;
		}
		super.dispose();
	}
	
	

}
