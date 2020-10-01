package com.example.sharp.reflection;

public class ReflectionHelper {
    @SuppressWarnings("rawtypes")
    public Class clz;
    public Object targetObject;
    public MethodCollector methods;
    public FieldCollector fields;

    public ReflectionHelper(Object targetObject) {
        this.targetObject = targetObject;
        this.clz = this.targetObject.getClass();
        this.methods = new MethodCollector(this.targetObject, clz);
        this.fields = new FieldCollector(this.targetObject, clz);
    }
    public ReflectionHelper(Class<?> clz) {
        this.targetObject = null;
        this.clz = clz;
        this.methods = new MethodCollector(this.targetObject, clz);
        this.fields = new FieldCollector(this.targetObject, clz);
    }
}
