package com.acutus.atk.util;

public class Call{


    public static interface Zero {
        public void call() throws Exception;
    }

    public static interface ZeroRet<T> {
        public T call() throws Exception;
    }

    public static interface One<A> {
        public void call(A a) throws Exception;
    }

    public static interface OneRet<A, R> {
        public R call(A a) throws Exception;
    }

    public static interface Two<A, B> {
        public void call(A a, B b) throws Exception;
    }

    public static interface TwoRet<A, B, R> {
        public R call(A a, B b) throws Exception;
    }

    public static interface Three<A, B, C> {
        public void call(A a, B b, C c) throws Exception;
    }

    public static interface ThreeRet<A, B, C, R> {
        public R call(A a, B b, C c) throws Exception;
    }
}
