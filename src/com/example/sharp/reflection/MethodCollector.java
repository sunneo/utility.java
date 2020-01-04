package com.example.sharp.reflection;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A collector which contains all methods within a class
 *
 */
public class MethodCollector {
    public Hashtable<String, Vector<MethodWrapper>> methods = new Hashtable<>();

    private void Collect(Object targetObject, @SuppressWarnings("rawtypes") Class clz) {
        if(clz == null){
            return;
        }
        for (Method m : clz.getDeclaredMethods()) {
            String name = m.getName();
            Vector<MethodWrapper> wrappers = null;
            if (methods.containsKey(name)) {
                wrappers = methods.get(name);
            } else {
                wrappers = new Vector<>();
                methods.put(name, wrappers);
            }
            wrappers.add(new MethodWrapper(targetObject, m));
        }
        for (Method m : clz.getMethods()) {
            String name = m.getName();
            Vector<MethodWrapper> wrappers = null;
            if (methods.containsKey(name)) {
                wrappers = methods.get(name);
            } else {
                wrappers = new Vector<>();
                methods.put(name, wrappers);
            }
            wrappers.add(new MethodWrapper(targetObject, m));
        }
        if(clz.getSuperclass()!=null) {
            Collect(targetObject,clz.getSuperclass());
        }
    }
    public MethodCollector(Object targetObject, @SuppressWarnings("rawtypes") Class clz) {
       Collect(targetObject,clz);
    }

    /**
     * get count of methods with the same name.
     * 
     * @param name name of method
     * @return count of overloads
     */
    public int getMethodCount(String name) {
        if (!methods.containsKey(name))
            return 0;
        return methods.get(name).size();
    }

    public Object invoke(String name, int index, Object... args) {
        if (!methods.containsKey(name))
            return null;
        methods.get(name).get(index).method.setAccessible(true);
        return methods.get(name).get(index).invoke(args);
    }

    public Object invoke(String name, Object... args) {
        return this.invoke(name, 0, args);
    }
}