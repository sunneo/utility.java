package com.example.sharp.coroutine;

import com.example.events.Var;
import com.example.sharp.Delegates;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Builder class to help create generators from coroutines.
 * Provides a fluent API for building generators with yield support.
 */
public class GeneratorBuilder {
    
    /**
     * Functional interface for generator body that receives a YieldContext
     * This version expects the body to add instructions to the coroutine
     */
    @FunctionalInterface
    public interface GeneratorBody<T> {
        void build(CoroutineContext<T> context);
    }
    
    /**
     * Context object for building generator instructions
     */
    public static class CoroutineContext<T> {
        private Coroutine coroutine;
        
        CoroutineContext(Coroutine coroutine) {
            this.coroutine = coroutine;
        }
        
        /**
         * Add an instruction to yield a value
         */
        public void yield(T value) {
            coroutine.addInstruction((cor) -> cor.yield(value));
        }
        
        /**
         * Add an instruction to execute code
         */
        public void exec(Consumer<Coroutine> action) {
            coroutine.addInstruction((cor) -> action.accept(cor));
        }
        
        /**
         * Get the underlying coroutine for advanced use
         */
        public Coroutine getCoroutine() {
            return coroutine;
        }
        
        /**
         * Add a for loop
         */
        public Coroutine.CompositeBlock For(Delegates.Action1<Coroutine> init, 
                                             Delegates.Func<Boolean> cond, 
                                             Delegates.Action1<Coroutine> step) {
            return coroutine.For(init, cond, step);
        }
        
        /**
         * Add a while loop
         */
        public Coroutine.CompositeBlock While(Delegates.Func<Boolean> cond) {
            return coroutine.While(cond);
        }
        
        /**
         * Add a foreach loop
         */
        public <E> Coroutine.ForeachBody<E> Foreach(Iterator<E> iterator) {
            return coroutine.Foreach(iterator);
        }
        
        /**
         * Add a foreach loop
         */
        public <E> Coroutine.ForeachBody<E> Foreach(Iterable<E> iterable) {
            return coroutine.Foreach(iterable);
        }
        
        /**
         * Add an if block
         */
        public Coroutine.IfBlock If(Delegates.Func<Boolean> cond) {
            return coroutine.If(cond);
        }
    }
    
    /**
     * Create a generator from a generator body
     */
    public static <T> Delegates.IterableEx<T> fromBody(GeneratorBody<T> body) {
        Coroutine cor = new Coroutine();
        CoroutineContext<T> context = new CoroutineContext<>(cor);
        
        // Build the coroutine instructions
        body.build(context);
        
        cor.start();
        
        return cor.iterable();
    }
    
    /**
     * Create a generator from a coroutine
     */
    public static <T> Delegates.IterableEx<T> fromCoroutine(Coroutine cor) {
        return cor.iterable();
    }
    
    /**
     * Create an iterator from a generator body
     */
    public static <T> Iterator<T> iterator(GeneratorBody<T> body) {
        return fromBody(body).iterator();
    }
    
    /**
     * Helper method to create a coroutine-based generator with manual control
     */
    public static Coroutine createCoroutine(Delegates.Action1<Coroutine> setup) {
        Coroutine cor = new Coroutine(setup);
        cor.start();
        return cor;
    }
}
