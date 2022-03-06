package com.example.sharp;

import com.example.events.WritableValue;

/**
 * an adapter to implement property in c# a property provides getter and setter
 * which can be overridden.
 *
 */
public class Property<T extends Object> extends WritableValue<T> implements IProperty<T> {

    @Override
    public T get() {
        return super.get();
    }

    @Override
    public void set(T value) {
        super.set(value);
    }

    public Property(T value) {
        super(value);
    }

    public Property() {

    }

}
