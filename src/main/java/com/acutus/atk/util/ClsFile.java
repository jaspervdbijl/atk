package com.acutus.atk.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ClsFile implements Closeable  {

  
  private File file;

  public ClsFile(File file) {
    this.file = file;
    this.file.deleteOnExit();
  }

  public static ClsFile getTemp(String pre, String post) throws IOException {
      return new ClsFile(File.createTempFile(pre,post));
  }

  public File getFile() {
    return file;
  }


  @Override
  public void close() {
    try {
      if (file.isDirectory()) {
        Files.delete(file.toPath());
      } else {
        file.delete();
      }
    } catch (IOException ioe) {
      log.warn(ioe.getMessage(),ioe);
    }
  }

}
