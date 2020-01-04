package com.example;

/**
 * locked variable which every set/get are protected.
 *
 * @param <T>
 */
public class Locked<T> {
	Object mLocker = new Object();
	T mValue;

	public T get() {
		synchronized (mLocker) {
			return mValue;
		}
	}

	public void set(T value) {
		synchronized (mLocker) {
			mValue = value;
		}
	}

	public Locked(T val) {
		mValue = val;
	}
}
