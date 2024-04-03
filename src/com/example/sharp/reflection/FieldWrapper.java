package com.example.sharp.reflection;

import com.example.sharp.Tracer;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

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
        return Modifier.isPublic(field.getModifiers());
    }

    public boolean isTransient() {
        return Modifier.isTransient(field.getModifiers());
    }

    public boolean isPrimitive() {
        return field!=null && field.getType().isPrimitive();
    }
    public boolean isBoolean() {
        return field!=null && field.getType().equals(Boolean.class);
    }
    public boolean isInteger() {
        return field!=null && field.getType().equals(Integer.class);
    }
    public boolean isDouble() {
        return field!=null && field.getType().equals(Double.class);
    }
    public boolean isString() {
        return field!=null && field.getType().equals(String.class);
    }
    public boolean isFloat() {
        return field!=null && field.getType().equals(Float.class);
    }

    public boolean isShort() {
        return field!=null && field.getType().equals(Short.class);
    }

    public boolean isEnum() {
        return field!=null && field.getType().isEnum();
    }
}
