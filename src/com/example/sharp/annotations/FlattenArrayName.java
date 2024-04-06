package com.example.sharp.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FlattenArrayName {
    String name();

    String replacement() default "$INDEX$";
}
