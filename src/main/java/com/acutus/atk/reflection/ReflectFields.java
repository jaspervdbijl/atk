package com.acutus.atk.reflection;

import com.acutus.atk.util.Strings;
import com.google.common.collect.UnmodifiableListIterator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.print.attribute.UnmodifiableSetException;
import java.lang.instrument.UnmodifiableModuleException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.acutus.atk.util.AtkUtil.handle;

/**
 * Created by jaspervdb on 2016/06/08.
 */
@Slf4j
public class ReflectFields implements Iterable<Field> {

    private List<Field> fields = new ArrayList<>();

    public ReflectFields() {
    }

    public ReflectFields cloneRefFields() {
        return new ReflectFields(fields.stream().collect(Collectors.toList()));
    }


    public ReflectFields(Collection<Field> fields) {
        this.fields.addAll(fields);
    }

    public ReflectFields(Class type) {
        for (; !Object.class.equals(type); type = type.getSuperclass()) {
            addAllFields(type);
        }
    }

    private void addAllFields(Class type) {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public Field get(int index) {
        return fields.get(index);
    }

    private void add(Field field) {
        fields.add(field);
    }


    public Strings getNames() {
        return fields.stream().map(f -> f.getName()).collect(Collectors.toCollection(Strings::new));
    }

    public Optional<Field> get(String name) {
        return fields.stream().filter(f -> f.getName().equals(name)).findFirst();
    }

    public ReflectFields getByNames(Strings names) {
        return new ReflectFields(fields.stream().filter(f -> names.contains(f.getName()))
                .collect(Collectors.toList()));
    }

    public Optional<Field> getByName(String name) {
        ReflectFields fields = getByNames(Strings.asList(name));
        return Optional.ofNullable(!fields.isEmpty() ? fields.get(0) : null);
    }

    public ReflectFields filterType(Class filterClass, boolean inverse) {
        List<Field> values = new ArrayList<>();
        for (Field field : fields) {
            if (inverse != filterClass.isAssignableFrom(field.getType())) {
                values.add(field);
            }
        }
        return new ReflectFields(values);
    }

    public ReflectFields filterType(Class filterClass) {
        return filterType(filterClass, false);
    }

    public ReflectFields filterAnnotation(Class ano) {
        return new ReflectFields(fields.stream().filter(f -> f.getAnnotation(ano) != null)
                .collect(Collectors.toList()));
    }

    public ReflectFields filter(Predicate<Field> predicate) {
        return new ReflectFields(fields.stream().filter(predicate)
                .collect(Collectors.toList()));
    }

    public ReflectFields getNonNull(Object ref) {
        try {
            ReflectFields values = new ReflectFields();
            for (Field field : fields) {
                if (field.get(ref) != null) {
                    values.add(field);
                }
            }
            return values;
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }

    public <T> List<T> getInstances(Class<T> type, Object source) {
        try {
            List<T> instances = new ArrayList<>();
            for (Field field : filterType(type).fields) {
                instances.add((T) field.get(source));
            }
            return instances;
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }

    private static boolean typeMatch(Class c1,Class c2) {
        return c1.equals(c2) ||
                (c1.getName().startsWith("java.lang") || c2.getName().startsWith("java.lang"))
                        && c1.getSimpleName().equalsIgnoreCase(c2.getSimpleName());
    }

    private boolean matches(Object source, Field f, ReflectFields dstFields, Object destination, ReflectFields exclude, boolean copyNull) {
        return dstFields.getByName(f.getName()).isPresent()
                && typeMatch(f.getType(),dstFields.getByName(f.getName()).get().getType()) &&
                (exclude == null || !exclude.getByName(f.getName()).isPresent()) &&
                (copyNull || handle(() -> f.get(source) != null));
    }
    /**
     * copy matching field by name and type
     */
    @SneakyThrows
    public ReflectFields copyMatchingTo(Object source, ReflectFields dstFields, Object destination, ReflectFields exclude, boolean copyNull) {
        fields.stream().filter(f -> matches(source,f,dstFields,destination,exclude,copyNull))
                .forEach(f -> handle(() -> dstFields.getByName(f.getName()).get().set(destination, f.get(source))));
        return this;
    }

    public ReflectFields copyMatchingTo(Object source, ReflectFields dstFields, Object destination, boolean copyNull) {
        return copyMatchingTo(source, dstFields, destination, new ReflectFields(),copyNull);
    }

    public ReflectFields copyMatchingTo(Object source, Object destination) {
        return copyMatchingTo(source, Reflect.getFields(destination.getClass()), destination, new ReflectFields(),false);
    }

    @Override
    public Iterator<Field> iterator() {
        return fields.iterator();
    }

    public Stream<Field> stream() {
        return fields.stream();
    }

    public Collection<Field> toCollection() {
        return new ArrayList<>(fields);
    }
}
