package com.acutus.atk.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MapUtil {

    public static <A,B> LinkedHashMap linkedMapOf(List<A> a, List<B> b) {
        Assert.isTrue(a.size() == b.size(),"Different lengths");
        LinkedHashMap<A,B> map = new LinkedHashMap();
        IntStream.range(0,a.size()).forEach(i -> map.put(a.get(i),b.get(i)));
        return map;
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a,B b) {
        return linkedMapOf(Arrays.asList(a),Arrays.asList(b));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2) {
        return linkedMapOf(Arrays.asList(a1,a2),Arrays.asList(b1,b2));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3) {
        return linkedMapOf(Arrays.asList(a1,a2,a3),Arrays.asList(b1,b2,b3));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4),Arrays.asList(b1,b2,b3,b4));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5),Arrays.asList(b1,b2,b3,b4,b5));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6),Arrays.asList(b1,b2,b3,b4,b5,b6));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6,A a7,B b7) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6,a7),Arrays.asList(b1,b2,b3,b4,b5,b6,b7));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6,A a7,B b7,A a8,B b8) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6,a7,a8),Arrays.asList(b1,b2,b3,b4,b5,b6,b7,b8));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6,A a7,B b7,A a8,B b8,A a9,B b9) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6,a7,a8,a9),Arrays.asList(b1,b2,b3,b4,b5,b6,b7,b8,b9));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6,A a7,B b7,A a8,B b8,A a9,B b9,A a10,B b10) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6,a7,a8,a9,a10),Arrays.asList(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6,A a7,B b7,A a8,B b8,A a9,B b9,A a10,B b10,A a11,B b11) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11),Arrays.asList(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11));
    }

    public static <A,B> LinkedHashMap linkedMapOf(A a1,B b1,A a2,B b2,A a3,B b3,A a4,B b4,A a5,B b5,A a6,B b6,A a7,B b7,A a8,B b8,A a9,B b9,A a10,B b10,A a11,B b11,A a12,B b12) {
        return linkedMapOf(Arrays.asList(a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12),Arrays.asList(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12));
    }

}
