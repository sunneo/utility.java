package com.example.sharp.coroutine.example;

import com.example.events.Var;
import com.example.sharp.Delegates;
import com.example.sharp.annotations.Generator;
import com.example.sharp.coroutine.YieldHelper;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Examples demonstrating simplified generator syntax using YieldHelper.
 * This provides a more natural way to write generators, closer to Python/C# syntax.
 */
public class SimplifiedGeneratorExample {

    /**
     * Simple range generator with natural yield syntax
     */
    @Generator(name = "range", returnType = Integer.class)
    public static Delegates.IterableEx<Integer> range(int start, int end) {
        return YieldHelper.generate((yield) -> {
            // Need to capture each value as final to avoid variable capture issues
            for (int i = start; i <= end; i++) {
                final int value = i;  // Capture as final
                yield.accept(value);
            }
        });
    }
    
    /**
     * Fibonacci sequence with simplified syntax
     */
    @Generator(returnType = BigInteger.class)
    public static Delegates.IterableEx<BigInteger> fibonacci(int count) {
        return YieldHelper.generate((yield) -> {
            BigInteger a = BigInteger.ZERO;
            BigInteger b = BigInteger.ONE;
            
            for (int i = 0; i < count; i++) {
                final BigInteger current = a;  // Capture as final
                yield.accept(current);
                
                BigInteger temp = a.add(b);
                a = b;
                b = temp;
            }
        });
    }
    
    /**
     * Filter elements with natural syntax
     */
    @Generator
    public static <T> Delegates.IterableEx<T> filter(Iterable<T> source, java.util.function.Predicate<T> predicate) {
        return YieldHelper.generate((yield) -> {
            for (T item : source) {
                if (predicate.test(item)) {
                    yield.accept(item);
                }
            }
        });
    }
    
    /**
     * Map elements with transformation
     */
    @Generator
    public static <T, R> Delegates.IterableEx<R> map(Iterable<T> source, java.util.function.Function<T, R> transform) {
        return YieldHelper.generate((yield) -> {
            for (T item : source) {
                yield.accept(transform.apply(item));
            }
        });
    }
    
    /**
     * Take first N elements
     */
    @Generator
    public static <T> Delegates.IterableEx<T> take(Iterable<T> source, int n) {
        return YieldHelper.generate((yield) -> {
            int count = 0;
            for (T item : source) {
                if (count >= n) break;
                yield.accept(item);
                count++;
            }
        });
    }
    
    /**
     * Repeat a value N times
     */
    @Generator
    public static <T> Delegates.IterableEx<T> repeat(T value, int times) {
        return YieldHelper.generate((yield) -> {
            for (int i = 0; i < times; i++) {
                yield.accept(value);
            }
        });
    }
    
    /**
     * Flatten nested iterables
     */
    @Generator
    public static <T> Delegates.IterableEx<T> flatten(Iterable<? extends Iterable<T>> nested) {
        return YieldHelper.generate((yield) -> {
            for (Iterable<T> inner : nested) {
                for (T item : inner) {
                    yield.accept(item);
                }
            }
        });
    }
    
    /**
     * Enumerate with index
     */
    @Generator
    public static <T> Delegates.IterableEx<IndexedValue<T>> enumerate(Iterable<T> source) {
        return YieldHelper.generate((yield) -> {
            int index = 0;
            for (T item : source) {
                yield.accept(new IndexedValue<>(index++, item));
            }
        });
    }
    
    /**
     * Zip two iterables
     */
    @Generator
    public static <T1, T2> Delegates.IterableEx<Pair<T1, T2>> zip(Iterable<T1> first, Iterable<T2> second) {
        return YieldHelper.generate((yield) -> {
            var iter1 = first.iterator();
            var iter2 = second.iterator();
            
            while (iter1.hasNext() && iter2.hasNext()) {
                yield.accept(new Pair<>(iter1.next(), iter2.next()));
            }
        });
    }
    
    /**
     * Example with coroutine control flow - for proper loop variables
     */
    @Generator(returnType = Integer.class)
    public static Delegates.IterableEx<Integer> rangeWithCoroutineLoop(int start, int end) {
        return YieldHelper.generateWithCoroutine((cor, yield) -> {
            Var<Integer> i = new Var<>(start);
            cor.For(
                (ctx) -> i.set(start),
                () -> i.get() <= end,
                (ctx) -> i.set(i.get() + 1)
            ).run((ctx) -> {
                yield.yieldInInstruction(i.get());
            });
        });
    }
    
    /**
     * Example: infinite sequence with coroutine
     */
    @Generator(returnType = Long.class)
    public static Delegates.IterableEx<Long> infiniteCounter(long start) {
        return YieldHelper.generateWithCoroutine((cor, yield) -> {
            Var<Long> count = new Var<>(start);
            cor.addInstruction("loop", (ctx) -> {
                yield.yieldInInstruction(count.get());
                count.set(count.get() + 1);
                ctx.jmp("loop");
            });
        });
    }
    
    // Helper classes
    public static class IndexedValue<T> {
        public final int index;
        public final T value;
        
        public IndexedValue(int index, T value) {
            this.index = index;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return "[" + index + "] = " + value;
        }
    }
    
    public static class Pair<T1, T2> {
        public final T1 first;
        public final T2 second;
        
        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
        
        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }
    
    /**
     * Main method demonstrating all simplified generators
     */
    public static void main(String[] args) {
        System.out.println("=== Range (1-10) ===");
        for (Integer i : range(1, 10)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Fibonacci (10 numbers) ===");
        for (BigInteger fib : fibonacci(10)) {
            System.out.print(fib + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Filter (even numbers from 1-20) ===");
        for (Integer i : filter(range(1, 20), x -> x % 2 == 0)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Map (squares of 1-10) ===");
        for (Integer square : map(range(1, 10), x -> x * x)) {
            System.out.print(square + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Take (first 5 from 1-100) ===");
        for (Integer i : take(range(1, 100), 5)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Repeat ('Hello' 5 times) ===");
        for (String s : repeat("Hello", 5)) {
            System.out.print(s + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Flatten ===");
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );
        for (Integer i : flatten(nested)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Enumerate ===");
        for (IndexedValue<String> item : enumerate(Arrays.asList("a", "b", "c", "d"))) {
            System.out.println(item);
        }
        System.out.println();
        
        System.out.println("=== Zip ===");
        for (Pair<Integer, String> pair : zip(range(1, 5), Arrays.asList("a", "b", "c", "d", "e"))) {
            System.out.println(pair);
        }
        System.out.println();
        
        System.out.println("=== Range with Coroutine Loop ===");
        for (Integer i : rangeWithCoroutineLoop(1, 10)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Infinite Counter (first 10) ===");
        int count = 0;
        for (Long num : infiniteCounter(100)) {
            System.out.print(num + " ");
            if (++count >= 10) break;
        }
        System.out.println();
    }
}
