package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class One<A> extends Collectable {

    private A a;

    public A getFirst() {
        return a;
    }

    @Override
    public One<A> initFromList(List values) {
        Assert.isTrue(!values.isEmpty(), "Empty list");
        this.a = (A) values.get(0);
        return this;
    }
}
