package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class Tuple3<A, B, C> extends Collectable {

    private A a;
    private B b;
    private C c;

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }

    public C getThird() {
        return c;
    }

    public Tuple3<A, B, C> setFirst(A a) {
        this.a = a;
        return this;
    }

    public Tuple3<A, B, C> setSecond(B b) {
        this.b = b;
        return this;
    }

    public Tuple3<A, B, C> setThird(C c) {
        this.c = c;
        return this;
    }

    @Override
    public Collectable initFromList(List values) {
        Assert.isTrue(values.size() > 1, "Values less then 3");
        this.a = (A) values.get(0);
        this.b = (B) values.get(1);
        this.c = (C) values.get(2);
        return this;
    }

}
