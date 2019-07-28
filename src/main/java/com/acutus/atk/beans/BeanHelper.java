package com.acutus.atk.beans;

import com.acutus.atk.util.Assert;
import com.acutus.atk.util.call.CallOneRet;
import lombok.SneakyThrows;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BeanHelper {

    public static final Map<Class, CallOneRet<String, Object>> DECODE_MAP = new HashMap<Class, CallOneRet<String, Object>>() {{
        put(String.class, s -> s);
        put(Integer.class, s -> Integer.parseInt(s));
        put(Long.class, s -> Long.parseLong(s));
        put(Boolean.class, s -> Boolean.parseBoolean(s));
        put(Byte.class, s -> Byte.parseByte(s));
        put(Double.class, s -> Double.parseDouble(s));
        put(Short.class, s -> Short.parseShort(s));
        put(Byte[].class, s -> Base64.getDecoder().decode(s));
        put(byte[].class, s -> Base64.getDecoder().decode(s));
        put(Date.class, s -> new Date(Long.parseLong(s)));
        put(Time.class, s -> new Date(Long.parseLong(s)));
        put(Timestamp.class, s -> new Date(Long.parseLong(s)));
        put(java.sql.Date.class, s -> new Date(Long.parseLong(s)));
    }};

    public static final Map<Class, CallOneRet<Object, String>> ENCODE_MAP = new HashMap<Class, CallOneRet<Object, String>>() {{
        put(String.class, s -> s.toString());
        put(Integer.class, s -> s.toString());
        put(Long.class, s -> s.toString());
        put(Boolean.class, s -> s.toString());
        put(Byte.class, s -> s.toString());
        put(Double.class, s -> s.toString());
        put(Short.class, s -> s.toString());
        put(Byte[].class, s -> Base64.getEncoder().encodeToString((byte[]) s));
        put(byte[].class, s -> Base64.getEncoder().encodeToString((byte[]) s));
        put(Date.class, s -> ((Date) s).getTime() + "");
        put(Time.class, s -> ((Date) s).getTime() + "");
        put(Timestamp.class, s -> ((Date) s).getTime() + "");
        put(java.sql.Date.class, s -> ((Date) s).getTime() + "");
    }};

    @SneakyThrows
    public static <T> T decode(Class<T> type, String value) {
        if (value == null) return null;

        CallOneRet<String, Object> call = DECODE_MAP.get(type);
        Assert.isTrue(call != null, "Type not implemented", type.getName());
        return (T) call.call(value);
    }

    @SneakyThrows
    public static String encode(Object value) {
        if (value == null) return null;

        CallOneRet<Object, String> call = ENCODE_MAP.get(value.getClass());
        Assert.isTrue(call != null, "Type not implemented", value.getClass().getName());
        return call.call(value);
    }
}
