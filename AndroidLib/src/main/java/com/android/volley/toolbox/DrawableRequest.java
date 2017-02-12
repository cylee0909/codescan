/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import java.util.HashMap;
import java.util.Map;

/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Drawable
 */
public class DrawableRequest extends Request<Drawable> {
    interface DrawableCreator{
        /**
         * 将一个网络请求解析成对应的Drawable对象
         * @param response
         * @return
         */
        Drawable doParse(NetworkResponse response);

        /**
         * 返回一个唯一对应这个Creator的一个Id
         * @return
         */
        String getId();
    }
    private final Response.Listener<Drawable> mListener;
    private DrawableCreator mCreator;
    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    private static final Object sDecodeLock = new Object();

    /**
     * Creates a new image request, decoding to a maximum specified width and
     * height. If both width and height are zero, the image will be decoded to
     * its natural size. If one of the two is nonzero, that dimension will be
     * clamped and the other one will be set to preserve the image's aspect
     * ratio. If both width and height are nonzero, the image will be decoded to
     * be fit in the rectangle of dimensions width x height while keeping its
     * aspect ratio.
     *
     * @param url URL of the image
     * @param listener Listener to receive the decoded bitmap
     * @param creator a transformer used to transform loaded bitmap into another(eg. round corner)
     * @param errorListener Error listener, or null to ignore errors
     */
    public DrawableRequest(String url, DrawableCreator creator, Response.Listener<Drawable> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
        mCreator = creator;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String,String> headerMap = new HashMap<String, String>();
        headerMap.put("X-Wap-Proxy-Cookie","none");
        return headerMap;
    }

    @Override
    protected Response<Drawable> parseNetworkResponse(NetworkResponse response) {
        // Serialize all decode on a global lock to reduce concurrent heap usage.
        synchronized (sDecodeLock) {
            try {
                return doParse(response);
            } catch (OutOfMemoryError e) {
                VolleyLog.e("Caught OOM for %d byte image, url=%s", response.data.length, getUrl());
                return Response.error(new ParseError(e));
            }
        }
    }

    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     */
    private Response<Drawable> doParse(NetworkResponse response) {
        Drawable drawable = mCreator.doParse(response);
        if (drawable == null) {
            return Response.error(new ParseError("Failed to create drawable"));
        } else {
            if(drawable instanceof BitmapDrawable){
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                logLong(Request.LOG_MEM,bitmap.getRowBytes() * bitmap.getHeight());
            }
            return Response.success(drawable, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected void deliverResponse(Drawable response) {
        mListener.onResponse(response);
    }
}
