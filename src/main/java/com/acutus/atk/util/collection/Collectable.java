package com.acutus.atk.util.collection;

import com.acutus.atk.reflection.Reflect;

import java.util.List;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.handle;

public abstract class Collectable {

    public abstract Collectable initFromList(List values);

    public List getValues() {
        return Reflect.getFields(getClass()).stream().map(field -> handle(() -> field.get(this))).collect(Collectors.toList());
    }
}
