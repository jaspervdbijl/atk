package com.acutus.atk.util;

import java.io.*;
import java.util.zip.*;

import static com.acutus.atk.util.IOUtil.copy;
import static com.acutus.atk.util.IOUtil.readFully;

public class ObjectUtils {

    /**
     *
     * @param n1
     * @param n2
     * @return
     */
    public static boolean numberEquals(Number n1,Number n2) {
        if (n1 != null && n2 != null) {
            if (n1 instanceof Long || n1 instanceof Integer) {
                return n1.longValue() == n2.longValue();
            } else {
                throw new UnsupportedOperationException("Type " + n1.getClass() + " not implemented yet");
            }
        }
        return false;
    }
}
