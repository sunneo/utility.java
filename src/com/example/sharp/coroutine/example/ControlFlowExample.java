package com.example.sharp.coroutine.example;

import com.example.events.Var;
import com.example.sharp.Delegates;
import com.example.sharp.annotations.Generator;
import com.example.sharp.coroutine.Coroutine;

/**
 * Example demonstrating enhanced control flow in coroutines:
 * - If-Else blocks
 * - Break statements
 * - Continue statements
 * - Nested control structures
 */
public class ControlFlowExample {

    /**
     * Generator that yields only even numbers from 0 to max
     * Demonstrates continue statement
     */
    @Generator(returnType = Integer.class)
    public static Delegates.IterableEx<Integer> evenNumbersGenerator(int max) {
        Coroutine cor = new Coroutine();
        Var<Integer> i = new Var<>(0);
        
        cor.For(
            (ctx) -> i.set(0),
            () -> i.get() <= max,
            (ctx) -> i.set(i.get() + 1)
        ).run((ctx) -> {
            // If odd, continue to next iteration
            if (i.get() % 2 != 0) {
                ctx.doContinue();
                return;
            }
            ctx.yield(i.get());
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Generator that yields numbers until finding first multiple of target
     * Demonstrates break statement
     */
    @Generator(returnType = Integer.class)
    public static Delegates.IterableEx<Integer> numbersUntilMultiple(int max, int target) {
        Coroutine cor = new Coroutine();
        Var<Integer> i = new Var<>(1);
        
        cor.For(
            (ctx) -> i.set(1),
            () -> i.get() <= max,
            (ctx) -> i.set(i.get() + 1)
        ).run((ctx) -> {
            ctx.yield(i.get());
            
            // Break if we found a multiple
            if (i.get() % target == 0 && i.get() > 0) {
                ctx.doBreak();
            }
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Generator demonstrating if-else control flow
     * Categorizes numbers as small/medium/large
     */
    @Generator(returnType = String.class)
    public static Delegates.IterableEx<String> categorizeNumbers(int count) {
        Coroutine cor = new Coroutine();
        Var<Integer> i = new Var<>(0);
        
        cor.For(
            (ctx) -> i.set(0),
            () -> i.get() < count,
            (ctx) -> i.set(i.get() + 1)
        ).run((ctx) -> {
            int num = i.get();
            Var<String> category = new Var<>("");
            
            // Nested if-else using coroutine control flow
            ctx.If(() -> num < 10)
                .then((ifCtx) -> {
                    category.set("small");
                })
                .Else((elseCtx) -> {
                    elseCtx.If(() -> num < 50)
                        .then((ifCtx2) -> {
                            category.set("medium");
                        })
                        .Else((elseCtx2) -> {
                            category.set("large");
                        });
                });
            
            ctx.yield(String.format("%d: %s", num, category.get()));
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * FizzBuzz generator using if-else control flow
     */
    @Generator(returnType = String.class)
    public static Delegates.IterableEx<String> fizzBuzzGenerator(int max) {
        Coroutine cor = new Coroutine();
        Var<Integer> i = new Var<>(1);
        
        cor.For(
            (ctx) -> i.set(1),
            () -> i.get() <= max,
            (ctx) -> i.set(i.get() + 1)
        ).run((ctx) -> {
            int num = i.get();
            Var<String> result = new Var<>("");
            
            ctx.If(() -> num % 15 == 0)
                .then((ifCtx) -> {
                    result.set("FizzBuzz");
                })
                .Else((elseCtx) -> {
                    elseCtx.If(() -> num % 3 == 0)
                        .then((ifCtx2) -> {
                            result.set("Fizz");
                        })
                        .Else((elseCtx2) -> {
                            elseCtx2.If(() -> num % 5 == 0)
                                .then((ifCtx3) -> {
                                    result.set("Buzz");
                                })
                                .Else((elseCtx3) -> {
                                    result.set(String.valueOf(num));
                                });
                        });
                });
            
            ctx.yield(result.get());
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Prime number generator with optimized sieve approach
     * Demonstrates complex control flow with break and continue
     */
    @Generator(returnType = Integer.class)
    public static Delegates.IterableEx<Integer> primesGenerator(int max) {
        Coroutine cor = new Coroutine();
        
        cor.addInstruction((ctx) -> {
            ctx.yield(2); // First prime
        });
        
        Var<Integer> candidate = new Var<>(3);
        
        cor.While(() -> candidate.get() <= max).run((ctx) -> {
            int num = candidate.get();
            Var<Boolean> isPrime = new Var<>(true);
            Var<Integer> divisor = new Var<>(2);
            int sqrtNum = (int) Math.sqrt(num);
            
            // Check if prime
            ctx.While(() -> divisor.get() <= sqrtNum).run((whileCtx) -> {
                if (num % divisor.get() == 0) {
                    isPrime.set(false);
                    whileCtx.doBreak();
                    return;
                }
                divisor.set(divisor.get() + 1);
            });
            
            // Yield if prime
            ctx.If(() -> isPrime.get())
                .then((ifCtx) -> {
                    ifCtx.yield(num);
                });
            
            candidate.set(candidate.get() + 2); // Skip even numbers
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Collatz sequence generator
     * Demonstrates complex control flow with conditionals
     */
    @Generator(returnType = Long.class)
    public static Delegates.IterableEx<Long> collatzGenerator(long start) {
        Coroutine cor = new Coroutine();
        Var<Long> n = new Var<>(start);
        
        cor.While(() -> n.get() != 1).run((ctx) -> {
            ctx.yield(n.get());
            
            long current = n.get();
            ctx.If(() -> current % 2 == 0)
                .then((ifCtx) -> {
                    n.set(current / 2);
                })
                .Else((elseCtx) -> {
                    n.set(3 * current + 1);
                });
        });
        
        // Yield final 1
        cor.addInstruction((ctx) -> ctx.yield(1L));
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Main method demonstrating all control flow examples
     */
    public static void main(String[] args) {
        System.out.println("=== Even Numbers (0-20) ===");
        for (Integer num : evenNumbersGenerator(20)) {
            System.out.print(num + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Numbers Until Multiple of 7 ===");
        for (Integer num : numbersUntilMultiple(50, 7)) {
            System.out.print(num + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Categorize Numbers (0-99) ===");
        int count = 0;
        for (String category : categorizeNumbers(100)) {
            System.out.println(category);
            if (++count >= 15) {
                System.out.println("... (showing first 15)");
                break;
            }
        }
        System.out.println();
        
        System.out.println("=== FizzBuzz (1-30) ===");
        count = 0;
        for (String result : fizzBuzzGenerator(30)) {
            System.out.printf("%s ", result);
            if (++count % 10 == 0) System.out.println();
        }
        System.out.println("\n");
        
        System.out.println("=== Prime Numbers (up to 100) ===");
        for (Integer prime : primesGenerator(100)) {
            System.out.print(prime + " ");
        }
        System.out.println("\n");
        
        System.out.println("=== Collatz Sequence (starting from 27) ===");
        for (Long num : collatzGenerator(27)) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
