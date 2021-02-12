package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class Seven<A, B, C, D, E, F, G> extends Collectable {

    private A a;
    private B b;
    private C c;
    private D d;
    private E e;
    private F f;
    private G g;

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

    public G getSeventh() {
        return g;
    }

    @Override
    public Collectable initFromList(List values) {
        Assert.isTrue(values.size() > 6, "Values less then 6");
        this.a = (A) values.get(0);
        this.b = (B) values.get(1);
        this.c = (C) values.get(2);
        this.d = (D) values.get(3);
        this.e = (E) values.get(4);
        this.f = (F) values.get(5);
        this.g = (G) values.get(6);
        return this;
    }
}

