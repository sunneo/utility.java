package com.example.events;

/**
 * a value changed event argument for Var<T>.valueChanged Event.
 *
 * @param <T> data type.
 */
public class ValueChangedEventArgs<T extends Object> extends INotificationEventArgs{
	@SuppressWarnings("unchecked")
	public T getValue() {
		return (T)this.object[0]; 
	}
	public ValueChangedEventArgs(T value) {
		this.object = new Object[] {value};
		
	}
}
