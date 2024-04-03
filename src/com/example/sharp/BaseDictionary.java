package com.example.sharp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class BaseDictionary<K, V> implements Map<K, V>{

    /**
     * lazy builder for creating dictionary in-line.
     * @author user
     *
     */
    class Builder implements IDictionaryBuilder<K,V>{
        BaseDictionary<K, V> currentDic;
        public <T extends BaseDictionary<K, V>> T build(){
            return (T)currentDic;
        }
        public Builder() {
             currentDic = new BaseDictionary<>();
        }
        public Builder(BaseDictionary<K,V> dic) {
            this.currentDic=dic;
        }
        @Override
        public IDictionaryBuilder<K, V> map(K key, V val) {
            currentDic.set(key, val);
            return this;
        }
    }
    public IDictionaryBuilder<K,V> getBuilder() {
        return new Builder(this);
    } 
    public BaseDictionary() {
    }

    LinkedHashMap<K, V> instance = new LinkedHashMap<>();

    public V get(Object key) {
        return instance.get(key);
    }
    

    public void set(K key, V value) {
        if (instance.containsKey(key)) {
            instance.remove(key);
        }
        instance.put(key, value);
    }
    public Vector<Entry<K,V>> Entries(){
        return new Vector<>(instance.entrySet());
    }

    public Vector<K> Keys() {
        return new Vector<>(instance.keySet());
    }

    public Vector<V> Values() {
        return new Vector<>(instance.values());
    }
    
    @Override
    public BaseDictionary<K, V> clone() {
        BaseDictionary<K, V> ref = new BaseDictionary<>();
        ref.instance.putAll(this.instance);
        return ref;
    }
    public BaseDictionary<V, K> inverse() {
        BaseDictionary<V, K> ref = new BaseDictionary<>();
        forEach((key, val) -> {
            ref.instance.put(val, key);
        });
        return ref;
    }
    public boolean ContainsKey(Object key) {
        return instance.containsKey(key);
    }

    public void Clear() {
        instance.clear();
        instance = new LinkedHashMap<>();
    }

    public void Add(K key, V value) {
        instance.put(key, value);
    }

    public boolean Remove(Object key) {
        if (instance.containsKey(key)) {
            instance.remove(key);
            return true;
        }
        return false;
    }
    public static class ComparisonResult<K,V>{
        public BaseDictionary<K,V> added = new BaseDictionary<K, V>();
        public BaseDictionary<K,V> removed = new BaseDictionary<K, V>();
        public BaseDictionary<K,V> modified = new BaseDictionary<K, V>();
    }
    /**
     * general merge for dictionary
     * @param that other dictionary
     * @return conflicted parts of other 
     */
    BaseDictionary<K, V> merge(BaseDictionary<K, V> that, boolean replaceOnConflict){
        BaseDictionary<K,V> conflictKeyValue = new BaseDictionary<>();
        for(K key:that.Keys()) {
            V value=that.get(key);
            if(replaceOnConflict) {
               this.set(key, value);
            } else {
                if(!this.ContainsKey(key)) {
                    this.set(key, value);
                } else {
                    conflictKeyValue.set(key, value);
                }
            }
        }
        return conflictKeyValue;
    }
    /**
     * merge value from other dictionary, and always replace with conflicted one
     * @param that other dictionary
     * @return this
     */
    public BaseDictionary<K,V> mergeReplace(BaseDictionary<K, V> that){
        merge(that,true);
        return this;
    }
    /**
     * merge value from other dictionary, avoid replace when conflict
     * @param that other dictionary
     * @return conflicted key/value of other dictionary
     */
    public BaseDictionary<K,V> mergeAvoidConflict(BaseDictionary<K, V> that){
        return merge(that,false);
    }
    
    public static <K,V> ComparisonResult<K,V> diff(BaseDictionary<K,V> oldOne, BaseDictionary<K,V> newOne){
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

    /**
     * dictionary count
     * use get() to fetch its value
     */
    public IGetter<Integer> Count = ()->instance==null?0:instance.size();
    /**
     * dictionary is empty
     * use get() to fetch its value
     */
    public IGetter<Boolean> IsEmpty = ()->instance==null||instance.isEmpty();
    boolean bDisposed=false;
    public boolean isDisposed() {
        return bDisposed;
    }
    public void dispose() {
        if(isDisposed()) return;
        if(instance == null) {
            return;
        }
        bDisposed=true;
        Count = null;
        IsEmpty = null;
        if(instance!=null) {
            instance.clear();
        }
        instance =  null;
        
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
    public static <K,V> BaseDictionary<K,V> from(Iterable<KeyValuePair<K,V>> kvIter) {
        BaseDictionary<K, V> ret = new BaseDictionary<K, V>();
        for(KeyValuePair<K,V> kv:kvIter) {
            ret.set(kv.getKey(), kv.getValue());
        }
        return ret;
    }
    @Override
    public void clear() {
        this.Clear();
    }
    @Override
    public boolean containsKey(Object arg0) {
        return this.ContainsKey(arg0);
    }
    @Override
    public boolean containsValue(Object arg0) {
        return this.instance.containsValue(arg0);
    }
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.instance.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return this.instance.isEmpty();
    }
    @Override
    public Set<K> keySet() {
        return this.instance.keySet();
    }
    @Override
    public V put(K arg0, V arg1) {
        this.set(arg0, arg1);
        return arg1;
    }
    @Override
    public void putAll(Map<? extends K, ? extends V> arg0) {
        for(Entry<? extends K,? extends V> entry:arg0.entrySet()) {
            put(entry.getKey(),entry.getValue());
        }
    }
    @Override
    public V remove(Object arg0) {
        V ret = null;
        if(this.ContainsKey(arg0)) {
            ret = this.get(arg0);
            this.Remove(arg0);
        }
        return ret;
    }
    @Override
    public int size() {
        return Count.get();
    }
    @Override
    public Collection<V> values() {
        return Values();
    }

}
