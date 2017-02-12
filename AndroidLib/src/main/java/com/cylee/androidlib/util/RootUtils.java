package com.cylee.androidlib.util;

import java.io.File;

public class RootUtils {

  private static String LOG_TAG = RootUtils.class.getName();

  public static boolean isDeviceRooted() {
    return checkRootMethod1() || checkRootMethod2() || checkRootMethod3() || checkRootMethod4();
  }

  public static boolean checkRootMethod1() {
    String buildTags = android.os.Build.TAGS;
    return buildTags != null && buildTags.contains("test-keys");
  }

  public static boolean checkRootMethod2() {
    try {
      File file = new File("/system/app/Superuser.apk");
      return file.exists();
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean checkRootMethod3() {
    return new ShellUtils().executeCommand(ShellUtils.SHELL_CMD.check_su_binary) != null;
  }

  private static boolean checkRootMethod4() {
    return findBinary("su");
  }

  public static boolean findBinary(String binaryName) {
    boolean found = false;
    if (!found) {
      String[] places = {
          "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/",
          "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"
      };
      for (String where : places) {
        if (new File(where + binaryName).exists()) {
          found = true;
          break;
        }
      }
    }
    return found;
  }
}
