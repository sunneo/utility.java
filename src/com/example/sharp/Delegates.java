package com.example.sharp;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.example.sharp.coroutine.Coroutine;
public class Delegates {
    public static interface Func<T> {
        public T Invoke();
    }

    public static interface Func1<P, T> {
        public T Invoke(P arg1);
    }

    public static interface Func2<P1, P2, T> {
        public T Invoke(P1 arg1, P2 arg2);
    }

    public static interface Func3<P1, P2, P3, T> {
        public T Invoke(P1 arg1, P2 arg2, P3 arg3);
    }

    public static interface Func4<P1, P2, P3, P4, T> {
        public T Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4);
    }

    public static interface Func5<P1, P2, P3, P4, P5, T> {
        public T Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5);
    }

    public static interface Func6<P1, P2, P3, P4, P5, P6, T> {
        public T Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5, P6 arg6);
    }

    public static interface Func7<P1, P2, P3, P4, P5, P6, P7, T> {
        public T Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5, P6 arg6, P7 arg7);
    }

    public static interface Func8<P1, P2, P3, P4, P5, P6, P7, P8, T> {
        public T Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5, P6 arg6, P7 arg7, P8 arg8);
    }

    public static interface Action {
        public void Invoke();
    }

    public static interface Action1<P> {
        public void Invoke(P arg1);
    }

    public static interface Action2<P1, P2> {
        public void Invoke(P1 arg1, P2 arg2);
    }

    public static interface Action3<P1, P2, P3> {
        public void Invoke(P1 arg1, P2 arg2, P3 arg3);
    }

    public static interface Action4<P1, P2, P3, P4> {
        public void Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4);
    }

    public static interface Action5<P1, P2, P3, P4, P5> {
        public void Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5);
    }

    public static interface Action6<P1, P2, P3, P4, P5, P6> {
        public void Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5, P6 arg6);
    }

    public static interface Action7<P1, P2, P3, P4, P5, P6, P7> {
        public void Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5, P6 arg6, P7 arg7);
    }

    public static interface Action8<P1, P2, P3, P4, P5, P6, P7, P8> {
        public void Invoke(P1 arg1, P2 arg2, P3 arg3, P4 arg4, P5 arg5, P6 arg6, P7 arg7, P8 arg8);
    }

    public static interface IterableEx<T> extends Iterable<T>{
    	public default <T2> IterableEx<T2> translate(Func1<T, T2> translator){
    		return null;
    	}
    	public default <K> Dictionary<K,Vector<T>> group(Delegates.Func1<T, K> transform){
    		return Delegates.groupBy(this, transform);
    	}
    	
    	/**
    	 * merge 2 iterator
    	 */
    	@SuppressWarnings("unchecked")
		public default IterableEx<T> merge(Iterable<T> concat){
    		return null;
    	}
    	/**
    	 * create dictionary from mapping function and accept function
    	 * @param <V>
    	 * @param transform
    	 * @param accept
    	 * @return
    	 */
    	public default <V> Dictionary<T,V> map(Delegates.Func1<T, V> transform, Delegates.Func1<T, Boolean> accept){
    		return null;
    	}
    	/**
    	 * create dictionary from mapping function
    	 * @param <V>
    	 * @param transform
    	 * @return
    	 */
    	public default <V> Dictionary<T,V> map(Delegates.Func1<T, V> transform){
    		return map(transform,null);
    	}
    	/**
    	 * create filtered iterator from given filter function.
    	 * @param accept
    	 * @return
    	 */
    	public default IterableEx<T> filter(Delegates.Func1<T, Boolean> accept){
    		return Delegates.filter(this, accept);
    	}
    	/**
    	 * convert to Array
    	 * @return
    	 */
    	public default T[] toArray() {
    		return Delegates.toArray(this);
    	}
    	/**
    	 * convert to vector
    	 * @return
    	 */
    	public default Vector<T> toVector(){
    		return Delegates.toVector(this);
    	}
    	/**
    	 * reduce whole iterable into 1 value 
    	 * @param reduceFnc function to reduce 2 value into 1, like minimal, maximal
    	 * @return
    	 */
    	public default T reduce(Delegates.Func2<T,T, T> reduceFnc){
    		return Delegates.reduce(this, reduceFnc);
    	}
    	/**
    	 * convert to linkedlist
    	 * @return
    	 */
    	public default LinkedList<T> toList(){
    		return Delegates.tolist(this);
    	}
    }
    public static interface IteratorEx<T> extends Iterator<T>{
    	public default <T2> IteratorEx<T2> translate(Func1<T, T2> translator){
    		return null;
    	}
    	@SuppressWarnings("unchecked")
		public default IteratorEx<T> merge(Iterator<T> concat){
    		return null;
    	}

    	public default <K> Dictionary<K,Vector<T>> group(Delegates.Func1<T, K> transform){
    		return Delegates.groupBy(Delegates.forall(this), transform);
    	}
    	public default <V> Dictionary<T,V> map(Delegates.Func1<T, V> transform, Delegates.Func1<T, Boolean> accept){
    		return null;
    	}
    	public default <V> Dictionary<T,V> map(Delegates.Func1<T, V> transform){
    		return map(transform,null);
    	}
    	public default IteratorEx<T> filter(Delegates.Func1<T, Boolean> accept){
    		return Delegates.filter(this, accept);
    	}
    	public default T[] toArray() {
    		return Delegates.toArray(this);
    	}
    	public default Vector<T> toVector(){
    		return Delegates.toVector(this);
    	}
    	/**
    	 * reduce whole iterable into 1 value 
    	 * @param reduceFnc function to reduce 2 value into 1, like minimal, maximal
    	 * @return
    	 */
    	public default T reduce(Delegates.Func2<T,T, T> reduceFnc){
    		return Delegates.reduce(this, reduceFnc);
    	}
    	/**
    	 * convert to linkedlist
    	 * @return
    	 */
    	public default LinkedList<T> toList(){
    		return Delegates.tolist(this);
    	}
    }
    static class IterableExImpl<T> implements IterableEx<T>{
    	Iterable<T> instance;
    	public IterableExImpl(Iterable<T> instance) {
    		this.instance = instance;
    	}
    	
    	public <T2> IterableEx<T2> translate(Func1<T, T2> translator){
    		return new IterableExImpl<T2>(Delegates.forall(instance,translator));
    	}
    	@SuppressWarnings("unchecked")
		public IterableEx<T> merge(Iterable<T> concat){
    		return new IterableExImpl<T>(Delegates.forall(this,concat));
    	}
    	public <V> Dictionary<T,V> map(Delegates.Func1<T, V> transform, Delegates.Func1<T, Boolean> accept){
    		return Delegates.map(instance, transform, accept);
    	}
		@Override
		public Iterator<T> iterator() {
			return new IteratorExImpl<T>(instance.iterator());
		}
    }
    static class IteratorExImpl<T> implements IteratorEx<T>{
    	Iterator<T> instance;
    	public IteratorExImpl(Iterator<T> instance) {
    		this.instance = instance;
    	}
    	
    	public <T2> IteratorEx<T2> translate(Func1<T, T2> translator){
    		return new IteratorExImpl<T2>(Delegates.iterator(Delegates.forall(instance),translator));
    	}
    	@SuppressWarnings("unchecked")
		public IteratorEx<T> merge(Iterator<T>... concat){
    		return new IteratorExImpl<T>(Delegates.mergeIterator(concat));
    	}
    	public <V> Dictionary<T,V> map(Delegates.Func1<T, V> transform, Delegates.Func1<T, Boolean> accept){
    		Iterable<T> iterable=Delegates.forall(instance);
    		return Delegates.map(iterable, transform, accept);
    	}
		@Override
		public boolean hasNext() {
			return instance.hasNext();
		}

		@Override
		public T next() {
			return instance.next();
		}
    	
		
    }
    public static <T> IterableEx<T> NullIterable() {
        return new IterableEx<T>() {

            @Override
            public Iterator<T> iterator() {

                return NullIterator();
            }

        };
    }
    public static <T> IteratorEx<T> NullIterator() {
        return new IteratorEx<T>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                return null;
            }

        };
    }

    

    /**
     * enumerate an array
     * 
     * @param <T>   array type
     * @param array target array to iterate
     * @return array iterator
     */
    public static <T> IteratorEx<T> iterator(T[] array) {
    	Iterator<T> ret =  new Iterator<T>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                if(array == null) return false;
                if(array.length == 0) return false;
                return index < array.length;
            }

            @Override
            public T next() {
            	if(index >= array.length) return null;
                T ret = array[index];
                ++index;
                return ret;
            }

        };
        return new IteratorExImpl<T>(ret);
    }
    /**
     * enumerate an Enumeration
     * 
     * wrap it into iterator.
     */
    public static <T> IteratorEx<T> iterator(Enumeration<T> enumeration) {
    	Iterator<T> ret =   new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
            
        };
        return new IteratorExImpl<T>(ret);
    }
    /**
     * enumerate an StringTokenizer
     * 
     * @param <T>   array type
     * @param array target array to iterate
     * @return array iterator
     */
    public static IteratorEx<String> iterator(StringTokenizer enumeration) {
    	Iterator<String> ret = new Iterator<String>() {
            
            @Override
            public boolean hasNext() {
                 return enumeration.hasMoreTokens();
            }

            @Override
            public String next() {
                return (String)enumeration.nextToken();
            }
            
        };
        return new IteratorExImpl<String>(ret);
    }
    
    /**
     * enumerate an collection(overloading)
     * 
     * @param <T>        collection data type
     * @param collection target collection to iterate
     * @return collection iterator
     */
    public static <T> IteratorEx<T> iterator(Collection<T> collection) {
    	return new IteratorExImpl<T>( collection.iterator() );
    }
    public static <T1,T2> IterableEx<T2> forall(T1[] iter,Func1<T1,T2> transform){
    	Iterable<T2> ret =  Delegates.forall(Delegates.iterator(Delegates.forall(iter),transform));
    	return new IterableExImpl<T2>(ret);
    }
    public static <T1,T2> Iterable<T2> forall(Iterable<T1> iter,Func1<T1,T2> transform){
    	Iterable<T2> ret =  Delegates.forall(Delegates.iterator(iter,transform));
    	return new IterableExImpl<T2>(ret); 
    }
    /**
     * enumerate an array
     * 
     * @param <T>   array type
     * @param array target array to iterate
     * @return array iterator
     */
    public static <T> IterableEx<T> forall(T[] array) {
    	Iterable<T> ret = null;
    	if(array == null || array.length == 0) {
    		ret = Delegates.NullIterable();
    	} else {
	    	 ret = new Iterable<T>() {
	
	            @Override
	            public Iterator<T> iterator() {
	                return Delegates.iterator((T[]) array);
	            }
	
	        };
    	}
    	return new IterableExImpl<T>(ret); 
    }
    /**
     * lazy iterable-concatenate
     * 
     * this function can combine many iterable object with same type 
     * into a single iterable, so user can iterate all of them with just 1 statement.
     * 
     * @param <T>   array type
     * @param concat two more iterables
     * @return combined iterable.
     */
    @SafeVarargs
	public static <T> IterableEx<T> forall(Iterable<T>... concat) {
    	if(concat == null) {
    		return Delegates.NullIterable();
    	}
    	Iterable<T> ret = new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                	Iterator<Iterable<T>> iterators = Delegates.iterator(concat);
                	Iterable<T> lastIterable=null;
                	Iterator<T> lastIterator=null;
					@Override
					public boolean hasNext() {
						boolean result=false;
						if(iterators.hasNext()) {
							while(iterators.hasNext()) {
								if(lastIterator == null || !lastIterator.hasNext()) {
									lastIterable=iterators.next();
									if(lastIterable != null) {
									   lastIterator = lastIterable.iterator();
									}
									if(lastIterator != null && lastIterator.hasNext()) {
										result = true;
										break;
									}
								} else {
									break;
								}
							}
						} 
						if(lastIterator != null) {
							result = lastIterator.hasNext();
						}
						
						return result;
					}

					@Override
					public T next() {
						if(lastIterator == null || !lastIterator.hasNext()) {
							lastIterable=iterators.next();
							if(lastIterable != null) {
							   lastIterator = lastIterable.iterator();
							}
						}
						return lastIterator.next();
					}
                	
                };
            }

        };
        return new IterableExImpl<T>(ret); 
    }
    @SafeVarargs
	public static <T> IteratorEx<T> mergeIterator(Iterator<T>... concat){
    	Iterator<T> ret =  new Iterator<T>() {
        	Iterator<Iterator<T>> iterators = Delegates.forall(concat).iterator();
        	Iterator<T> lastIterator=null;
			@Override
			public boolean hasNext() {
				return iterators.hasNext() || (lastIterator != null && lastIterator.hasNext());
			}

			@Override
			public T next() {
				while((lastIterator == null || !lastIterator.hasNext()) && iterators.hasNext()) {
					lastIterator=iterators.next();
				}
				return lastIterator.next();
			}
        	
        };
        return new IteratorExImpl<T>(ret); 
    }
    @SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterable<T> iterable){
        Vector<T> vec = toVector(iterable);
        if(vec.isEmpty()) {
        	return (T[]) null;
        }
        try {
        	if(vec.get(0) == null) return null;
	        T[] ret = (T[]) Array.newInstance(vec.get(0).getClass(), vec.size());
	        return vec.toArray(ret);
        }catch(Exception ee) {
        	return null;
        }
    }
	public static <T> T[] toArray(Iterator<T> iterator) {
		return toArray(Delegates.forall(iterator));
	}
    /**
     * convert an iterable into a vector
     * @param <T> value type
     * @param iterable an iterable object
     * @return list of values in vector
     */
    public static <T> Vector<T> toVector(Iterable<T> iterable){
        Vector<T> ret = new Vector<T>();
        for(T value:iterable) {
            ret.add(value);
        }
        return ret;
    }
    public static <T> Vector<T> toVector(Vector<T> iterator){
    	// prevent unnecessary transform 
        return iterator;
    }
    public static <T> Vector<T> toVector(T[] iterator){
        return toVector(forall(iterator));
    }
    public static <T> Vector<T> toVector(Iterator<T> iterator){
        return toVector(forall(iterator));
    }
    public static <T> Vector<T> toVector(Enumeration<T> enumeration){
        return toVector(forall(enumeration));
    }
    public static Vector<String> toVector(StringTokenizer tokenizer){
    	
        return toVector(forall(tokenizer));
    }
    
    public static IterableEx<String> forall(StringTokenizer tokenizer){
        return new IterableExImpl<String>(()->iterator(tokenizer)); 
    }
    public static <T> IterableEx<T> forall(Enumeration<T> enumeration){
        return new IterableExImpl<T>(()->iterator(enumeration));
    }
    public static <T> IterableEx<T> forall(Iterator<T> iterator) {
        return new IterableExImpl<T>(() -> iterator);
    }

    /**
     * iterate an T1 -typed iterable with a transform function which returns a value in T2 refer to T1.
     * 
     * the following example prepares a hashtable with <SequeceNumber, Random > mapping
     * the iterator will return strings 
     * 
     * <pre>
     * {@code
     * 
     * Hashtable<Integer, Double> randomKv = new Hashtable<>();
     * for(int i=0; i<100; ++i){
     *    randomKv.put(i, Math.random());
     * }
     * Iterator<String> onlyValue=iterator(randomKv.entrySet(), (entry)->Integer.toString(entry.getKey())+"_"+Double.toString(entry.getValue()));
     * 
     * } 
     * </pre>
     * @param <T1> type of iterable
     * @param <T2> transformed type when iterating, transformed by transform
     * @param iter1 iterable 
     * @param transform transform function
     * @return iterator to transformed results.
     * 

     *
     */
    public static <T1,T2> IteratorEx<T2> iterator(Iterable<T1> iter1, Delegates.Func1<T1, T2> transform){
    	Iterator<T2> ret =  new Iterator<T2>() {
    		Iterator<T1> iter = iter1.iterator();
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public T2 next() {
				return transform.Invoke(iter.next());
			}
    		
    	};
    	return new IteratorExImpl<T2>(ret);
    }
    
    public static <T1, T2> IterableEx<KeyValuePair<T1,T2>> EnumerateNestedIterator(IterableEx<T1> p1, Func1<T1, IterableEx<T2>> iteratorGenerator) {
    	return Delegates.forall(
     			  EnumerateNestedIterator(p1.iterator(), (val)->iteratorGenerator.Invoke(val).iterator())
    		   );
    }
    
    /**
     * enumerate nested iterator, this function can be chained to traversal multiple
     * level iterator.
     * 
     * @param <T1>              outer iterator data type
     * @param <T2>              result data type
     * @param p1                outer iterator
     * @param iteratorGenerator generator for retrieving inner iterator
     * @return an iterator returns inner result
     */
    public static <T1, T2> IteratorEx<KeyValuePair<T1,T2>> EnumerateNestedIterator(Iterator<T1> p1, Func1<T1, Iterator<T2>> iteratorGenerator) {
    	Iterator<KeyValuePair<T1,T2>> ret =  new Iterator<KeyValuePair<T1,T2>>() {

            private boolean   inited           = false;
            Property<Boolean> entryHasNext     = new Property<Boolean>();
            Iterator<T2>      nestedIterator = null;
            T1 current;
            @Override
            public boolean hasNext() {
                if (!inited) {
                    inited = true;
                    if (p1 == null) {
                        return false;
                    }
                }
                boolean nestedIteratorNeedNext = false;
                if (nestedIterator == null || !nestedIterator.hasNext()) {
                    nestedIteratorNeedNext = true;
                    entryHasNext.hasValue = false;
                    nestedIterator = null;
                }
                if (nestedIteratorNeedNext) {
                    while (p1.hasNext()) {
                        current = p1.next();
                        nestedIterator = iteratorGenerator.Invoke(current);
                        /// returns null iterator to indicate that the next p1 value is not suitable to
                        /// iterate
                        if (nestedIterator != null && nestedIterator.hasNext()) {
                            nestedIteratorNeedNext = false;
                            break;
                        }
                    }
                }
                return !nestedIteratorNeedNext;
            }

            @Override
            public KeyValuePair<T1,T2> next() {
                if (nestedIterator != null) {
                    return KeyValuePair.pair(current,nestedIterator.next());
                }
                return null;
            }

        };
        return new IteratorExImpl<KeyValuePair<T1,T2>>(ret);
    }
    
    /**
     * group a list of items by a key
   	 * <pre>
   	 * {@code
   	 * 
   	 * String[] vals = new String[] { "0Q","1P","2O","3N","4M","5L","01k","02j","11i","12h","13g","25f","33e","31d","32c","48b","59a" };
   	 * Dictionary<String,Vector<String> > groups = Delegates.groupBy(Delegates.toVector(Delegates.forall(vals)), (v)->v.charAt(0) );
   	 * for(Map.Entry<Character,Vector<String>> kv:groups) {
   	 *    Character key = kv.getKey();
   	 *    Vector<String> values=kv.getValue();
   	 *    for(String val:values){
   	 *       System.out.printf("key:%c -> %s\n",key,val);
   	 *    }
   	 * }
   	 * 
   	 * }
   	 * </pre>
     * @param <K> key type
     * @param <V> value type
     * @param filtered list of values
     * @param groupNameDelegate a delegate to map key -> Value (given Value, return key)
     * @return a dictionary which maps key -> values

   	 */
    public static <K,V> Dictionary<K,Vector<V>> groupBy(Iterable<V> filtered, Delegates.Func1<V, K> groupNameDelegate){
    	Dictionary<K,Vector<V>> ret = new Dictionary<K,Vector<V>>();
		for(V entry:filtered) {
			K groupName = groupNameDelegate.Invoke(entry);
			if(groupName == null) continue;
			Vector<V> subGroup = null;
			if(ret.ContainsKey(groupName)) {
				subGroup = ret.get(groupName);
			} else {
				subGroup = new Vector<V>();
				ret.set(groupName, subGroup);
			}
			subGroup.add(entry);
		}
		return ret;
    }
    
   	public static <K> K[] distinct(Iterable<K> keys){
       	return distinct(keys, (x)->x);
    }
   	/**
   	 * get distinct items with original order.
   	 * 
   	 * <pre>{@code
   	 * String[] vals = new String[] { "0Q","1P","2O","3N","4M","5L","01k","02j","11i","12h","13g","25f","33e","31d","32c","48b","59a" };
   	 * String[] keys = Delegates.distinct(Delegates.forall(vals), (v)->v.charAt(0) );
   	 * for(String key:keys) System.out.println(key);
   	 * }
   	 * </pre>
   	 *  
   	 * @param <K> key type
   	 * @param <V> value type
   	 * @param vals values
   	 * @param vtok a delegate to map from value to a key
   	 * @return a list of distinct keys 
   	 */
    @SuppressWarnings("unchecked")
	public static <K, V> K[] distinct(Iterable<V> vals, Delegates.Func1<V, K> vtok){
    	Dictionary<K,K> set = new Dictionary<K, K>();
    	Vector<K> values = new Vector<K>();
    	for(V v:vals) {
    		K key = vtok.Invoke(v);
    		if(!set.ContainsKey(key)) {
    			set.set(key, key);
    			values.add(key);
    		}
    	}
    	return (K[])values.toArray();
    }

    public static <K> Delegates.Func1<K, Boolean> NonNull(){
    	return (x)->x!=null;
    }
    
    public static <K,V> Dictionary<K,V> map(Iterable<K> keys, Delegates.Func1<K, V> transform, Delegates.Func1<K, Boolean> accept){
    	Dictionary<K, V> ret = new Dictionary<K, V>();
    	if(accept == null) {
    		accept=NonNull();
    	}
    	for(K k:keys) {
    		if(!accept.Invoke(k)) continue;
    		ret.set(k,transform.Invoke(k));
    	}
    	return ret;
    }
    
    public static <K> Dictionary<K,K> map(K[] keys, Delegates.Func1<K, Boolean> accept){
    	return map(Delegates.forall(keys),accept);
    }
    public static <K> Dictionary<K,K> map(Iterable<K> keys, Delegates.Func1<K, Boolean> accept){
    	Dictionary<K, K> ret = new Dictionary<K, K>();
    	if(accept == null) {
    		accept=NonNull();
    	}
    	for(K k:keys) {
    		if(!accept.Invoke(k)) continue;
    		ret.set(k,k);
    	}
    	return ret;
    }
    

    public static IterableEx<String> filter(StringTokenizer iter, Delegates.Func1<String,Boolean> accept){
    	return Delegates.forall(filter(Delegates.iterator(iter),accept));
    }
    public static <T> IterableEx<T> filter(T[] iter, Delegates.Func1<T,Boolean> accept){
    	return Delegates.forall(filter(Delegates.iterator(iter),accept));
    }
    public static <T> IterableEx<T> filter(Iterable<T> iter, Delegates.Func1<T,Boolean> accept){
    	return Delegates.forall(filter(iter.iterator(),accept));
    }
    public static <T> IteratorEx<T> filter(Iterator<T> iter, Delegates.Func1<T,Boolean> accept){
    	Iterator<T> ret = new Iterator<T>() {
    		boolean tested=false;
    		Coroutine newCoroutine() {
    			Coroutine cor= new Coroutine();
    			cor.addInstruction("TESTITERATOR", (pthis)->{
    				if(!iter.hasNext()) {
    					pthis.jmp("END");
    					pthis.stop();
    				}
    			});
    			cor.addInstruction((pthis) -> {
					T val = iter.next();
					if(accept != null && accept.Invoke(val)) {
						pthis.yield(val);	
					}
					
				});

				cor.addInstruction((pthis) -> {
					pthis.jmp("TESTITERATOR");
				});
				cor.addInstruction("END", (pthis)->{});

				cor.start();
				return cor;
    		}
    		Coroutine cor= newCoroutine();
			@Override
			public boolean hasNext() {
				if(!tested) {
					if (!cor.isYield()) {
						while (cor.exec()) { }
					}
					tested = true;
				}
				return !cor.isStopped();
			}

			@Override
			public T next() {
				if (!cor.isYield()) {
					while (cor.exec()) { }
				}

				T ret = cor.getYieldValue();
				while (cor.exec()) {

				}
				return ret;
			}
    		
    	};
    	return new IteratorExImpl<T>(ret);
    }
    public static <T> IteratorEx<T> flat(Iterator<Iterator<T>> nested){
    	IteratorEx<KeyValuePair<Iterator<T>, T>> iter=Delegates.EnumerateNestedIterator(nested, (n)->n);
    	return iter.translate((x)->x.Value);
    }
    public static <T> IterableEx<T> flat(Iterable<Iterable<T>> nested){
    	IteratorEx<KeyValuePair<Iterable<T>, T>>  iter=Delegates.EnumerateNestedIterator(nested.iterator(), (n)->n.iterator());
    	return Delegates.forall(iter.translate((x)->x.Value));
    }
   
    /**
	 * reduce whole iterable into 1 value 
	 * @param reduceFnc function to reduce 2 value into 1, like minimal, maximal
	 * @return
	 */
	public static <T> T reduce(Iterable<T> iter,Delegates.Func2<T,T, T> reduceFnc){
		T val = null;
		for(T t:iter) {
			if(val == null) {
				val = t;
			} else {
				val = reduceFnc.Invoke(t, val);
			}
		}
		return val;
	}
    /**
	 * reduce whole iterator into 1 value 
	 * @param reduceFnc function to reduce 2 value into 1, like minimal, maximal
	 * @return
	 */
	public static <T> T reduce(Iterator<T> iter,Delegates.Func2<T,T, T> reduceFnc){
		return reduce(Delegates.forall(iter),reduceFnc);
	}
    /**
     * translate from iterable to LinkedList
     * @param <T1>
     * @param itemVector
     * @return
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T1> LinkedList<T1> tolist(Iterable<T1> itemVector) {
		if(itemVector instanceof LinkedList) return (LinkedList)itemVector;
		LinkedList<T1> ret = new LinkedList<>();
		for(T1 item:itemVector) {
			ret.AddLast(item);
		}
		return ret;
	}
	public static <T1> LinkedList<T1> tolist(Iterator<T1> iter){
		return tolist(Delegates.forall(iter));
	}
	
    
}
