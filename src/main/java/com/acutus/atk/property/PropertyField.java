package com.acutus.atk.property;

import com.acutus.atk.util.Assert;
import com.acutus.atk.util.StringUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * bind to system properties
 */
public class PropertyField<T> {

    private String name;
    private T defaultValue;
    private Map<Class, Method> PARSE_METHOD_MAP = new HashMap<>();

    public PropertyField(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * \
     * find the parse method for this class
     *
     * @param type
     * @return
     */
    private Method getParseMethodForClass(Class type) {
        if (!PARSE_METHOD_MAP.containsKey(type)) {
            Optional<Method> parseMethod = Arrays.stream(type.getMethods())
                    .filter(m -> m.getName().startsWith("parse")
                            && m.getParameterCount() == 1
                            && String.class.equals(m.getParameterTypes()[0]))
                    .findAny();
            Assert.isTrue(parseMethod.isPresent(), "No Parse method found for type %s", type);
            PARSE_METHOD_MAP.put(type, parseMethod.get());
        }
        return PARSE_METHOD_MAP.get(type);
    }

    @SneakyThrows
    public T get() {
        if (!System.getProperties().containsKey(name)) {
            return defaultValue;
        }

        String value = System.getProperty(name);

        if (StringUtils.isEmpty(value)) {
            return null;
        }

        Class type = defaultValue.getClass();
        if (String.class.equals(type)) {
            return (T) value;
        } else {
            Method parseMethod = getParseMethodForClass(type);
            T instanze = (T) type.newInstance();
            return (T) parseMethod.invoke(instanze, value);
        }
    }

    public void set(T value) {
        System.getProperties().put(name, value != null ? value.toString() : "");
    }
}
