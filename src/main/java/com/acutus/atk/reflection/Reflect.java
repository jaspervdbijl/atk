package com.acutus.atk.reflection;

import lombok.Synchronized;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reflect<T> {
    private static Map<Class, ReflectFields> FIELD_MAP = new HashMap<>();
    private static Map<Class, ReflectMethods> METHOD_MAP = new HashMap<>();

    private ReflectFields fields;
    private ReflectMethods methods;
    private T entity;

    public Reflect(T entity) {
        this.entity = entity;
        fields = getFields(entity.getClass());
        methods = getMethods(entity.getClass());
    }

    @Synchronized
    public static ReflectFields getFields(Class type) {
        if (!FIELD_MAP.containsKey(type)) {
            FIELD_MAP.put(type, new ReflectFields(type));
        }
        return FIELD_MAP.get(type);
    }

    public static ReflectFields getFields(Class type, boolean ignoreMap) {
        if (ignoreMap) {
            return new ReflectFields(type);
        } else {
            return getFields(type);
        }
    }

    @Synchronized
    public static ReflectMethods getMethods(Class type) {
        if (!METHOD_MAP.containsKey(type)) {
            METHOD_MAP.put(type, new ReflectMethods(type));
        }
        return METHOD_MAP.get(type);
    }

    public ReflectFields getFields() {
        return fields;
    }

    public ReflectMethods getMethods() {
        return methods;
    }

    public List<String> getNames() {
        return fields.getNames();
    }



}
