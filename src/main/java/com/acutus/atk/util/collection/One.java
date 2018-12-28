package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;

import java.util.List;

public class One<A> extends Collectable {

    private A a;
    private List values;

    public One(A a) {
        this.a = a;
    }

    public A getFirst() {
        return a;
    }

    @Override
    public One<A> initFromList(List values) {
        Assert.isTrue(values.isEmpty(), "Empty list");
        this.a = (A) values.get(0);
        return this;
    }
}
