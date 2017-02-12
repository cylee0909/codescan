package com.android.volley;

/**
 * Created by sn on 13-11-12.
 * 接口响应成功，但是响应结果错误(errNo != 0)
 */
public class ResponseContentError extends VolleyError {
    private ErrorCode errorCode;
    public ResponseContentError(ErrorCode errorCode){
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
