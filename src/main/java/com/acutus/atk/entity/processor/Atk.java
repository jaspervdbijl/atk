package com.acutus.atk.entity.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Atk {

    public enum Match {
        PARTIAL,FULL
    }

    String className() default "";

    String classNameExt() default "Entity";

    Class daoClass() default Void.class;
    boolean daoCopyAll() default true;

    Atk.Match daoMatch() default Atk.Match.FULL;

    String [] daoIgnore() default {};



}
