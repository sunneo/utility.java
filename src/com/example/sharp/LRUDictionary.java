package com.example.sharp;

import java.util.Vector;

import com.example.events.EventDelegate;
import com.example.events.INotification;
import com.example.events.INotificationEventArgs;

/**
 * A dictionary with LRU(least recently used) algorithm.
 * 
 * LRUDictionary will be initialized with a max capacity, when get item from 
 * dictionary, it will raise item to used.
 * 
 * As adding item to LRUDictionary, it will remove an item which is least recently used
 * from dictionary while capacity is full.
 *
 * 
 * @param <K> key type
 * @param <V> value type
 */
public class LRUDictionary<K,V> {
	int capacity;
	Dictionary<K, KeyValuePair<LinkedListNode<K>, V>> dict = new Dictionary<>();
	LinkedList<K> list = new LinkedList<>();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemAdded = dict.ItemAdded.route();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg2<K,V>>> BeforeRemoveItem = new EventDelegate<>();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemRemoved = dict.ItemRemoved.route();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemUpdated = dict.ItemUpdated.route();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<Object>>> Cleared = dict.Cleared.route();
	/**
	 * get all values
	 */
	public Vector<V> Values(){
		return Delegates.forall(dict.Values()).translate((kv)->kv.Value).toVector();
	}
	/**
	 * get all keys
	 * @return
	 */
	public Vector<K> Keys(){
		return dict.Keys();
	}
	/**
	 * add listener to observe key-removed
	 * @param callback
	 */
	public void addKeyRemovedListener(INotification<INotificationEventArgs.INotificationEventArg1<K>> callback) {
		ItemRemoved.addDelegate(callback);
	}
	/**
	 * remove listener for observe key-removed
	 * @param callback
	 */
	public void removeKeyRemovedListener(INotification<INotificationEventArgs.INotificationEventArg1<K>> callback) {
		ItemRemoved.removeDelegate(callback);
	}
	/**
	 * add listener to observe notifications before key-removed
	 * @param callback
	 */
	public void addKeyBeforeRemovedListener(INotification<INotificationEventArgs.INotificationEventArg2<K,V>> callback) {
		BeforeRemoveItem.addDelegate(callback);
	}
	/**
	 * delete listener for notifications before key-removed
	 * @param callback
	 */
	public void removeKeyBeforeRemovedListener(INotification<INotificationEventArgs.INotificationEventArg2<K,V>> callback) {
		BeforeRemoveItem.removeDelegate(callback);
	}
	/**
	 * dispose object
	 */
	public void dispose() {
		ItemRemoved.dispose();
		BeforeRemoveItem.dispose();
		ItemAdded.dispose();
		ItemUpdated.dispose();
		Cleared.dispose();
		dict.dispose();
		list.dispose();
	}
	/**
	 * create LRUDictionary with max capacity 128 items
	 */
	public LRUDictionary() {
		this(128);
	}
	/**
	 * create LRUDictionary with given capacity
	 * @param capacity max size
	 */
	public LRUDictionary(int capacity) {
		this.capacity = capacity;
	}
	/**
	 * check if dictionary contains key
	 * @param k
	 * @return
	 */
	public boolean ContainsKey(K k) {
		return dict.ContainsKey(k);
	}
	/**
	 * get value if key available
	 * this will also move item to last of list 
	 * @param k key
	 * @return value
	 */
	public V get(K k) {
		KeyValuePair<LinkedListNode<K>, V> kv = null;
		if(dict.ContainsKey(k)) {
			kv=dict.get(k);
			kv.Key.Remove();
			if(kv.Key != null) {
			   list.AddLast(kv.Key);
			}
			return kv.Value;
		}
		return null;
	}
	/**
	 * remove item from dictionary
	 * @param k key
	 * @return value if available, or null
	 */
	public V remove(K k) {
		KeyValuePair<LinkedListNode<K>, V> kv = null;
		if(dict.ContainsKey(k)) {
			kv=dict.get(k);
			kv.Key.Remove();
			BeforeRemoveItem.invoke(this, k, kv.Value);
			dict.Remove(k);
		}
		return null;
	}
	/**
	 * put value to dictionary,
	 * if key existed, it will replace the value
	 * latest value will be placed to end of list
	 * @param k key
	 * @param v value
	 * @return new value 
	 */
	public V set(K k,V v) {
		KeyValuePair<LinkedListNode<K>, V> kv = null;
		if(dict.ContainsKey(k)) {
			kv=dict.get(k);
			kv.Key.Remove();
			kv.Value = v;
		} else {
			if(list.Count.get()+1 > capacity) {
				LinkedListNode<K> victim = list.RemoveFirst();
				if(victim != null) {
					kv = dict.get(victim.Value);
					if(kv != null) {
					   BeforeRemoveItem.invoke(this,kv.Key.Value,kv.Value);
					}
				}
				dict.Remove(k);
			}
			kv = new KeyValuePair<>();
			kv.Key = list.AddLast(k);
			kv.Value = v;
			dict.set(k,kv);
		}
		list.AddLast(kv.Key);
		return v;
	}

}
