package com.acutus.atk.util;

public class FieldUtil {

    public static boolean nullSafe(Boolean value) {
        return value == null ? false : value;
    }
}
