package com.acutus.atk.util;

import com.acutus.atk.reflection.Reflect;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldUtil {

    private static Map<Class, Method> parseMap = new HashMap<>();

    static {
        for (Class type : new Class[]{Long.class, Float.class, Boolean.class, Double.class, Short.class}) {
            parseMap.put(type, Reflect.getMethods(type).getByName("valueOf").get());
        }
    }

    public static boolean nullSafe(Boolean value) {
        return value == null || !value ? false : value;
    }

    public static int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    public static int defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }


    public static void setSerialized(Object dto, Field field, String serial) throws IllegalAccessException, InvocationTargetException {
        field.set(dto, field.getType().equals(String.class) ? serial : parseMap.get(field.getType()).invoke(null, serial));
    }

    @SneakyThrows
    public static void setHexAsBit(Object entity, String hex, boolean inverse, boolean reverse) {
        String binary = Strings.splitByLength(hex, 2)
                .stream().map(s -> Integer.toBinaryString(Integer.parseInt(s, 16)))
                .reduce((a, b) -> a + b).get();
        binary = reverse ? new StringBuilder(binary).reverse().toString() : binary;
        List<Field> fields = Reflect.getFields(entity.getClass()).stream().toList();
        Assert.isTrue(fields.size() == binary.length(), "Invalid hex length");
        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).set(entity, binary.charAt(i) == (inverse ? '0' : '1'));
        }
    }

    @SneakyThrows
    public static void setHexAsBit(Object entity, String hex, boolean inverse) {
        setHexAsBit(entity, hex, inverse, false);
    }
}
