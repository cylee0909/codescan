package com.cylee.androidlib.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;

import com.cylee.androidlib.base.BaseApplication;
import com.cylee.androidlib.GsonBuilderFactory;
import com.google.jtm.Gson;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 1、保存基础类型(String、Boolean、Integer、Long、Float)的Key-Value
 * 	    说明：所有和SharedPreference相关的配置信息。(目的是统一操作SharedPreference,避免在UI线程中对SharedPreference进行文件读写操作)
 * 2、保存Model
 * 3、保存List<?>
 * @author chenmiao
 */
public class PreferenceUtils {
    private static ArrayMap<String,SharedPreferences> preferenceCache = new ArrayMap<>();
    public static <T extends Enum<T>> void addPreferenceChangeListener(final SharedPreferences.OnSharedPreferenceChangeListener listener, final T ... keyIndexes){
        for(T key : keyIndexes){
            SharedPreferences sharedPreferences = resolvePreference(key);
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }
    public static <T extends Enum<T>> void removePreferenceChangeListener(final SharedPreferences.OnSharedPreferenceChangeListener listener,final T ... keyIndexes){
        for(T key : keyIndexes){
            SharedPreferences sharedPreferences = resolvePreference(key);
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    /**
     * 通过key获取存储在Preference中的Key
     * @param keyIndex 要解析的key
     */
    public static <T extends Enum<T>> String getStorageKey(T keyIndex) {
        return keyIndex.name();
    }

    /**
     * 根据指定的key获取对应的SharePreference
     * @param keyIndex 指定的key
     */
    private static <T extends Enum<T>> SharedPreferences resolvePreference(T keyIndex){
        String namespace = ((DefaultValueInterface)keyIndex).getNameSpace();
        SharedPreferences preference = preferenceCache.get(namespace);
        if(preference == null){
            synchronized (PreferenceUtils.class) {
                if(preferenceCache.get(namespace) == null) {
                    Context context = BaseApplication.getApplication();
                    String pkgName = context.getPackageName();
                    preference = context.getSharedPreferences(pkgName+"." +
                            ((PreferenceUtils.DefaultValueInterface) keyIndex).getNameSpace(), Context.MODE_MULTI_PROCESS);
                    preferenceCache.put(namespace, preference);
                }else{
                    preference = preferenceCache.get(namespace);
                }
            }
        }
        return preference;
    }
    /**
     * get preference data
     * @param keyIndex
     * */
    public static <T extends Enum<T>> Long getLong(T keyIndex) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        Long value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            value = preferences.getLong(key, 0);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            Object defaultValue = defaultEnum.getDefaultValue();
            if (defaultValue != null) {
                value = (Long) defaultValue;
            } else {
                value = 0L;
            }
        }
        return value;
    }

    /**
     * @param keyIndex
     * @param value
     * */
    public static synchronized <T extends Enum<T>> void setLong(T keyIndex, long value) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        String key = getStorageKey(keyIndex);
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * @param keyIndex
     * */
    public static <T extends Enum<T>> int getInt(T keyIndex) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        Integer value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            value = preferences.getInt(key, 0);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            Object defaultValue = defaultEnum.getDefaultValue();
            if (defaultValue != null) {
                value = (Integer) defaultValue;
            } else {
                value = 0;
            }
        }
        return value;
    }

    /**
     * @param keyIndex
     * @param value
     * */
    public static synchronized <T extends Enum<T>> void setInt(T keyIndex, int value) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        String key = getStorageKey(keyIndex);
        editor.putInt(key, value);
        editor.apply();
    }


    /**
     * @param keyIndex
     * */
    public static <T extends Enum<T>> String getString(T keyIndex) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        String value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            value = preferences.getString(key, null);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            Object defaultValue = defaultEnum.getDefaultValue();
            if (defaultValue != null) {
                value = (String) defaultValue;
            } else {
                value = null;
            }
        }
        return value;
    }

    /**
     * @param keyIndex
     * @param value
     * */
    public static synchronized <T extends Enum<T>> void setString(T keyIndex, String value) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        String key = getStorageKey(keyIndex);
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * @param keyIndex
     * */
    public static <T extends Enum<T>> boolean getBoolean(T keyIndex) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        Boolean value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            value = preferences.getBoolean(key, false);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            Object defaultValue = defaultEnum.getDefaultValue();
            if (defaultEnum != null) {
                value = (Boolean) defaultValue;
            } else {
                value = false;
            }
        }
        return value;
    }


    public static <T extends Enum<T>> void setBoolean(T keyIndex, boolean value) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        String key = getStorageKey(keyIndex);
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static <T extends Enum<T>> float getFloat(T keyIndex) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        Float value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            value = preferences.getFloat(key, 0f);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            Object defaultValue = defaultEnum.getDefaultValue();
            if (defaultEnum != null) {
                value = (Float) defaultValue;
            } else {
                value = 0f;
            }
        }
        return value;
    }

    public static <T extends Enum<T>> void setFloat(T keyIndex, float value) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        String key = getStorageKey(keyIndex);
        editor.putFloat(key, value);
        editor.apply();
    }


    public static <T extends Enum<T>> Set<String> getStringSet(T keyIndex) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        Set<String> value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            value = preferences.getStringSet(key, null);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            Object defaultValue = defaultEnum.getDefaultValue();
            if (defaultEnum != null) {
                value = (Set<String>) defaultValue;
            } else {
                value = new LinkedHashSet<>();
            }
        }
        return value;
    }


    public static <T extends Enum<T>> void setStringSet(T keyIndex, Set<String> value) {
        SharedPreferences preferences = resolvePreference(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        String key = getStorageKey(keyIndex);
        editor.putStringSet(key, value);
        editor.apply();
    }
    /**
     * 保存Model（Model里面可以潜逃Model与List）
     * 说明：建议在线程中调用此函数
     * @param keyIndex
     * @param object
     * @return
     */
    public static <T extends Enum<T>> void setObject(T keyIndex, Object object){
        SharedPreferences preferences = resolvePreference(keyIndex);
        if(object == null){
            removeKey(keyIndex);
        }else {
            String jsonValue;
            Gson gson = GsonBuilderFactory.createBuilder();
            jsonValue = gson.toJson(object);
            String key = getStorageKey(keyIndex);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, jsonValue);
            editor.apply();
        }
    }

    /**
     * 通过key获取Model
     * 说明：建议在线程中调用此函数
     * @param keyIndex
     * @param clazz
     * @return
     */
    public static <E,T extends Enum<T>> E getObject(T keyIndex, Class<E> clazz){
        SharedPreferences preferences = resolvePreference(keyIndex);
        E value;
        String key = getStorageKey(keyIndex);
        if (preferences.contains(key)) {
            String jsonValue = preferences.getString(key, null);
            Gson gson = GsonBuilderFactory.createBuilder();
            value = gson.fromJson(jsonValue, clazz);
        } else {
            DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
            value = (E) defaultEnum.getDefaultValue();
        }
        return value;
    }
    /**
     * 删除掉一个key
     * @param keyIndex 要删掉的key
     * @return
     */
    public static <T extends Enum<T>> void removeKey(T keyIndex){
        SharedPreferences preferences = resolvePreference(keyIndex);
        String key = getStorageKey(keyIndex);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 判断Preference中是否存在对应的key
     * @param keyIndex
     * @return
     */
    public static <T extends Enum<T>> boolean hasKey(T keyIndex){
        SharedPreferences preferences = resolvePreference(keyIndex);
        String key = getStorageKey(keyIndex);
        return preferences.contains(key);
    }
    /**
     * 还原某一项为默认值
     * @param keyIndex
     * @param <T>
     */
    public static <T extends Enum<T>> void restoreToDefault(T keyIndex){
        DefaultValueInterface defaultEnum = (DefaultValueInterface) keyIndex;
        Object value = defaultEnum.getDefaultValue();
        if(value instanceof Boolean){
            setBoolean(keyIndex, (Boolean) value);
        }else if(value instanceof Integer){
            setInt(keyIndex, (Integer) value);
        }else if(value instanceof Long){
            setLong(keyIndex, (Long) value);
        }else if(value instanceof String){
            setString(keyIndex, (String) value);
        }else if(value instanceof Float){
            setFloat(keyIndex, (Float) value);
        }else if(value instanceof Set){
            setStringSet(keyIndex, (Set) value);
        }else{
            setObject(keyIndex,value);
        }
    }

    /**
     * 表示枚举变量中每个枚举值的默认值
     * 用于保存默认的share Preference 字段值
     */
    public interface DefaultValueInterface {
        Object getDefaultValue();
        String getNameSpace();
    }
}
