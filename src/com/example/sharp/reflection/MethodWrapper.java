package com.example.sharp.reflection;

import com.example.sharp.Tracer;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
        return Modifier.isPublic(method.getModifiers());
    }
}