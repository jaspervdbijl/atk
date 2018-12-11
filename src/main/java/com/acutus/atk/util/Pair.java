package com.acutus.atk.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Pair<T,R> {

    private T t;
    private R r;

    public T getFirst() {return t;}

    public R getSecond() {return r;}
}
