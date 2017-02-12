package com.cylee.androidlib.net;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.ErrorCode;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ResponseContentError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RetryPolicyFactory;
import com.cylee.androidlib.GsonBuilderFactory;
import com.cylee.androidlib.util.DirectoryManager;
import com.cylee.androidlib.util.FileUtils;
import com.cylee.androidlib.util.TextUtil;
import com.google.jtm.Gson;
import com.google.jtm.JsonSyntaxException;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 作业帮底层网络请求request对象
 * 继承至volley的request，做参数准备和GSON解析
 */
public class HWRequest<T> extends Request<T> {
    private static Random RANDOM = new Random();
    private long mRequestID;
    private String mSearchString = null;
    private String mParams;
    private final Type mClazz;
    private final File mFile;
    private final byte[] mFileBytes;
    private final String mFileName;
    private final Response.Listener<T> mListener;
    private MultipartEntity mEntity;
    private Map<String, String> mHeaderParams;
    //当前Request携带的Cookie
    private List<String> cookies;

    private static final String ERROR_NO_0_EXPRESSION = "[\\S\\s]*\"err[Nn]o\"\\s*:\\s*0[\\s\\S]*";
    /**
     * 创建一个普通的数据请求
     * @param input
     * @param successListener
     * @param errorListener
     * @return
     */
    public static <T>HWRequest<T> newRequest(InputBase input, Response.Listener<T> successListener, Response.ErrorListener errorListener) {
        HWRequest<T> request = new HWRequest<T>(input, null, null, null, successListener, errorListener);
        request.setRetryPolicy(RetryPolicyFactory.getRetryPolicy(RetryPolicyFactory.RETRY_POLICY.NORMAL));
        return request;
    }


    /**
     * 创建一个普通的文件提交请求
     * @param input
     * @param filename
     * @param file
     * @param successListener
     * @param errorListener
     * @param <T>
     * @return
     */
    public static <T>HWRequest<T> newFileRequest(InputBase input, String filename, File file, Response.Listener<T> successListener, Response.ErrorListener errorListener) {
        HWRequest<T> request = new HWRequest<T>(input, filename, file, null, successListener, errorListener);
        request.setRetryPolicy(RetryPolicyFactory.getRetryPolicy(RetryPolicyFactory.RETRY_POLICY.MULTIPART));
        return request;
    }

    /**
     * 创建一个byte array数据流提交请求
     * @param input
     * @param filename
     * @param fileBytes
     * @param successListener
     * @param errorListener
     * @param <T>
     * @return
     */
    public static <T>HWRequest<T> newByteRequest(InputBase input, String filename, byte[] fileBytes, Response.Listener<T> successListener, Response.ErrorListener errorListener) {
        HWRequest<T> request = new HWRequest<T>(input, filename, null, fileBytes, successListener, errorListener);
        request.setRetryPolicy(RetryPolicyFactory.getRetryPolicy(RetryPolicyFactory.RETRY_POLICY.MULTIPART));
        return request;
    }


    /**
     * 构造函数，创建一个request对象
     * @param input
     * @param filename
     * @param file
     * @param fileBytes
     * @param successListener
     * @param errorListener
     */
    private HWRequest(InputBase input, String filename, File file, byte[] fileBytes,
                      Response.Listener<T> successListener, Response.ErrorListener errorListener) {
        super(Method.POST, Config.getHost() + input.url, errorListener);
        mSearchString = input.url;
        mClazz = input.aClass;
        mFileName = filename;
        mFile = file;
        mFileBytes = fileBytes;
        mListener = successListener;
        mRequestID = Math.abs(RANDOM.nextInt()) + 1;
    }


    @Override
    public Response<T> parseNetworkResponse(NetworkResponse response) {
        Response<T> result;
        try {
            if (response.data != null) {
                    String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    if (mClazz == String.class) {
                        return Response.success((T) json, HttpHeaderParser.parseCacheHeaders(response));
                    } else if (mClazz == File.class) {
                        String fileName = TextUtil.md5(getUrl());
                        File outFile = new File(DirectoryManager.getDirectory(DirectoryManager.DIR.DATA), fileName);
                        FileUtils.writeFile(outFile.getAbsolutePath(), response.data);
                        return Response.success((T) outFile, HttpHeaderParser.parseCacheHeaders(response));
                    } else {
                        if (json.matches(ERROR_NO_0_EXPRESSION)) {  // errno=0
                            T data;
                            JSONObject jsonObject = new JSONObject(json);
                            json = jsonObject.getString("data");
                            if (json.trim().startsWith("[")) {
                                json = "{}";
                            }
                            //JELLY_BEAN以下的设备不支持emoji表情，过滤掉，防止一些山寨机crash
                            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                json = TextUtil.filterEmoji(json);
                            }
                            //其他属于Gson请求，解析对象返回
                            Gson gson = GsonBuilderFactory.createBuilder();
                            data = (T) gson.fromJson(json, mClazz);
                            return Response.success(data, HttpHeaderParser.parseCacheHeaders(response));
                        } else {
                            int errNo;
                            JSONObject jsonObject = new JSONObject(json);
                            if (jsonObject.has("errNo")) {
                                errNo = jsonObject.getInt("errNo");
                                ErrorCode errorCode;
                                errorCode = ErrorCode.valueOf(errNo,jsonObject.optString("errstr"));
                                return Response.error(new ResponseContentError(errorCode));
                            } else {
                                return Response.error(new ParseError("Error response format: errNo not found"));
                            }
                        }
                }
            } else {
                result = Response.error(new ResponseContentError(ErrorCode.NETWORK_ERROR));
            }
        } catch (UnsupportedEncodingException e) {
            result = Response.error(new ParseError(e));
        } catch (JSONException|JsonSyntaxException e) {
            result = Response.error(new ParseError(e));
        }

        return result;
    }

    @Override
    protected void deliverResponse(T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public void setUrl(String url) throws IllegalStateException {
        int mark = url.indexOf("?");
        if(!isUploadFile()){
            if (mark != -1) {
                mParams = url.substring(mark+1);
            }
        }
        else{
            mEntity = new MultipartEntity();
            try {
                if (mFile != null) { mEntity.addPart(mFileName, new FileBody(mFile)); }
                if (mFileBytes != null) { mEntity.addPart(mFileName, new ByteArrayBody(mFileBytes, "image.jpg")); }
                // todo 可以通过net里头进行参数传递来优化
                List<NameValuePair> queryParams = URLEncodedUtils.parse(URI.create(url), "UTF-8");
                for (NameValuePair keyValue : queryParams) {
                    String value = keyValue.getValue();
                    if (TextUtils.isEmpty(value)) {
                        value = "";
                    }
                    mEntity.addPart(keyValue.getName(), new StringBody(value, Charset.forName("UTF-8")));
                }
            } catch (Exception e) {
            }
        }
        url = mark == -1 ? url : url.substring(0, mark);
        super.setUrl(url);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mHeaderParams == null) {
            mHeaderParams = new HashMap<>();
        }
        mHeaderParams.put("X-Wap-Proxy-Cookie", "none");
        mHeaderParams.put("Cookie",getCookies());
        return mHeaderParams;
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }

    @Override
    public String getBodyContentType() {
        if (isUploadFile()) {
            return mEntity.getContentType().getValue();
        }
        return super.getBodyContentType();
    }

    public String getCookies(){
        if(cookies == null){
            cookies = new ArrayList<>();
        }
        cookies.add("requestId="+mRequestID);
        return TextUtils.join("; ",cookies);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] body = null;
        if (!isUploadFile()) {
            try {
                body =  mParams == null? null : mParams.getBytes(getParamsEncoding());
            } catch (UnsupportedEncodingException e) {
            }
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                mEntity.writeTo(bos);
                bos.close();
                body =  bos.toByteArray();
            } catch (IOException e) {
            } catch (OutOfMemoryError e) {
            }
        }
        logLong(Request.LOG_SEND_SIZE,body != null ? body.length : 0);
        return body;
    }


    /**
     * 获取接口类名称
     * @return
     */
    public Type getClassName() {
        return mClazz;
    }

    /**
     * url的search部分
     */
    public String getSearchString() {
        return mSearchString;
    }

    /**
     * 是否上传文件
     * @return
     */
    public boolean isUploadFile() {
        return mFile != null || mFileBytes != null;
    }

}
