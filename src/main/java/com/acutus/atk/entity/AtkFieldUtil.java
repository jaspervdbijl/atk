package com.acutus.atk.entity;

import com.acutus.atk.util.Assert;
import com.acutus.atk.util.RunException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class AtkFieldUtil {

    public static Field getFieldByName(Class type, String name) {
        Optional<Field> field = Arrays.stream(type.getDeclaredFields())
                .filter(f -> name.equals(f.getName()))
                .findFirst();

        Assert.isTrue(field.isPresent() || !Object.class.equals(type.getSuperclass())
                ,() -> new RunException("Field not found %s in class %s",name,type.getName()));
        return field.isPresent() ? field.get() : getFieldByName(type.getSuperclass(), name);
    }
}
