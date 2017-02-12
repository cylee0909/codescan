package com.cylee.androidlib.util;

import com.cylee.lib.BuildConfig;

/**
 * Created by cylee on 15/3/9.
 */
public class DebugUtils {
  private static boolean DEBUG = BuildConfig.DEBUG;
  private static final String TAG = DebugUtils.class.getSimpleName();
  private DebugUtils(){}

  private static long getCurrentMem() {
    return Runtime.getRuntime().totalMemory() / 1000;
  }

  public static void logCurrentMem() {
    if (DEBUG) {
      Log.d(TAG+"#mem: ", getCurrentMem()+" kb");
    }
  }

  public static void logCurrentMem(String aheadMsg) {
    if (DEBUG) {
      Log.d(TAG+"#mem: ", aheadMsg +"/"+ getCurrentMem()+" kb");
    }
  }

  public static void logCurrentTime() {
    if (DEBUG) {
      Log.d(TAG+"#time: ",System.currentTimeMillis()+" ms");
    }
  }

  public static void logCurrentTime(String aheadMsg) {
    if (DEBUG) {
      Log.d(TAG+"#time: ",aheadMsg+"/"+System.currentTimeMillis()+" ms");
    }
  }
}
