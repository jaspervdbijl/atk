package com.acutus.atk.entity;

import com.acutus.atk.entity.processor.AtkEdit;
import com.acutus.atk.util.IOUtil;
import com.acutus.atk.reflection.Reflect;
import com.acutus.atk.reflection.ReflectFields;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.getGenericType;
import static com.acutus.atk.util.AtkUtil.handle;

// T the current instance and O is the original dao entity
public class AbstractAtk<T extends AbstractAtk, O> {

    private static Map<String, String> CACHED_RESOURCES = new HashMap<>();

    @SneakyThrows
    public static String getCachedResource(String resource) {
        if (!CACHED_RESOURCES.containsKey(resource)) {
            CACHED_RESOURCES.put(resource, new String(IOUtil.readAvailable(Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream(resource))));
        }
        return CACHED_RESOURCES.get(resource);
    }

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
        getRefFields().copyMatchingTo(this, Reflect.getFields(base.getClass()), base, true);
        return base;
    }

    public T initFrom(O base, AtkFieldList exclude, boolean copyNull) {
        ReflectFields bFields = Reflect.getFields(base.getClass());
        // ignore from base ignore fields
        ReflectFields exclFields = (exclude == null ? new AtkFieldList() : exclude).toRefFields()
                .addAll(bFields.stream().filter(f -> f.getAnnotation(AtkEdit.class) != null)
                        .collect(Collectors.toList()));
        bFields.copyMatchingTo(base, getRefFields(), this, exclFields, copyNull);
        // update set state
        bFields.stream().forEach(f -> handle(() ->
                getFields().getByName(f.getName()).ifPresent(myField -> ((AtkField) myField).setSet(true))));
        return (T) this;
    }

    public T initFrom(O base, AtkFieldList exclude) {
        return initFrom(base, exclude, false);
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

    public T ignoreFields() {
        getFields().stream().forEach(f -> ((AtkField) f).ignore());
        return (T) this;
    }

    public T ignoreFieldsExcept(Field... fields) {
        List<String> fieldList = Arrays.stream(fields).map(f -> f.getName()).collect(Collectors.toList());
        getFields().stream()
                .filter(f -> {
                    String fName = ((AtkField) f).getField().getName();
                    return !fieldList.contains(fName) && !fieldList.contains("_" + fName);
                })
                .forEach(f -> ((AtkField) f).ignore());
        return (T) this;
    }


    public String getMd5Hash() {
        throw new RuntimeException("Not implemented");
    }

    public String getCompileTime() {
        throw new RuntimeException("Not implemented");
    }

    public LocalDateTime getCompileTimeAsTime() {
        return LocalDateTime.parse(getCompileTime());
    }

}
