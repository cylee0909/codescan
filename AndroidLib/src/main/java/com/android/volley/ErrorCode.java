package com.android.volley;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.SparseArray;

public class ErrorCode {
    private static final SparseArray<ErrorCode> errorCodes = new SparseArray<>();
    private final int errNo;
    private String info;
    private boolean isUnknown;

    @SuppressLint("DefaultLocale")
    private ErrorCode(int code, String errorInfo) {
        this.errNo = code;
        this.info = errorInfo;
        if (errorCodes.get(code) == null) {
            errorCodes.put(code, this);
        } else {
            //错误码有重复定义时抛出运行时异常
            throw new RuntimeException(String.format("错误码定义有重复 %d:%s", code, errorInfo));
        }
    }

    public String toString() {
        return errNo + ":" + info;
    }

    /**
     * 返回错误码
     *
     * @return
     */
    public int getErrorNo() {
        return errNo;
    }

    /**
     * 是否在客户端本地找不到对应的错误码
     *
     * @return
     */
    public boolean isUnknown() {
        return isUnknown;
    }

    /**
     * 返回错误信息描述
     *
     * @return
     */
    public String getErrorInfo() {
        return info;
    }

    public static final ErrorCode NETWORK_ERROR = new ErrorCode(2,"网络繁忙");

    /*客户端产生的错误码*/
    public static final ErrorCode CLIENT_NET_EXCEPTION = new ErrorCode(-100680001, "网络异常");
    public static final ErrorCode CLIENT_PARSE_EXCEPTION = new ErrorCode(-100680002, "数据解析异常");
    public static final ErrorCode CLIENT_TIMEOUT_EXCEPTION = new ErrorCode(-100680003, "响应超时");
    public static final ErrorCode CLIENT_URL_INVALID_EXCEPTION = new ErrorCode(-100680004, "URL非法");
    public static final ErrorCode CLIENT_PB_PARSE_EXCEPTION = new ErrorCode(-100680005, "数据解析异常");
    public static final ErrorCode CLIENT_AUTH_EXCEPTION = new ErrorCode(-100680006, "身份验证异常");
    public static final ErrorCode CLIENT_OOM_EXCEPTION = new ErrorCode(-100680007, "网络异常");
    public static final ErrorCode CLIENT_SERVER_EXCEPTION = new ErrorCode(-100680008, "服务异常");
    public static final ErrorCode CLIENT_SSL_EXCEPTION = new ErrorCode(-100680009, "SSL连接异常");
    public static final ErrorCode CLIENT_UNKNOWN_EXCEPTION = new ErrorCode(-100680010, "未知异常");

    public static ErrorCode valueOf(int code, String errStr) {
        ErrorCode errorCode = errorCodes.get(code);
        if (errorCode == null) {
            ErrorCode ec = new ErrorCode(code, TextUtils.isEmpty(errStr) ? "未知错误:" + code : errStr);
            ec.isUnknown = true;
        }
        return errorCode;
    }
}
