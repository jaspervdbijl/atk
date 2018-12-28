package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Five<A, B, C, D, E> extends Collectable {

    private A a;
    private B b;
    private C c;
    private D d;
    private E e;

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

    @Override
    public Collectable initFromList(List values) {
        Assert.isTrue(values.size() > 3, "Values less then 5");
        this.a = (A) values.get(0);
        this.b = (B) values.get(1);
        this.c = (C) values.get(2);
        this.d = (D) values.get(3);
        this.e = (E) values.get(4);
        return this;
    }


}
