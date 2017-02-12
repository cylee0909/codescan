/**
 * 接口输入基类，所有接口必须实现该接口
 * */
package com.cylee.androidlib.net;

import com.android.volley.Request;

import java.util.Collections;
import java.util.Map;

public abstract class InputBase {
    public Map<String, Object> getParams() {
        return Collections.emptyMap();
    }

    public int method;
    public Class aClass;
    public String url;
    public boolean needCache;

    protected InputBase() {
        method = Request.Method.GET;
        aClass = this.getClass();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Config.getHost());
        builder.append(url).append("?");
        return builder.toString();
    }
}