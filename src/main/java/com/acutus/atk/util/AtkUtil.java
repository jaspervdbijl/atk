package com.acutus.atk.util;

import com.acutus.atk.util.call.CallNil;
import com.acutus.atk.util.call.CallNilRet;
import com.acutus.atk.util.call.CallOne;
import com.acutus.atk.util.call.CallTwo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.acutus.atk.util.StringUtils.subLength;

@Slf4j
public class AtkUtil {

    @SneakyThrows
    public static void handle(CallNil call) {
        call.call();
    }

    @SneakyThrows
    public static void handle(CallNil call, CallOne<Exception> handle) {
        try {
            call.call();
        } catch (Exception ex) {
            log.warn(ex.getMessage(),ex);
            handle.call(ex);
        }
    }

    @SneakyThrows
    public static <T, R> R handle(CallNilRet<R> call) {
        return call.call();
    }

    @SneakyThrows
    public static <T, R> Optional<R> handle(CallNilRet<R> call,CallOne<Exception> handleEx) {
        try {
            return Optional.of(call.call());
        } catch (Exception ex) {
            log.warn(ex.getMessage(),ex);
            handleEx.call(ex);
            return Optional.empty();
        }
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }

    /**
     * return the generic type of a class
     *
     * @param clazz
     * @return
     */
    public static Class getGenericType(Class clazz, int index) {
        return ((Class) ((ParameterizedType) clazz.getGenericSuperclass())
                .getActualTypeArguments()[index]);
    }

    public static Class getGenericType(Class clazz) {
        return getGenericType(clazz, 0);
    }

    public static Class getGenericFieldType(Field field) {
        return (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
    }

    /**
     * if the class is a defined primitive type
     *
     * @param type
     * @return
     */
    public static boolean isPrimitive(String type) {
        return type.startsWith("java.lang.") ||
                type.startsWith("java.time.Local");
    }

    public static boolean isPrimitive(Class type) {
        return isPrimitive(type.getName());
    }

    public static String convertStackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String convertStackTraceToString(Throwable e,int length) {
        return subLength(convertStackTraceToString(e),length);
    }

    public static boolean nonNull(Boolean value) {
        return value != null ? value : false;
    }

    public static double nonNull(Double value) {
        return value != null ? value : 0.0;
    }

    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException pe) {
            return false;
        }
    }

    
    public static String getHumanFriendlyName(String name) {
        name = name.replaceAll("[^A-Za-z]"," ");
        return Arrays.stream(name.split(" ")).map(n -> n.substring(0,1).toUpperCase() + n.substring(1).toLowerCase()).reduce((a, b) -> a+" " + b).get();
    }

    public static <T> void idxStream(Stream<T> s, CallTwo<Integer, T> call) {
        AtomicInteger i = new AtomicInteger(0);
        s.forEach(a -> handle(() -> call.call(i.getAndIncrement(),a)));
    }

    public static String leftPad(String value, char pad, int length) {
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length) {
            sb.insert(0,pad);
        }
        return sb.toString();
    }

}
