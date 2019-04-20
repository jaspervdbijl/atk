package com.acutus.atk.entity;

import com.acutus.atk.reflection.Reflect;
import com.acutus.atk.reflection.ReflectFields;

import java.util.List;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.handle;

public class AbstractAtk<T> {

    public List<Class> getPathToRoot(List<Class> sources, Class source) {
        if (!source.equals(Object.class)) {
            sources.add(source);
            getPathToRoot(sources,source.getSuperclass());
        }
        return sources;
    }

    public ReflectFields getRefFields() {
        return Reflect.getFields(getClass());
    }

    public <T extends AtkFieldList> T getFields() {
        return (T) getRefFields()
                .filterType(AtkField.class)
                .stream()
                .map(f -> (AtkField) handle(() -> f.get(this)))
                .collect(Collectors.toCollection(AtkFieldList::new));
    }

}
