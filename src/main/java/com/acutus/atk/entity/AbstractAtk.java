package com.acutus.atk.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.handle;

public interface AbstractAtk<T> {

    public default List<Class> getPathToRoot(List<Class> sources, Class source) {
        if (!source.equals(Object.class)) {
            sources.add(source);
            getPathToRoot(sources,source.getSuperclass());
        }
        return sources;
    }

    public default List<Field> getRefFields() {
        return getPathToRoot(new ArrayList<>(),getClass())
                .stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredFields())).filter(f -> AtkField.class.isAssignableFrom(f.getType()))
                .collect(Collectors.toList());
    }


    public default <T extends AtkFieldList> T getFields() {
        return (T) getRefFields().stream()
                .map(f -> (AtkField) handle(() -> f.get(this)))
                .collect(Collectors.toCollection(AtkFieldList::new));
    }

}
