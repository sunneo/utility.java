package com.example.sharp.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

import com.example.sharp.Tracer;

public class FieldWrapper {
    public Object targetObject;
    public Field field;

    public FieldWrapper(Object targetObject, Field field) {
        this.targetObject = targetObject;
        this.field = field;
    }

    public Object get() {
        try {
            return field.get(this.targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return null;
    }

    public void set(Object value) {
        try {
            field.set(targetObject, value);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
    }

    public double getDouble() {
        try {
            return field.getDouble(targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return 0.0;
    }

    public float getFloat() {
        try {
            return field.getFloat(targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return 0.0f;
    }

    public int getInt() {
        try {
            return field.getInt(targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return 0;
    }

    public short getShort() {
        try {
            return field.getShort(targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return 0;
    }

    public long getLong() {
        try {
            return field.getLong(targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return 0L;
    }

    public char getChar() {
        try {
            return field.getChar(targetObject);
        } catch (IllegalAccessException e) {
            Tracer.D(e);
        }
        return (char) 0;
    }

    public boolean isPublic() {
        return field.getModifiers() == Member.PUBLIC;
    }
}
