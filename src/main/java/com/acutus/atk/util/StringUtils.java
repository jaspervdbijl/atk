package com.acutus.atk.util;

import com.acutus.atk.util.call.CallNilRet;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StringUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static String notEmpty(String value1, String value2) {
        return !isEmpty(value1) ? value1 : value2;
    }

    public static String nonNullStr(Object value) {
        return value != null ? value.toString() : "";
    }

    public static String defaultString(String value) {
        return isEmpty(value) ? "" : value;
    }

    @SneakyThrows
    public static String defaultString(String value, CallNilRet<String> call,String defaultValue) {
        return isNotEmpty(value) ? call.call() : defaultValue;
    }

    public static String maxLen(String value,int length) {
        return notEmpty(value,"").length() > length ? value.substring(0,length) : value;
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

    public static String subLength(String string, int length) {
        return string.length() > length ? string.substring(0,length) : string;
    }

    public static String filterNonVisibleAsciChars(String text) {
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        return text.replaceAll("\\p{C}", "");
    }


}
