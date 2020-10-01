package com.example.sharp;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

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

    public static <T> Iterable<T> NullIterable() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {

                return NullIterator();
            }

        };
    }
    public static <T> Iterator<T> NullIterator() {
        return new Iterator<T>() {

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
    public static <T> Iterator<T> iterator(T[] array) {
        return new Iterator<T>() {
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
    }
    /**
     * enumerate an Enumeration
     * 
     * wrap it into iterator.
     */
    public static <T> Iterator<T> iterator(Enumeration<T> enumeration) {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
            
        };
    }
    /**
     * enumerate an StringTokenizer
     * 
     * @param <T>   array type
     * @param array target array to iterate
     * @return array iterator
     */
    public static Iterator<String> iterator(StringTokenizer enumeration) {
        return new Iterator<String>() {
            
            @Override
            public boolean hasNext() {
                 return enumeration.hasMoreTokens();
            }

            @Override
            public String next() {
                return (String)enumeration.nextToken();
            }
            
        };
    }
    
    /**
     * enumerate an collection(overloading)
     * 
     * @param <T>        collection data type
     * @param collection target collection to iterate
     * @return collection iterator
     */
    public static <T> Iterator<T> iterator(Collection<T> collection) {
        return collection.iterator();
    }
    /**
     * enumerate an array
     * 
     * @param <T>   array type
     * @param array target array to iterate
     * @return array iterator
     */
    public static <T> Iterable<T> forall(T[] array) {
    	if(array == null || array.length == 0) {
    		return Delegates.NullIterable();
    	}
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return Delegates.iterator((T[]) array);
            }

        };
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
	public static <T> Iterable<T> forall(Iterable<T>... concat) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                	Iterator<Iterable<T>> iterators = Delegates.iterator(concat);
                	Iterable<T> lastIterable=null;
                	Iterator<T> lastIterator=null;
					@Override
					public boolean hasNext() {
						if(lastIterator == null || !lastIterator.hasNext()) {
							lastIterable=iterators.next();
							if(lastIterable != null) {
							   lastIterator = lastIterable.iterator();
							}
						}
						return iterators.hasNext() || (lastIterator != null && lastIterator.hasNext());
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
    }
    @SafeVarargs
	public static <T> Iterator<T> mergeIterator(Iterator<T>... concat){
    	return new Iterator<T>() {
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
    }
    @SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterable<T> iterable){
        Vector<T> vec = toVector(iterable);
        if(vec.isEmpty()) {
        	return (T[]) null;
        }
        T[] ret = (T[]) Array.newInstance(vec.get(0).getClass(), vec.size());
        return vec.toArray(ret);
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
    	if(iterable instanceof Vector) {
    		return (Vector<T>)iterable;
    	}
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
    
    public static Iterable<String> forall(StringTokenizer tokenizer){
        return ()->iterator(tokenizer); 
    }
    public static <T> Iterable<T> forall(Enumeration<T> enumeration){
        return ()->iterator(enumeration);
    }
    public static <T> Iterable<T> forall(Iterator<T> iterator) {
        return () -> iterator;
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
    public static <T1,T2> Iterator<T2> iterator(Iterable<T1> iter1, Delegates.Func1<T1, T2> transform){
    	return new Iterator<T2>() {
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
    public static <T1, T2> Iterator<KeyValuePair<T1,T2>> EnumerateNestedIterator(Iterator<T1> p1, Func1<T1, Iterator<T2>> iteratorGenerator) {
        return new Iterator<KeyValuePair<T1,T2>>() {

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
                    return new KeyValuePair<T1,T2>(current,nestedIterator.next());
                }
                return null;
            }

        };
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
    public static <K,V> Dictionary<K,Vector<V>> groupBy(Vector<V> filtered, Delegates.Func1<V, K> groupNameDelegate){
    	Dictionary<K,Vector<V>> ret = new Dictionary<K,Vector<V>>();
		for(V entry:filtered) {
			K groupName = groupNameDelegate.Invoke(entry);
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

    
}
