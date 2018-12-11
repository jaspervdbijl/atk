package com.acutus.atk.util;

import lombok.AllArgsConstructor;

public class Call{

    public static interface One<T> {
        public T call();
    }

    public static interface OneEx<T> {
        public T call() throws Exception;
    }

    public static interface Two<T,R> {
        public R call(T t);
    }

    public static interface Three<A,B,R> {
        public R call(A a,B b);
    }

}
