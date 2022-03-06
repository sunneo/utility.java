package com.example.events;

import com.example.sharp.IGetter;
import com.example.sharp.ISetter;

public class WritableValue <T> implements IGetter<T>, ISetter<T>{
	protected T value;
	public static <T> WritableValue<T> create(T val){
		return new WritableValue<T>(val);
	}
	public WritableValue() {
		
	}
	public WritableValue(T value) {
		this.value = value;
	}
	
	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return this.value;
	}
	public void dispose() {
		this.value = null;
	}
}
