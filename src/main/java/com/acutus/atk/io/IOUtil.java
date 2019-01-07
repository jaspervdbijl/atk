package com.acutus.atk.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

    public static byte[] readAvailable(InputStream is) throws IOException {
        byte buffer[] = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (is.available() > 0) {
            int read = is.read(buffer);
            bos.write(buffer, 0, read);
        }
        return bos.toByteArray();
    }

    public static String readAvailableAsStr(InputStream is) throws IOException {
        return new String(readAvailable(is));
    }

    public static void copyAvailable(InputStream is, OutputStream os) throws IOException {
        os.write(readAvailable(is));
    }

    /**
     * will copy the whole stream, until end of the stream is reached
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte buffer[] = new byte[1024];
        for (int r = is.read(buffer); r != -1; is.read(buffer)) {
            os.write(buffer, 0, r);
        }
    }

}
