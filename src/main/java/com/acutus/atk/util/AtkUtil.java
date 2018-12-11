package com.acutus.atk.util;

import lombok.SneakyThrows;

public class AtkUtil {

    @SneakyThrows
    public static <T,R> R handle(Call.OneEx<R> call) {
        return call.call();
    }

    public static boolean equals(Object o1,Object o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }

}
