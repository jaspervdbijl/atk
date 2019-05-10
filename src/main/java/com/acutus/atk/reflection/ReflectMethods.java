package com.acutus.atk.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by jaspervdb on 2016/06/08.
 */
public class ReflectMethods extends ArrayList<Method> {

    public ReflectMethods() {
    }

    public ReflectMethods(Collection<Method> methods) {
        super.addAll(methods);
    }


    public ReflectMethods(Class type) {
        for (; !Object.class.equals(type); type = type.getSuperclass()) {
            addAllFields(type);
        }
    }

    public ReflectMethods filter(boolean inverse, String... names) {
        return stream().filter(f -> !inverse == Arrays.stream(names)
                .filter(n -> f.getName().equalsIgnoreCase(n)).findFirst().isPresent())
                .collect(Collectors.toCollection(ReflectMethods::new));
    }

    public ReflectMethods filter(String... names) {
        return filter(false, names);
    }

    public ReflectMethods filter(Class<? extends Annotation> aType) {
        return stream().filter(m -> m.getAnnotation(aType) != null).collect(Collectors.toCollection(ReflectMethods::new));
    }

    public void addAllFields(Class type) {
        for (Method method : type.getMethods()) {
            method.setAccessible(true);
            add(method);
        }
    }

    public Method get(boolean ignoreCase, String name) {
        for (Method method : this) {
            if (ignoreCase ? method.getName().equalsIgnoreCase(name) : method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public ReflectMethods getByNames(boolean ignoreCase, String... names) {
        ReflectMethods fields = new ReflectMethods();
        for (String name : names) {
            if (get(ignoreCase, name) != null) {
                fields.add(get(ignoreCase, name));
            }
        }
        return fields;
    }

    public ReflectMethods getByNames(String... names) {
        return getByNames(false, names);
    }

    public ReflectMethods filterByReturnType(Class type) {
        return stream().filter(m -> type.isAssignableFrom(m.getReturnType()))
                .collect(Collectors.toCollection(ReflectMethods::new));
    }

    private boolean paramEquals(List<Class> l1, List<Class> l2) {
        return l1.size() == l2.size() &&
                IntStream.range(0, l1.size()).filter(i -> l1.get(i).getSimpleName().equalsIgnoreCase(l2.get(i).getSimpleName()))
                        .count() == l1.size();
    }

    public ReflectMethods filterStatic(boolean inverse) {
        return stream().filter(m -> Modifier.isStatic(m.getModifiers()) != inverse).collect(Collectors.toCollection(ReflectMethods::new));
    }

    public ReflectMethods filterStatic() {
        return filterStatic(false);
    }

    public ReflectMethods filterParams(Class... params) {
        return stream().filter(m ->
                paramEquals(Arrays.stream(m.getParameters()).map(p -> p.getType()).collect(Collectors.toList())
                        , Arrays.asList((params == null ? new Class[]{} : params))))
                .collect(Collectors.toCollection(ReflectMethods::new));
    }

    public Optional<Method> getByName(String name, boolean ignoreCase) {
        ReflectMethods fields = getByNames(ignoreCase, name);
        return Optional.ofNullable(!fields.isEmpty() ? fields.get(0) : null);
    }

    public Optional<Method> getByName(String name) {
        return getByName(name, false);
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            names.add(get(i).getName());
        }
        return names;
    }

    public boolean contains(boolean ignoreCase, String name) {
        return get(ignoreCase, name) != null;
    }

    public ReflectMethods setAccessible(boolean accessible) {
        for (Method method : this) {
            method.setAccessible(accessible);
        }
        return this;
    }

}
