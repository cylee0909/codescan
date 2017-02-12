package com.cylee.androidlib.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Random;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

public class AndroidUtils {
  private static DisplayMetrics mDisplayMetrics;
  private static String mMD5IMEI;

  public static int dip2px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dipValue * scale + 0.5f);
  }

  public static int px2dp(Context context, float pxValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }


  public static int randomColor() {
    Random random = new Random();
    return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
  }

  /**
   * getAppVersion:获取当前版本code
   *
   * @param @param context
   * @return int
   * @throws
   */
  public static String getAppVersion(Context context) {
    PackageManager m = context.getPackageManager();
    String app_ver;
    try {
      app_ver = m.getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError();
    }
    return app_ver;
  }

  public static int getAppVersionCode(Context context) {
    PackageManager m = context.getPackageManager();
    int app_ver;
    try {
      app_ver = m.getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError();
    }
    return app_ver;
  }

  public int getStatusBarHeight(Context context) {
    int result = 0;
    int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = context.getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }

  public static int calculateMemoryCacheSize(Context context) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
    int memoryClass = am.getMemoryClass();
    if (largeHeap && SDK_INT >= HONEYCOMB) {
      memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
    }
    // Target ~15% of the available heap.
    return 1024 * 1024 * memoryClass / 7;
  }

  @TargetApi(HONEYCOMB)
  private static class ActivityManagerHoneycomb {
    static int getLargeMemoryClass(ActivityManager activityManager) {
      return activityManager.getLargeMemoryClass();
    }
  }

  public static DisplayMetrics getDisplayMetrics(Context context) {
    if (mDisplayMetrics == null) {
      mDisplayMetrics = context.getResources().getDisplayMetrics();
    }
    return mDisplayMetrics;
  }

  public static String getIMEI(Context context) {
    try {
      return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    } catch (Exception paramContext) {
    }
    return "";
  }

  public static String getMD5IMEI(Context context) {
    if (mMD5IMEI == null) {
      String imei = getIMEI(context);
      if (!TextUtils.isEmpty(imei)) {
        mMD5IMEI = TextUtil.md5(imei);
      }
    }
    return mMD5IMEI;
  }
}