package com.example;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

public class ReflectionHelper{
    public static class FieldWrapper{
        public Object targetObject;
        public Field field;
        public FieldWrapper(Object targetObject, Field field){
            this.targetObject = targetObject;
            this.field = field;
        }
        public Object get(){
            try {
                return field.get(this.targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
        public void set(Object value){
            try {
                field.set(targetObject,value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        public double getDouble(){
            try {
                return field.getDouble(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0.0;
        }
        public float getFloat(){
            try {
                return field.getFloat(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0.0f;
        }
        public int getInt(){
            try {
                return field.getInt(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0;
        }
        public short getShort(){
            try {
                return field.getShort(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0;
        }
        public long getLong(){
            try {
                return field.getLong(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0L;
        }
        public char getChar(){
            try {
                return field.getChar(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return (char)0;
        }
        public boolean isPublic(){
            return field.getModifiers() == Field.PUBLIC;
        }
    }
    public static class MethodWrapper{
        public Object targetObject;
        public Method method;
        public MethodWrapper(Object targetObject, Method method){
            this.targetObject = targetObject;
            this.method = method;
        }
        public Object invoke(Object... args){
            try {
                return method.invoke(targetObject,args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
        public boolean isPublic(){
            return method.getModifiers() == Field.PUBLIC;
        }
    }
    public static class MethodCollector{
        public Hashtable<String,Vector<MethodWrapper>> methods = new Hashtable<>();
        public MethodCollector(Object targetObject,Class clz){
            for(Method m : clz.getDeclaredMethods()){
                String name = m.getName();
                Vector<MethodWrapper> wrappers = null;
                if(methods.containsKey(name)){
                    wrappers = methods.get(name);
                }
                else{
                    wrappers = new Vector<>();
                    methods.put(name,wrappers);
                }
                wrappers.add(new MethodWrapper(targetObject,m));
            }
            for(Method m : clz.getMethods()){
                String name = m.getName();
                Vector<MethodWrapper> wrappers = null;
                if(methods.containsKey(name)){
                    wrappers = methods.get(name);
                }
                else{
                    wrappers = new Vector<>();
                    methods.put(name,wrappers);
                }
                wrappers.add(new MethodWrapper(targetObject,m));
            }
        }
        public int getMethodCount(String name){
            if(!methods.containsKey(name)) return 0;
            return methods.get(name).size();
        }
        public Object invoke(String name,int index,Object... args){
            if(!methods.containsKey(name)) return null;
            return methods.get(name).get(index).invoke(args);
        }
        public Object invoke(String name,Object... args){
            if(!methods.containsKey(name)) return null;
            return methods.get(name).get(0).invoke(args);
        }
    }
    public static class FieldCollector{
        public Hashtable<String,FieldWrapper> fields = new Hashtable<>();
        public FieldCollector(Object targetObject,Class clz){
            for(Field f : clz.getDeclaredFields()){
                String name = f.getName();
                if(!fields.containsKey(name))
                    fields.put(name,new FieldWrapper(targetObject,f));
            }
            for(Field f : clz.getFields()){
                String name = f.getName();
                if(!fields.containsKey(name))
                    fields.put(name,new FieldWrapper(targetObject,f));
            }
        }
        public boolean containsField(String name){
            return fields.containsKey(name);
        }
        public Object get(String name){
            if(!fields.containsKey(name)) return null;
            return fields.get(name).get();
        }
        public double getDouble(String name){
            if(!fields.containsKey(name)) return 0.0;
            return fields.get(name).getDouble();
        }
        public float getFloat(String name){
            if(!fields.containsKey(name)) return 0.0f;
            return fields.get(name).getFloat();
        }
        public int getInt(String name){
            if(!fields.containsKey(name)) return 0;
            return fields.get(name).getInt();
        }
        public short getShort(String name){
            if(!fields.containsKey(name)) return 0;
            return fields.get(name).getShort();
        }
        public long getLong(String name){
            if(!fields.containsKey(name)) return 0L;
            return fields.get(name).getLong();
        }
        public char getChar(String name){
            if(!fields.containsKey(name)) return (char)0;
            return fields.get(name).getChar();
        }
        public boolean isPublic(String name){
            if(!fields.containsKey(name)) return false;
            return fields.get(name).isPublic();
        }

        public void set(String name,Object value){
            if(!fields.containsKey(name)) return;
            fields.get(name).set(value);
        }
    }
    public Class clz;
    public Object targetObject;
    public MethodCollector methods;
    public FieldCollector fields;
    public ReflectionHelper(Object targetObject){
        this.targetObject = targetObject;
        this.clz = this.targetObject.getClass();
        this.methods = new MethodCollector(this.targetObject,clz);
        this.fields = new FieldCollector(this.targetObject,clz);
    }
}