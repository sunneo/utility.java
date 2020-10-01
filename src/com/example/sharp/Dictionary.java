package com.example.sharp;

import java.util.Hashtable;
import java.util.Vector;

import com.example.events.EventDelegate;
import com.example.events.INotification;
import com.example.events.INotificationEventArgs;

public class Dictionary<K, V> {
	public EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemAdded = new EventDelegate<>();
	public EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemRemoved = new EventDelegate<>();
	public EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemUpdated = new EventDelegate<>();
	public EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<Object>>> Cleared = new EventDelegate<>();
	/**
	 * lazy builder for creating dictionary in-line.
	 * @author user
	 *
	 */
	class Builder implements IDictionaryBuilder<K,V>{
		Dictionary<K,V> currentDic = new Dictionary<>();
		public Dictionary<K,V> build(){
			return currentDic;
		}
		public Builder() {
			
		}
		public Builder(Dictionary<K,V> dic) {
			this.currentDic=dic;
		}
		@Override
		public IDictionaryBuilder<K, V> map(K key, V val) {
			currentDic.set(key, val);;
			return this;
		}
	}
	public IDictionaryBuilder<K,V> getBuilder() {
		return new Builder(this);
	} 
    public Dictionary() {
    }

    Hashtable<K, V> instance = new Hashtable<>();

    public V get(K key) {
        return instance.get(key);
    }

    public void set(K key, V value) {
    	boolean changed=false;
        if (instance.containsKey(key)) {
            instance.remove(key);
            changed=true;
        }
        instance.put(key, value);
        if(changed) {
           ItemUpdated.invoke(this, key);
        } else {
        	ItemAdded.invoke(this, key);
        }
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
        Cleared.invoke(this, this);
    }

    public void Add(K key, V value) {
        boolean changed=false;
        if (instance.containsKey(key)) {
            changed=true;
        }
        instance.put(key, value);
        if(changed) {
           ItemUpdated.invoke(this, key);
        } else {
        	ItemAdded.invoke(this, key);
        }
    }

    public boolean Remove(K key) {
        if (instance.containsKey(key)) {
            instance.remove(key);
            ItemRemoved.invoke(this, key);
            return true;
        }
        return false;
    }
    public static class ComparisonResult<K,V>{
    	public Dictionary<K,V> added = new Dictionary<K, V>();
    	public Dictionary<K,V> removed = new Dictionary<K, V>();
    	public Dictionary<K,V> modified = new Dictionary<K, V>();
    }
    public static <K,V> ComparisonResult<K,V> diff(Dictionary<K,V> oldOne, Dictionary<K,V> newOne){
    	ComparisonResult<K, V> ret= new ComparisonResult<K, V>();
    	for(K id:newOne.Keys()) {
			if(!oldOne.ContainsKey(id)) {
				ret.added.set(id, newOne.get(id));
			} else {
				if(!oldOne.get(id).equals(newOne.get(id))) {
					ret.modified.set(id, newOne.get(id));
				}
			}
		}
		for(K id:oldOne.Keys()) {
			if(!newOne.ContainsKey(id)) {
				ret.removed.set(id, oldOne.get(id));
			} else {
				if(!oldOne.get(id).equals(newOne.get(id))) {
					ret.modified.set(id, newOne.get(id));
				}
			}
		}
    	return ret;
    }

    public Property<Integer> Count = new Property<Integer>() {
        @Override
		public Integer get() {
            return instance.size();
        }
    };
}
