package com.cylee.androidlib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.Set;

/**
 * Created by air on 15/3/7.
 */
public class Settings {
  private static SharedPreferences mPreference;

  private Settings() {
  }

  public static void init(Context context) {
    if (context == null) {
      throw new RuntimeException("context must not be null!");
    }
    mPreference = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
  }

  public static int getInt(String key) {
    return mPreference.getInt(key, 0);
  }

  public static int getInt(String key, int def) {
    return mPreference.getInt(key, def);
  }

  public static boolean getBoolean(String key) {
    return mPreference.getBoolean(key, false);
  }

  public static boolean getBoolean(String key, boolean def) {
    return mPreference.getBoolean(key, def);
  }

  public static long getLong(String key) {
    return mPreference.getLong(key, 0);
  }

  public static long getLong(String key, long def) {
    return mPreference.getLong(key, def);
  }

  public static String getString(String key) {
    return mPreference.getString(key, "");
  }

  public static String getString(String key, String def) {
    return mPreference.getString(key, def);
  }

  public static void putInt(String key, int value) {
    putInt(key, value, false);
  }

  public static void putInt(String key, int value, boolean immediately) {
    if (immediately) {
      mPreference.edit().putInt(key, value).commit();
    } else {
      mPreference.edit().putInt(key, value).apply();
    }
  }

  public static void putLong(String key, long value) {
    putLong(key, value, false);
  }

  public static void putLong(String key, long value, boolean immediately) {
    if (immediately) {
      mPreference.edit().putLong(key, value).commit();
    } else {
      mPreference.edit().putLong(key, value).apply();
    }
  }

  public static void putBoolean(String key, boolean value) {
    putBoolean(key, value, false);
  }

  public static void putBoolean(String key, boolean value, boolean immediately) {
    if (immediately) {
      mPreference.edit().putBoolean(key, value).commit();
    } else {
      mPreference.edit().putBoolean(key, value).apply();
    }
  }

  public static void putString(String key, String value) {
    putString(key, value, false);
  }

  public static void putString(String key, String value, boolean immediately) {
    if (immediately) {
      mPreference.edit().putString(key, value).commit();
    } else {
      mPreference.edit().putString(key, value).apply();
    }
  }

  public static void putStringSet(String newAppsListKey, Set<String> savedNewApps) {
    mPreference.edit().putStringSet(newAppsListKey, savedNewApps).apply();
  }

  public static Set<String> getStringSet(String newAppsListKey, Set<String> newApps) {
    return mPreference.getStringSet(newAppsListKey,newApps);
  }

  public static boolean contains(String key) {
    return mPreference.contains(key);
  }
}
