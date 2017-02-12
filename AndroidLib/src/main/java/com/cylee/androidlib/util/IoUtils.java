package com.cylee.androidlib.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by cylee on 15/4/1.
 */
public class IoUtils {

  public static void closeQuietly(Closeable output) {
    try {
      if (output != null) {
        output.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

  public static String toString(InputStream in, String encoding){
    String result = null;
    try {
      int size = in.available();
      byte[] buffer = new byte[size];
      in.read(buffer);
      try {
        result = new String(buffer, encoding);
      } catch (UnsupportedEncodingException e) {
        result = new String(buffer);
      }
    } catch (IOException ex) {
    } finally {
      closeQuietly(in);
    }
    return result;
  }
}
