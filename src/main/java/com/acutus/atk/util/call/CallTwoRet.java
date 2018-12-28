package com.acutus.atk.util.call;

public interface CallTwoRet<A, B, R> {
    public R call(A a, B b) throws Exception;
}