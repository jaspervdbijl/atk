package com.acutus.atk.reflection;

import com.acutus.atk.util.Assert;
import com.acutus.atk.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.acutus.atk.util.AtkUtil.handle;

/**
 * Created by jaspervdb on 2016/06/08.
 */
public class ReflectFields extends HashMap<String,Field> {

    public ReflectFields() {
    }

    public ReflectFields(Collection<Field> fields) {
        fields.stream().forEach(f -> put(f.getName(),f));
    }

    public ReflectFields(Class type) {
        for (; !Object.class.equals(type); type = type.getSuperclass()) {
            addAllFields(type);
        }
    }


    public void addAllFields(Class type) {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                put(field.getName(),field);
            }
        }
    }

    public Strings getNames() {
        return new Strings(keySet());
    }

    public Optional<Field> get(String name) {
        return Optional.ofNullable(super.get(name));
    }

    public ReflectFields remove(String name) {
        super.remove(name);
        return this;
    }

    public ReflectFields getByNames(Strings names) {
        ReflectFields fields = new ReflectFields();
        names.stream().forEach(n -> fields.put(n,get(n).get()));
        return fields;
    }

    public Optional<Field> getByName(String name) {
        return Optional.ofNullable(get(name) != null? get(name).get(): null);
    }

    /**
     * assert that the field name is uniqie
     * @param field
     */
    public void add(Field field) {
        Assert.isTrue(get(field.getName()) == null,"Field %s has already been added in set",field.getName());
        put(field.getName(),field);
    }

    public ReflectFields filterType(Class filterClass, boolean inverse) {
        ReflectFields fields = new ReflectFields();
        for (Field field : values()) {
            if (inverse != filterClass.isAssignableFrom(field.getType())) {
                fields.add(field);
            }
        }
        return fields;
    }

    public ReflectFields filterType(Class filterClass) {
        return filterType(filterClass, false);
    }

    public ReflectFields getNonNull(Object ref) {
        try {
            ReflectFields fields = new ReflectFields();
            for (Field field : values()) {
                if (field.get(ref) != null) {
                    fields.add(field);
                }
            }
            return fields;
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }

    public <T> List<T> getInstances(Class<T> type, Object source) {
        try {
            List<T> instances = new ArrayList<>();
            for (Field field : filterType(type).values()) {
                instances.add((T) field.get(source));
            }
            return instances;
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }

    /**
     * copy matching field by name and type
     */
    public ReflectFields copyMatchingTo(Object source, ReflectFields dstFields, Object destination, ReflectFields exclude) {
        values().stream().filter(f ->
                dstFields.getByName(f.getName()).isPresent()
                        && f.getType().equals(dstFields.getByName(f.getName()).get().getType()) &&
                        !exclude.getByName(f.getName()).isPresent()
        )
                .forEach(f -> handle(() -> dstFields.getByName(f.getName()).get().set(destination, f.get(source))));
        return this;
    }

    public ReflectFields copyMatchingTo(Object source, ReflectFields dstFields, Object destination) {
        return copyMatchingTo(source, dstFields, destination, new ReflectFields());
    }

}
