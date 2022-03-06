package com.example.sharp;

import com.example.events.EventDelegate;
import com.example.events.INotification;
import com.example.events.INotificationEventArgs;

public class Dictionary<K, V> extends BaseDictionary<K, V>{
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemAdded = new EventDelegate<>();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemRemoved = new EventDelegate<>();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<K>>> ItemUpdated = new EventDelegate<>();
	public final EventDelegate<INotification<INotificationEventArgs.INotificationEventArg1<Object>>> Cleared = new EventDelegate<>();
	public static class DebugCallbacks<K>{
		public final INotification<INotificationEventArgs.INotificationEventArg1<K>> OnItemAdded = (s,e)->{
			Tracer.D("DEBUG");
		};
		public final INotification<INotificationEventArgs.INotificationEventArg1<K>> OnItemRemoved = (s,e)->{
			Tracer.D("DEBUG");
		};
		public final INotification<INotificationEventArgs.INotificationEventArg1<K>> OnItemUpdated = (s,e)->{
			Tracer.D("DEBUG");
		};
		public final INotification<INotificationEventArgs.INotificationEventArg1<Object>> OnCleared = (s,e)->{
			Tracer.D("DEBUG");
		};
	}
	DebugCallbacks<K> debugCallBacks;
	public DebugCallbacks<K> getDebugCallbacks(){
		if(debugCallBacks==null) {
			debugCallBacks=new DebugCallbacks<>();
		}
		return debugCallBacks;
	}
	   /**
     * drop callback support 
     */
    public Dictionary<K,V> dropCallbacks() {
        this.debugCallBacks = null;
        this.ItemAdded.dispose();
        this.ItemRemoved.dispose();
        this.ItemUpdated.dispose();
        this.Cleared.dispose();
        return this;
    }
	
	public IDictionaryBuilder<K,V> getBuilder() {
		return new Builder(this);
	} 
    public Dictionary() {
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
   
    
    @Override
	public Dictionary<K, V> clone() {
    	Dictionary<K, V> ref = new Dictionary<>();
    	ref.instance.putAll(this.instance);
    	return ref;
	}
    public Dictionary<V, K> inverse() {
    	Dictionary<V, K> ref = new Dictionary<>();
    	forEach((key, val) -> {
			ref.instance.put(val, key);
    	});
    	return ref;
    }

    public void Clear() {
        super.Clear();
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

    public boolean Remove(Object key) {
        if (instance.containsKey(key)) {
            instance.remove(key);
            ItemRemoved.invoke(this, key);
            return true;
        }
        return false;
    }
    
    public void dispose() {
    	if(isDisposed()) return;
    	super.dispose();
    	if(ItemAdded != null) {
    		ItemAdded.dispose();
    	}
    	
    	if(ItemRemoved != null) {
    		ItemRemoved.dispose();
    	}
    	
    	if(ItemUpdated!=null) {
    		ItemUpdated.dispose();
    	}
    	
    	if(Cleared != null) {
    		Cleared.dispose();
    	}
    
    }
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	/**
	 * create dictionary from kv pairs
	 * @param <K>
	 * @param <V>
	 * @param kvIter
	 * @return
	 */
	public static <K,V> Dictionary<K,V> from(Iterable<KeyValuePair<K,V>> kvIter) {
		Dictionary<K, V> ret = new Dictionary<K, V>();
		for(KeyValuePair<K,V> kv:kvIter) {
			ret.set(kv.getKey(), kv.getValue());
		}
		return ret;
	}

}
