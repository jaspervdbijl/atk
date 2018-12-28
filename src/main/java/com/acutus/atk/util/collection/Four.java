package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Four<A, B, C, D> extends Collectable {

    private A a;
    private B b;
    private C c;
    private D d;

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

    @Override
    public Collectable initFromList(List values) {
        Assert.isTrue(values.size() > 3, "Values less then 4");
        this.a = (A) values.get(0);
        this.b = (B) values.get(1);
        this.c = (C) values.get(2);
        this.d = (D) values.get(3);
        return this;
    }

}
