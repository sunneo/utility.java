package com.example.sharp.coroutine.example;

import com.example.events.Var;
import com.example.sharp.Delegates;
import com.example.sharp.annotations.Generator;
import com.example.sharp.coroutine.Coroutine;
import com.example.sharp.coroutine.GeneratorBuilder;

import java.util.Random;

/**
 * Example demonstrating the @Generator annotation pattern.
 * Shows how methods can be marked with @Generator and easily converted to iterators.
 */
public class GeneratorAnnotationExample {

    /**
     * Simple range generator - yields numbers from start to end
     */
    @Generator(name = "range", returnType = Integer.class)
    public static Delegates.IterableEx<Integer> range(int start, int end) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Var<Integer> i = new Var<>(start);
            ctx.For(
                (cor) -> i.set(start),
                () -> i.get() <= end,
                (cor) -> i.set(i.get() + 1)
            ).run((cor) -> {
                cor.yield(i.get());
            });
        });
    }
    
    /**
     * Range with step generator
     */
    @Generator(name = "rangeStep", returnType = Integer.class)
    public static Delegates.IterableEx<Integer> rangeWithStep(int start, int end, int step) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Var<Integer> i = new Var<>(start);
            if (step > 0) {
                ctx.For(
                    (cor) -> i.set(start),
                    () -> i.get() <= end,
                    (cor) -> i.set(i.get() + step)
                ).run((cor) -> {
                    cor.yield(i.get());
                });
            } else if (step < 0) {
                ctx.For(
                    (cor) -> i.set(start),
                    () -> i.get() >= end,
                    (cor) -> i.set(i.get() + step)
                ).run((cor) -> {
                    cor.yield(i.get());
                });
            }
        });
    }
    
    /**
     * Infinite counter generator
     */
    @Generator(name = "counter", returnType = Long.class)
    public static Delegates.IterableEx<Long> infiniteCounter(long start) {
        Coroutine cor = new Coroutine();
        Var<Long> count = new Var<>(start);
        
        cor.addInstruction("loop", (ctx) -> {
            ctx.yield(count.get());
            count.set(count.get() + 1);
            ctx.jmp("loop");
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Repeat value generator
     */
    @Generator(returnType = Object.class)
    public static <T> Delegates.IterableEx<T> repeat(T value, int times) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Var<Integer> i = new Var<>(0);
            ctx.For(
                (cor) -> i.set(0),
                () -> i.get() < times,
                (cor) -> i.set(i.get() + 1)
            ).run((cor) -> {
                cor.yield(value);
            });
        });
    }
    
    /**
     * Cycle through array elements infinitely
     */
    @Generator
    public static <T> Delegates.IterableEx<T> cycle(T[] elements) {
        if (elements.length == 0) {
            Coroutine cor = new Coroutine();
            cor.start();
            return cor.iterable();
        }
        
        Coroutine cor = new Coroutine();
        int startPos = cor.addInstruction((ctx) -> {}); // marker
        
        // Add yield instruction for each element
        for (T element : elements) {
            cor.addInstruction((ctx) -> ctx.yield(element));
        }
        
        // Jump back to start
        final int finalStart = startPos;
        cor.addInstruction((ctx) -> ctx.jmp(finalStart));
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Take first n elements from another iterable
     */
    @Generator
    public static <T> Delegates.IterableEx<T> take(Iterable<T> source, int n) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Var<Integer> count = new Var<>(0);
            ctx.Foreach(source).run((cor, item) -> {
                if (count.get() >= n) {
                    cor.doBreak();
                    return;
                }
                cor.yield(item);
                count.set(count.get() + 1);
            });
        });
    }
    
    /**
     * Filter elements based on predicate
     */
    @Generator
    public static <T> Delegates.IterableEx<T> filter(Iterable<T> source, Delegates.Func1<T, Boolean> predicate) {
        return GeneratorBuilder.fromBody((ctx) -> {
            ctx.Foreach(source).run((cor, item) -> {
                if (predicate.Invoke(item)) {
                    cor.yield(item);
                }
            });
        });
    }
    
    /**
     * Map elements using transformation function
     */
    @Generator
    public static <T, R> Delegates.IterableEx<R> map(Iterable<T> source, Delegates.Func1<T, R> transform) {
        return GeneratorBuilder.fromBody((ctx) -> {
            ctx.Foreach(source).run((cor, item) -> {
                cor.yield(transform.Invoke(item));
            });
        });
    }
    
    /**
     * Zip two iterables together
     */
    @Generator
    public static <T1, T2> Delegates.IterableEx<Pair<T1, T2>> zip(Iterable<T1> first, Iterable<T2> second) {
        Coroutine cor = new Coroutine();
        var iter1 = first.iterator();
        var iter2 = second.iterator();
        
        cor.While(() -> iter1.hasNext() && iter2.hasNext()).run((ctx) -> {
            ctx.yield(new Pair<>(iter1.next(), iter2.next()));
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Generate random numbers
     */
    @Generator(returnType = Integer.class)
    public static Delegates.IterableEx<Integer> randomNumbers(int count, int min, int max) {
        return GeneratorBuilder.fromBody((ctx) -> {
            Random random = new Random();
            Var<Integer> i = new Var<>(0);
            
            ctx.For(
                (cor) -> i.set(0),
                () -> i.get() < count,
                (cor) -> i.set(i.get() + 1)
            ).run((cor) -> {
                cor.yield(random.nextInt(max - min + 1) + min);
            });
        });
    }
    
    /**
     * Flatten nested iterables
     */
    @Generator
    public static <T> Delegates.IterableEx<T> flatten(Iterable<Iterable<T>> nested) {
        return GeneratorBuilder.fromBody((ctx) -> {
            ctx.Foreach(nested).run((cor, inner) -> {
                Coroutine innerCor = cor.push();
                innerCor.Foreach(inner).run((cor2, item) -> {
                    cor2.yield(item);
                });
            });
        });
    }
    
    /**
     * Generate sliding window over elements
     */
    @Generator
    public static Delegates.IterableEx<int[]> slidingWindow(int[] elements, int windowSize) {
        return GeneratorBuilder.fromBody((ctx) -> {
            if (windowSize <= 0 || windowSize > elements.length) {
                return;
            }
            
            Var<Integer> i = new Var<>(0);
            ctx.For(
                (cor) -> i.set(0),
                () -> i.get() <= elements.length - windowSize,
                (cor) -> i.set(i.get() + 1)
            ).run((cor) -> {
                int[] window = new int[windowSize];
                System.arraycopy(elements, i.get(), window, 0, windowSize);
                cor.yield(window);
            });
        });
    }
    
    /**
     * Simple pair class for zip
     */
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
     * Main method demonstrating all generator patterns
     */
    public static void main(String[] args) {
        System.out.println("=== Range Generator ===");
        for (Integer i : range(1, 10)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Range with Step ===");
        for (Integer i : rangeWithStep(0, 20, 3)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Infinite Counter (first 15) ===");
        int count = 0;
        for (Long num : infiniteCounter(100)) {
            System.out.print(num + " ");
            if (++count >= 15) break;
        }
        System.out.println("\n");
        
        System.out.println("=== Repeat ===");
        for (String s : repeat("Hello", 5)) {
            System.out.print(s + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Cycle (first 15) ===");
        String[] colors = {"Red", "Green", "Blue"};
        count = 0;
        for (String color : cycle(colors)) {
            System.out.print(color + " ");
            if (++count >= 15) break;
        }
        System.out.println("\n");
        
        System.out.println("=== Take ===");
        for (Integer i : take(range(1, 100), 5)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Filter (even numbers) ===");
        for (Integer i : filter(range(1, 20), x -> x % 2 == 0)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Map (square numbers) ===");
        for (Integer i : map(range(1, 10), x -> x * x)) {
            System.out.print(i + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Zip ===");
        for (Pair<Integer, String> pair : zip(range(1, 5), repeat("item", 5))) {
            System.out.print(pair + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Random Numbers ===");
        for (Integer num : randomNumbers(10, 1, 100)) {
            System.out.print(num + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Sliding Window ===");
        int[] nums = {1, 2, 3, 4, 5, 6};
        for (int[] window : slidingWindow(nums, 3)) {
            System.out.print("[ ");
            for (int n : window) {
                System.out.print(n + " ");
            }
            System.out.print("] ");
        }
        System.out.println();
    }
}
