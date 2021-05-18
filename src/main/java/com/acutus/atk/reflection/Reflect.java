package com.acutus.atk.reflection;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Reflect<T> {
    private static ConcurrentHashMap<Class, ReflectFields> FIELD_MAP = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Class, ReflectMethods> METHOD_MAP = new ConcurrentHashMap<>();

    private ReflectFields fields;
    private ReflectMethods methods;
    private T entity;

    public Reflect(T entity) {
        this.entity = entity;
        fields = getFields(entity.getClass());
        methods = getMethods(entity.getClass());
    }

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
