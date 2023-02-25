package com.acutus.atk.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ClsFile implements Closeable {


    private File file;

    public ClsFile(File file) {
        this.file = file;
        this.file.deleteOnExit();
    }

    public static ClsFile getTemp(String pre, String post) throws IOException {
        return new ClsFile(File.createTempFile(pre, post));
    }

    public static ClsFile getTempFolder(String pre, String post) throws IOException {
        File file = File.createTempFile(pre, post);
        file.delete();
        Assert.isTrue(file.mkdir(), "Unable to create folder " + file.getAbsolutePath());
        return new ClsFile(new File(file.getAbsolutePath()));
    }

    public File getFile() {
        return file;
    }


    @Override
    public void close() {
        if (file.isDirectory()) {
            FileUtil.delete(file);
        } else {
            file.delete();
        }
    }

}
