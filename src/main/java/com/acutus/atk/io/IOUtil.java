package com.acutus.atk.io;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public static byte[] readFully(InputStream is, Integer length) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (int r = is.read(buffer); r > -1 && (length == null || length == -1 || bos.size() < length); r = is.read(buffer)) {
                bos.write(buffer, 0, r);
            }
            if (length != null && length != -1 && bos.size() != length) {
                throw new EOFException("Length to read: " + length + " actual: " + bos.size());
            }
            return bos.toByteArray();
        } finally {
            is.close();
        }
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
        for (int r = is.read(buffer); r != -1;r = is.read(buffer)) {
            os.write(buffer, 0, r);
        }
    }

    public static byte[] readFully(InputStream is) throws IOException {
        return readFully(is, null);
    }

    public static byte[] readFully(File file) throws IOException {
        return readFully(new FileInputStream(file), null);
    }


    public static byte[] zip(InputStream is) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gos = new GZIPOutputStream(bos)) {
                gos.write(readFully(is));
                gos.close();
                return bos.toByteArray();
            }
        }
    }

    public static byte[] zip(String data) throws IOException {
        return zip(new ByteArrayInputStream(data.getBytes()));
    }

    public static byte[] unzip(InputStream zip) throws IOException {
        try (GZIPInputStream fis = new GZIPInputStream(zip)) {
            return readFully(fis);
        }
    }

}
