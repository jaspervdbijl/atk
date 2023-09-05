package com.acutus.atk.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by jaspervdb on 2016/06/08.
 */
public class ReflectMethods implements Iterable<Method> {

    private List<Method> methods = new ArrayList<>();

    public ReflectMethods() {
    }

    public ReflectMethods(Collection<Method> methods) {
        this.methods.addAll(methods);
    }


    public ReflectMethods(Class type) {
        for (; !Object.class.equals(type); type = type.getSuperclass()) {
            addAllMethods(type);
        }
    }

    public ReflectMethods filter(boolean inverse, String... names) {
        return new ReflectMethods(methods.stream().filter(f -> !inverse == Arrays.stream(names)
                        .filter(n -> f.getName().equalsIgnoreCase(n)).findFirst().isPresent())
                .collect(Collectors.toList()));
    }

    public ReflectMethods filter(String... names) {
        return filter(false, names);
    }

    public ReflectMethods filter(Class<? extends Annotation> aType) {
        return new ReflectMethods(methods.stream().filter(m -> m.getAnnotation(aType) != null).collect(Collectors.toList()));
    }

    private void addAllMethods(Class type) {
        for (Method method : type.getMethods()) {
            method.setAccessible(true);
            methods.add(method);
        }
    }

    public Method get(boolean ignoreCase, String name) {
        for (Method method : methods) {
            if (ignoreCase ? method.getName().equalsIgnoreCase(name) : method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public Method get(boolean ignoreCase, String name, Class... parameters) {
        for (Method method : methods) {
            if (ignoreCase ? method.getName().equalsIgnoreCase(name) : method.getName().equals(name)) {
                if (method.getParameterCount() == parameters.length &&
                        !IntStream.range(0, parameters.length).filter(i -> method.getParameterTypes().equals(parameters[i])).findFirst().isPresent())
                    return method;
            }
        }
        return null;
    }

    public Method get(int index) {
        return methods.get(index);
    }

    private void add(Method method) {
        methods.add(method);
    }


    public ReflectMethods getByNames(boolean ignoreCase, String... names) {
        ReflectMethods values = new ReflectMethods();
        for (String name : names) {
            if (get(ignoreCase, name) != null) {
                values.add(get(ignoreCase, name));
            }
        }
        return values;
    }

    public boolean isEmpty() {
        return methods.isEmpty();
    }

    public int size() {
        return methods.size();
    }

    public ReflectMethods getByNames(String... names) {
        return getByNames(false, names);
    }

    public ReflectMethods filterByReturnType(Class type) {
        return new ReflectMethods(methods.stream().filter(m -> type.isAssignableFrom(m.getReturnType()))
                .collect(Collectors.toList()));
    }

    private boolean paramEquals(List<Class> l1, List<Class> l2) {
        return l1.size() == l2.size() &&
                IntStream.range(0, l1.size()).filter(i -> l1.get(i).getSimpleName().equalsIgnoreCase(l2.get(i).getSimpleName()))
                        .count() == l1.size();
    }

    public ReflectMethods filterStatic(boolean inverse) {
        return new ReflectMethods(methods.stream().filter(m -> Modifier.isStatic(m.getModifiers()) != inverse).collect(Collectors.toList()));
    }

    public ReflectMethods filterStatic() {
        return filterStatic(false);
    }

    public ReflectMethods filterParams(Class... params) {
        return new ReflectMethods(methods.stream().filter(m ->
                        paramEquals(Arrays.stream(m.getParameters()).map(p -> p.getType()).collect(Collectors.toList())
                                , Arrays.asList((params == null ? new Class[]{} : params))))
                .collect(Collectors.toList()));
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
        for (Method method : methods) {
            method.setAccessible(accessible);
        }
        return this;
    }

    @Override
    public Iterator<Method> iterator() {
        return methods.iterator();
    }

    public Stream<Method> stream() {
        return methods.stream();
    }

    public Collection<Method> toCollection() {
        return new ArrayList<>(methods);
    }


}
