package com.example.sharp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a generator.
 * Methods annotated with @Generator can use yield operations
 * and will be automatically converted to iterators.
 * 
 * The method should return a Coroutine or implement a generator body
 * that can be converted to an Iterator/Iterable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Generator {
    /**
     * Optional name for the generator
     */
    String name() default "";
    
    /**
     * Return type hint for the generator
     */
    Class<?> returnType() default Object.class;
}
