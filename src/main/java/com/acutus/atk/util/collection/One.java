package com.acutus.atk.util.collection;

import com.acutus.atk.util.Assert;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class One<A> extends Collectable {

    @Setter
    private A a;

    public A getFirst() {
        return a;
    }

    public boolean isPresent() {
        return a != null;
    }

    @Override
    public One<A> initFromList(List values) {
        Assert.isTrue(!values.isEmpty(), "Empty list");
        this.a = (A) values.get(0);
        return this;
    }
}
