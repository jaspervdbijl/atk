package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class Six<A, B, C, D, E, F> extends Collectable {

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

    @Override
    public Collectable initFromList(List values) {
        Assert.isTrue(values.size() > 5, "Values less then 5");
        this.a = (A) values.get(0);
        this.b = (B) values.get(1);
        this.c = (C) values.get(2);
        this.d = (D) values.get(3);
        this.e = (E) values.get(4);
        this.f = (F) values.get(5);
        return this;
    }
}

