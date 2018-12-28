package com.acutus.atk.util.collection;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Six<A, B, C, D, E, F> {

    private A a;
    private B b;
    private C c;
    private D d;
    private E e;
    private F f;

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }

    public C getThird() {
        return c;
    }

    public D getFourth() {
        return d;
    }

    public E getFifth() {
        return e;
    }

    public F getSixth() {
        return f;
    }

}

