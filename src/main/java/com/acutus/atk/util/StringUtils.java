package com.acutus.atk.util;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StringUtils {

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String notEmpty(String value1, String value2) {
        return !isEmpty(value1) ? value1 : value2;
    }

    public static String nonNullStr(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * recursively replace
     *
     * @param string
     * @param replace
     * @param with
     * @return
     */
    public static String replaceAll(String string, String replace, String with) {
        while (string.contains(replace)) string = string.replace(replace, with);
        return string;
    }

    public static String removeAll(String string, String remove) {
        return replaceAll(string, remove, "");
    }

    public static String removeAllASpaces(String string) {
        return removeAll(string, " ");
    }

    public static int indexOf(String string, String value, int position) {
        Assert.isTrue(position > 0,"Position can't be null");
        String split[] = string.split(value);
        return split.length >= position ?
                IntStream.range(0, position).map(i -> split[i].length()).sum() + value.length() * (position-1)
                : -1;
    }

    public static boolean contains(String string, String value, int position) {
        return indexOf(string, value, position) != -1;
    }
    public static String substring(int start, String string, String value, int position) {
        return string.substring(start,indexOf(string, value, position));
    }


}
