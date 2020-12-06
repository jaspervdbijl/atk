package com.acutus.atk.reflection;

import com.acutus.atk.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.handle;

/**
 * Created by jaspervdb on 2016/06/08.
 */
public class ReflectFields extends ArrayList<Field> {

    public ReflectFields() {
    }

    public ReflectFields cloneRefFields() {
        return stream().collect(Collectors.toCollection(ReflectFields::new));
    }


    public ReflectFields(Collection<Field> fields) {
        super.addAll(fields);
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
                add(field);
            }
        }
    }

    public Strings getNames() {
        return stream().map(f -> f.getName()).collect(Collectors.toCollection(Strings::new));
    }

    public Optional<Field> get(String name) {
        return stream().filter(f -> f.getName().equals(name)).findFirst();
    }

    public ReflectFields remove(String name) {
        remove(get(name));
        return this;
    }

    public ReflectFields getByNames(Strings names) {
        return stream().filter(f -> names.contains(f.getName()))
                .collect(Collectors.toCollection(ReflectFields::new));
    }

    public Optional<Field> getByName(String name) {
        ReflectFields fields = getByNames(Strings.asList(name));
        return Optional.ofNullable(!fields.isEmpty() ? fields.get(0) : null);
    }

    public ReflectFields filterType(Class filterClass, boolean inverse) {
        ReflectFields fields = new ReflectFields();
        for (Field field : this) {
            if (inverse != filterClass.isAssignableFrom(field.getType())) {
                fields.add(field);
            }
        }
        return fields;
    }

    public ReflectFields filterType(Class filterClass) {
        return filterType(filterClass, false);
    }

    public ReflectFields filterAnnotation(Class ano) {
        return new ReflectFields((Collection<Field>) stream().filter(f -> f.getAnnotation(ano) != null)
                .collect(Collectors.toList()));
    }

    public ReflectFields filter(Predicate<Field> predicate) {
        return stream().filter(predicate)
                .collect(Collectors.toCollection(ReflectFields::new));
    }

    public ReflectFields getNonNull(Object ref) {
        try {
            ReflectFields fields = new ReflectFields();
            for (Field field : this) {
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
            for (Field field : filterType(type)) {
                instances.add((T) field.get(source));
            }
            return instances;
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(ie);
        }
    }

    private static boolean typeMatch(Class c1,Class c2) {
        return c1.equals(c2) ||
                (c1.getName().startsWith("java.lang") || c2.getName().startsWith("java.lang")) && c2.getSimpleName().equalsIgnoreCase(c2.getSimpleName());
    }
    /**
     * copy matching field by name and type
     */
    public ReflectFields copyMatchingTo(Object source, ReflectFields dstFields, Object destination, ReflectFields exclude, boolean copyNull) {
        stream().filter(f ->
                dstFields.getByName(f.getName()).isPresent()
                        && typeMatch(f.getType(),dstFields.getByName(f.getName()).get().getType()) &&
                        exclude != null && !exclude.getByName(f.getName()).isPresent() &&
                        (copyNull || handle(() -> f.get(source) != null))
        )
                .forEach(f -> handle(() -> dstFields.getByName(f.getName()).get().set(destination, f.get(source))));
        return this;
    }

    public ReflectFields copyMatchingTo(Object source, ReflectFields dstFields, Object destination, boolean copyNull) {
        return copyMatchingTo(source, dstFields, destination, new ReflectFields(),copyNull);
    }

    public ReflectFields copyMatchingTo(Object source, Object destination) {
        return copyMatchingTo(source, Reflect.getFields(destination.getClass()), destination, new ReflectFields(),false);
    }

}
