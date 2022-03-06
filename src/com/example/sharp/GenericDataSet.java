package com.example.sharp;

import java.util.Map;



/**
 * a storage for arbitrary data. 
 *
 */
public class GenericDataSet {
    BaseDictionary<String,Object> dataSet = new BaseDictionary<String, Object>();
	final static Object nullInstance=new Object();
	public BaseDictionary<String,Object> getDictionary(){
		return dataSet;
	}
	public GenericDataSet() {
		
	}
	public void clear() {
		dataSet.Clear();
	}
	
	/**
	 * initialize GenericDataSet
	 * @param items list of [key0],[value1],[key1],[value1],[key2],[value2]...
	 */
	public GenericDataSet(Object... items) {
		addAll(items);
	}
	/**
	 * add all from items in key,value order
	 * @param items list of [key0],[value1],[key1],[value1],[key2],[value2]...
	 */
	public void addAll(Object... items) {
		for(int i=0; i<items.length; i+=2) {
			String key = (String)items[i];
			Object value = items[i+1];
			set(key,value);
		}
	}
	/**
	 * add all from items in key,value order
	 * @param items list of <[key0],[value1]>,<[key1],[value1]>,<[key2],[value2]>...
	 */
	@SuppressWarnings("unchecked")
	public void addAll(KeyValuePair<String,Object>... items) {
		for(int i=0; i<items.length; ++i) {
			KeyValuePair<String,Object> item = items[i];
			set(item.Key,item.Value);
		}
	}
	/**
	 * initialize GenericDataSet
	 * @param items list of KeyValuePair<String,Object> items.
	 */
	@SafeVarargs
	public GenericDataSet(KeyValuePair<String,Object>... items) {
		addAll(items);
	}
	
	/**
	 * set value 
	 * @param <T>
	 * @param key
	 * @param value
	 */
	public <T> void set(String key,T value) {
		if(key != null) {
			if(value == null) {
				dataSet.set(key, nullInstance);
			} else {
				dataSet.set(key, value);
			}
		}
	}
	public void Remove(String key) {
		dataSet.Remove(key);
	}
	/**
	 * get value, when key not found or cast-error, use defaultVal
	 * @param <T>
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T defaultVal) {
		Object objRet = dataSet.get(key);
		// compare reference and check whether it is reference to null instance.
		if(objRet == nullInstance) {
			return null;
		}
		T ret = defaultVal;
		try {
			if(defaultVal != null && objRet != null) {
				 if(!defaultVal.getClass().equals(objRet.getClass())) {
					 return defaultVal;
				 }
			}
			ret = (T)objRet;
		}catch(Exception ee) {
			Tracer.D(ee);
		}
		return ret;
	}
	/**
	 * test if generic data set contains key
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return dataSet.ContainsKey(key);
	}
	/**
	 * get value by key
	 * @param <T>
	 * @param key
	 * @return
	 */
	public <T> T get(String key) {
		return get(key,(T)null);
	}
	/**
	 * get all keys.
	 * @return iterable key set. 
	 */
	public Iterable<String> keySet(){
		return dataSet.Keys();
	}
	/**
	 * get all param values for IParameterValues interface
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map getParameterValues() {
		return this.dataSet.instance;
	}
	public void mergeReplace(GenericDataSet that) {
		this.dataSet.mergeReplace(that.dataSet);
	}
	 
}
