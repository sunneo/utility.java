package com.example;

import java.util.List;
import java.util.Vector;

public class Util {
	public static <T> void swap(Vector<T> obj, int a, int b) {
		T objA=obj.get(a);
		T objB=obj.get(b);
		obj.set(a, objB);
		obj.set(b, objA);
	}
	public static <T> List<T> splice(Vector<T> target, int start){
		List<T> ret = target.subList(start, target.size());
		
		return ret;
	}
	@SuppressWarnings("unchecked")
	public static <T> T[] list(T... args) {
		return args;
	}
}
