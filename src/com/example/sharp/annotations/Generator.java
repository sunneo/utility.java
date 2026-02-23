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
 * 
 * Note: This annotation is primarily for documentation purposes.
 * For generic methods, the returnType parameter may be omitted.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Generator {
    /**
     * Optional name for the generator
     */
    String name() default "";
    
    /**
     * Return type hint for the generator.
     * For generic methods, this may be omitted or set to Object.class.
     */
    Class<?> returnType() default Object.class;
}
