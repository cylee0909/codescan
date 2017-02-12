package com.cylee.codescan

import com.cylee.androidlib.util.PreferenceUtils

/**
 * Created by cylee on 16/9/6.
 */
enum class CommonPreference private constructor(private val defaultValue: Any?) : PreferenceUtils.DefaultValueInterface {
    CODE_COLLECTION(null);

    override fun getDefaultValue(): Any? {
        return defaultValue
    }

    override fun getNameSpace(): String {
        return "CommonPreference"
    }
}
