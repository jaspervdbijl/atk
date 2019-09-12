package com.acutus.atk.entity;

import com.acutus.atk.reflection.Reflect;
import com.acutus.atk.reflection.ReflectFields;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.getGenericType;
import static com.acutus.atk.util.AtkUtil.handle;

// T the current instance and O is the original dao entity
public class AbstractAtk<T extends AbstractAtk, O> {

    public List<Class> getPathToRoot(List<Class> sources, Class source) {
        if (!source.equals(Object.class)) {
            sources.add(source);
            getPathToRoot(sources, source.getSuperclass());
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

    public Class<O> getBaseClass() {
        return getGenericType(getClass(), 1);
    }

    /**
     * @return the base type form which the entity was generated
     */
    @SneakyThrows
    public O toBase() {
        O base = (O) getBaseClass().getConstructor().newInstance();
        getRefFields().copyMatchingTo(this, Reflect.getFields(base.getClass()), base);
        return base;
    }

    public T initFrom(O base, AtkFieldList exclude) {
        Reflect.getFields(base.getClass()).
                copyMatchingTo(base, getRefFields(), this, exclude != null ? exclude.toRefFields() : null);
        restoreSet();
        return (T) this;
    }

    public T initFrom(O base, AtkField... exclude) {
        return initFrom(base, new AtkFieldList(Arrays.asList(exclude)));
    }

    public T initFrom(O base) {
        return initFrom(base, new AtkFieldList());
    }

    public String toString() {
        return getClass().getSimpleName() + "{\n\t" + getFields().toString() + "\n}";
    }

    @SneakyThrows
    public T clone() {
        T clone = (T) getClass().getConstructor().newInstance();
        clone.getFields().initFrom(getFields());
        return clone;
    }

    /**
     * this will restore the set state of all the fields with values
     *
     * @return
     */
    public T restoreSet() {
        getFields().restoreSet();
        return (T) this;
    }


}
