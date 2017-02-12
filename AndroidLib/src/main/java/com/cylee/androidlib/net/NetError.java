package com.cylee.androidlib.net;

import com.android.volley.ErrorCode;

/**
 * Created by xiaoqiang on 6/15/15.
 */
public class NetError extends Exception {
    private ErrorCode errorCode;
    public NetError(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public NetError(ErrorCode errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errorCode = errorCode;
    }

    public NetError(ErrorCode errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return String.format("错误码[%s],详细信息[%s]",errorCode.toString(),super.toString());
    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }
}
