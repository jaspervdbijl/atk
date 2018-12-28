package com.acutus.atk.util.call;

public interface CallThreeRet<A, B, C, R> {
    public R call(A a, B b, C c) throws Exception;
}