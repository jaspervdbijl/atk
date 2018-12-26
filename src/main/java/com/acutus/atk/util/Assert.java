package com.acutus.atk.util;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by jaspervdb on 2/24/16.
 */
@Log
public class Assert {

    public static void isTrue(Class<? extends RuntimeException > exClass, boolean value, String msg, Object... params) {
        if (!value) {
            try {
                msg = params != null? String.format(msg,params):msg;
                throw exClass.getConstructor(String.class).newInstance(msg);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SneakyThrows
    public static void isTrue(boolean value, Call.ZeroRet<RuntimeException> call) {
        if (!value) {
            RuntimeException ex = call.call();
            log.log( Level.WARNING,ex.getMessage(),ex);
            throw ex;
        }
    }

    public static void isTrue(boolean value, String msg) {
        if (!value) {
             throw new RuntimeException(msg);
        }
    }

    public static void isTrue(boolean value, Runnable callback) {
        if (!value) {
            callback.run();
        }
    }

    private static Object mapParam(Object value) {
        if (value != null && value instanceof Object[]) {
            return Arrays.asList((Object[])value).stream().map(o -> mapParam(o)).reduce((s1, s2)->s1+","+s2).get();
        } else if (value != null && value instanceof List) {
            return ((List)value).stream().map(o -> mapParam(o)).reduce((s1, s2)->s1+","+s2).get();
        } else {
            return value;
        }
    }

    public static void isTrue(boolean condition, String msg, Object... params) {
        if (!condition) {
            // map params to strings
            if (params != null) {
                params = Arrays.asList(params).stream().map(o -> mapParam(o)).toArray();
            }
            throw new RuntimeException(String.format(msg,params));
        }
    }

    public static void notNull(Class<? extends RuntimeException > exClass, Object value, String msg) {
        isTrue(exClass,value != null,msg);
    }

    public static void notNull(Object value, String msg) {
        isTrue(value != null,msg);
    }

    public static void notNull(Object value, String msg, Object... params) {
        isTrue(value != null,msg,params);
    }

    public static void notEmpty(Collection value, String msg, Object... params) {
        isTrue(value != null && !value.isEmpty(),msg,params);
    }

    public static void notEmpty(Collection value, String msg) {
        notEmpty(value,msg,null);
    }

}
