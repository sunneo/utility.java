package com.example.sharp;


public class GenericDataSet {
	Dictionary<String,Object> dataSet = new Dictionary<String, Object>();
	
	public GenericDataSet() {
		
	}
	/**
	 * initialize GenericDataSet
	 * @param items list of [key0],[value1],[key1],[value1],[key2],[value2]...
	 */
	public GenericDataSet(Object... items) {
		for(int i=0; i<items.length; i+=2) {
			String key = (String)items[i];
			Object value = items[i+1];
			set(key,value);
		}
	}
	/**
	 * initialize GenericDataSet
	 * @param items list of KeyValuePair<String,Object> items.
	 */
	@SafeVarargs
	public GenericDataSet(KeyValuePair<String,Object>... items) {
		for(int i=0; i<items.length; ++i) {
			KeyValuePair<String,Object> item = items[i];
			set(item.Key,item.Value);
		}
	}
	
	/**
	 * set value 
	 * @param <T>
	 * @param key
	 * @param value
	 */
	public <T> void set(String key,T value) {
		dataSet.set(key, value);
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
}
