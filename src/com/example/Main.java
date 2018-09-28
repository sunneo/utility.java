package com.example;

public class Main {
    public static class ExampleClass{
        public Integer a;
        public Double b;
        public static class Inner{
            public int a2;
            public double b2;
        }
        Inner inner = new Inner();
    }
    public static void main(String[] args) {
	// write your code here
        ExampleClass example = new ExampleClass();
        ReflectionHelper reflectionHelper = new ReflectionHelper(example);
    }
}
