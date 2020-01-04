package com.example.sharp;

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
				if (array == null)
					return false;
				return index < array.length;
			}

			@Override
			public T next() {
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
				return (String) enumeration.nextToken();
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
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return Delegates.iterator((T[]) array);
			}

		};
	}

	public static <T> Vector<T> toVector(Iterable<T> iterable) {
		Vector<T> ret = new Vector<T>();
		for (T value : iterable) {
			ret.add(value);
		}
		return ret;
	}

	public static <T> Vector<T> toVector(Iterator<T> iterator) {
		return toVector(forall(iterator));
	}

	public static <T> Vector<T> toVector(Enumeration<T> enumeration) {
		return toVector(forall(enumeration));
	}

	public static Vector<String> toVector(StringTokenizer tokenizer) {
		return toVector(forall(tokenizer));
	}

	public static Iterable<String> forall(StringTokenizer tokenizer) {
		return () -> iterator(tokenizer);
	}

	public static <T> Iterable<T> forall(Enumeration<T> enumeration) {
		return () -> iterator(enumeration);
	}

	public static <T> Iterable<T> forall(Iterator<T> iterator) {
		return () -> iterator;
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
	public static <T1, T2> Iterator<KeyValuePair<T1, T2>> EnumerateNestedIterator(Iterator<T1> p1,
			Func1<T1, Iterator<T2>> iteratorGenerator) {
		return new Iterator<KeyValuePair<T1, T2>>() {

			private boolean inited = false;
			Property<Boolean> entryHasNext = new Property<Boolean>();
			Iterator<T2> nestedIterator = null;
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
						// returns null iterator to indicate that the next p1 value is not suitable to
						// iterate
						if (nestedIterator != null && nestedIterator.hasNext()) {
							nestedIteratorNeedNext = false;
							break;
						}
					}
				}
				return !nestedIteratorNeedNext;
			}

			@Override
			public KeyValuePair<T1, T2> next() {
				if (nestedIterator != null) {
					return new KeyValuePair<T1, T2>(current, nestedIterator.next());
				}
				return null;
			}

		};
	}
}
