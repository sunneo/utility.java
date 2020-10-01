package com.example.sharp.reflection;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.example.sharp.Tracer;
public class MethodWrapper {
    public Object targetObject;
    public Method method;

    public MethodWrapper(Object targetObject, Method method) {
        this.targetObject = targetObject;
        this.method = method;
    }

    public Object invoke(Object... args) {
        try {
            return method.invoke(targetObject, args);
        } catch (Exception e) {
            Tracer.D(e);
        }
        return null;
    }

    public boolean isPublic() {
        return method.getModifiers() == Member.PUBLIC;
    }
}