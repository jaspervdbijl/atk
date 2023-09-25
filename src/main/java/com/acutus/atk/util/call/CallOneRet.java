package com.acutus.atk.util.call;

public interface CallOneRet<A, R> {
    public R call(A a) throws Exception;
}
