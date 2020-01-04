package com.example.sharp.reflection;

import java.lang.reflect.Field;
import java.util.Hashtable;

/**
 * collection to maintains all fields within a class
 *
 */
public class FieldCollector {
    public Hashtable<String, FieldWrapper> fields = new Hashtable<>();

    private void Collect(Object targetObject, @SuppressWarnings("rawtypes") Class clz) {
        if(clz == null) {
            return;
        }
        for (Field f : clz.getDeclaredFields()) {
            String name = f.getName();
            if (!fields.containsKey(name))
                fields.put(name, new FieldWrapper(targetObject, f));
        }
        for (Field f : clz.getFields()) {
            String name = f.getName();
            if (!fields.containsKey(name))
                fields.put(name, new FieldWrapper(targetObject, f));
        }
        if(clz.getSuperclass() != null) {
            Collect(targetObject,clz.getSuperclass());
        }
    }
    public FieldCollector(Object targetObject, @SuppressWarnings("rawtypes") Class clz) {
        Collect(targetObject,clz);
    }

    /**
     * test if there a field with given name existed.
     * 
     * @param name
     * @return
     */
    public boolean containsField(String name) {
        return fields.containsKey(name);
    }

    /**
     * get value of field by name.
     * 
     * @param name name of field.
     * @return value of field.
     */
    public Object get(String name) {
        if (!fields.containsKey(name))
            return null;
        fields.get(name).field.setAccessible(true);
        return fields.get(name).get();
    }

    /**
     * get double value (primitive type) of field.
     * 
     * @param name name of field
     * @return value of field
     */
    public double getDouble(String name) {
        if (!fields.containsKey(name))
            return 0.0;
        return fields.get(name).getDouble();
    }

    /**
     * get float value (primitive type) of field.
     * 
     * @param name name of field
     * @return value of field
     */
    public float getFloat(String name) {
        if (!fields.containsKey(name))
            return 0.0f;
        return fields.get(name).getFloat();
    }

    /**
     * get int value (primitive type) of field.
     * 
     * @param name name of field
     * @return value of field
     */
    public int getInt(String name) {
        if (!fields.containsKey(name))
            return 0;
        return fields.get(name).getInt();
    }

    /**
     * get short value (primitive type) of field.
     * 
     * @param name name of field
     * @return value of field
     */
    public short getShort(String name) {
        if (!fields.containsKey(name))
            return 0;
        return fields.get(name).getShort();
    }

    /**
     * get long value (primitive type) of field.
     * 
     * @param name name of field
     * @return value of field
     */
    public long getLong(String name) {
        if (!fields.containsKey(name))
            return 0L;
        return fields.get(name).getLong();
    }

    /**
     * get char value (primitive type) of field.
     * 
     * @param name name of field
     * @return value of field
     */
    public char getChar(String name) {
        if (!fields.containsKey(name))
            return (char) 0;
        return fields.get(name).getChar();
    }

    /**
     * test whether modifier of field is public
     * 
     * @param name name of field
     * @return true if public
     */
    public boolean isPublic(String name) {
        if (!fields.containsKey(name))
            return false;
        return fields.get(name).isPublic();
    }

    /**
     * set arbitrary value to given field name.
     * 
     * @param name  name of field
     * @param value value
     */
    public void set(String name, Object value) {
        if (!fields.containsKey(name))
            return;
        fields.get(name).set(value);
    }

}
