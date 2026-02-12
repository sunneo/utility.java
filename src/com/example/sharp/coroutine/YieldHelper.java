package com.example.sharp.coroutine;

import com.example.events.Var;
import com.example.sharp.Delegates;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to simplify generator syntax by collecting yield values.
 * Provides a more natural way to write generators similar to Python or C#.
 * 
 * Example usage:
 * 
 * public static Iterable<Integer> range(int start, int end) {
 *     return YieldHelper.generate((yield) -> {
 *         for (int i = start; i <= end; i++) {
 *             yield.accept(i);
 *         }
 *     });
 * }
 * 
 * This provides a simpler syntax compared to manually building coroutines.
 */
public class YieldHelper {
    
    /**
     * Functional interface for generator bodies that use yield
     */
    @FunctionalInterface
    public interface GeneratorAction<T> {
        void run(YieldCollector<T> yield) throws Exception;
    }
    
    /**
     * Collector for yield values with natural syntax
     */
    public static class YieldCollector<T> {
        private Coroutine coroutine;
        
        YieldCollector(Coroutine coroutine) {
            this.coroutine = coroutine;
        }
        
        /**
         * Yield a value - call this instead of coroutine.yield()
         * We need to capture the value immediately to avoid variable capture issues
         */
        public void accept(T value) {
            final T capturedValue = value;  // Ensure value is captured
            coroutine.addInstruction((cor) -> cor.yield(capturedValue));
        }
        
        /**
         * Yield return - alias for accept
         */
        public void yieldReturn(T value) {
            accept(value);
        }
        
        /**
         * Access to underlying coroutine for advanced operations
         */
        public Coroutine coroutine() {
            return coroutine;
        }
    }
    
    /**
     * Generate an iterable from a generator body with natural yield syntax.
     * This is the simplest way to create generators.
     * 
     * Example:
     * <pre>
     * Iterable<Integer> numbers = YieldHelper.generate((yield) -> {
     *     for (int i = 0; i < 10; i++) {
     *         yield.accept(i);
     *     }
     * });
     * </pre>
     * 
     * Note: The body runs immediately to build instructions.
     * Use variables carefully - they should be captured at the right time.
     */
    public static <T> Delegates.IterableEx<T> generate(GeneratorAction<T> action) {
        Coroutine cor = new Coroutine();
        YieldCollector<T> collector = new YieldCollector<>(cor);
        
        try {
            action.run(collector);
        } catch (Exception e) {
            cor.addInstruction((c) -> {
                throw new RuntimeException("Generator error", e);
            });
        }
        
        // Add a final no-op instruction to ensure last yield is properly returned
        // This works around the iterator implementation that pre-executes to next yield
        cor.addInstruction((c) -> {
            // No-op to ensure last yield value is returned
        });
        
        cor.start();
        return cor.iterable();
    }
    
    /**
     * Generate with automatic state management for loop variables.
     * Use this when you need proper coroutine loop control.
     * 
     * Example:
     * <pre>
     * Iterable<Integer> evens = YieldHelper.generateWithCoroutine((cor, yield) -> {
     *     Var<Integer> i = new Var<>(0);
     *     cor.For(
     *         (ctx) -> i.set(0),
     *         () -> i.get() < 100,
     *         (ctx) -> i.set(i.get() + 2)
     *     ).run((ctx) -> {
     *         yield.accept(i.get());
     *     });
     * });
     * </pre>
     */
    public static <T> Delegates.IterableEx<T> generateWithCoroutine(
            BiGeneratorAction<T> action) {
        Coroutine cor = new Coroutine();
        YieldCollectorAdvanced<T> collector = new YieldCollectorAdvanced<>(cor);
        
        try {
            action.run(cor, collector);
        } catch (Exception e) {
            cor.addInstruction((c) -> {
                throw new RuntimeException("Generator error", e);
            });
        }
        
        cor.start();
        return cor.iterable();
    }
    
    @FunctionalInterface
    public interface BiGeneratorAction<T> {
        void run(Coroutine coroutine, YieldCollectorAdvanced<T> yield) throws Exception;
    }
    
    /**
     * Advanced yield collector with direct coroutine access
     */
    public static class YieldCollectorAdvanced<T> extends YieldCollector<T> {
        YieldCollectorAdvanced(Coroutine coroutine) {
            super(coroutine);
        }
        
        /**
         * Yield in a coroutine instruction - use within For/While/Foreach bodies
         */
        public void yieldInInstruction(T value) {
            coroutine().yield(value);
        }
    }
}
