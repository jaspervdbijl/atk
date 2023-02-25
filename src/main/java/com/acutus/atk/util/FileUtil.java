package com.acutus.atk.util;

import java.io.*;
import java.util.zip.*;

import static com.acutus.atk.util.IOUtil.copy;
import static com.acutus.atk.util.IOUtil.readFully;

public class FileUtil {
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

    public static File zip(File files[], String fname[]) throws ZipException, IOException {
        File zfile = File.createTempFile("data", ".zip");
        try (ZipOutputStream zostream = new ZipOutputStream(new FileOutputStream(zfile))) {
            zostream.setLevel(Deflater.BEST_COMPRESSION);
            int cnt = 0;
            for (File file : files) {
                ZipEntry entry = new ZipEntry(fname[cnt++]);
                zostream.putNextEntry(new ZipEntry(entry.getName()));
                try (FileInputStream fis = new FileInputStream(file)) {
                    copy(fis, zostream);
                }
            }
        }
        return zfile;
    }

    public static File zip(File file[]) throws IOException {
        Strings lst = new Strings();
        for (File f : file) {
            lst.add(f.getName());
        }
        return zip(file, lst.toArray(new String[]{}));
    }

    public static File zip(File file) throws IOException {
        return zip(new File[]{file}, new String[]{file.getName()});
    }

    public static void delete(File folder) {
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                delete(file);
            }
        }
        folder.delete();
    }
}
