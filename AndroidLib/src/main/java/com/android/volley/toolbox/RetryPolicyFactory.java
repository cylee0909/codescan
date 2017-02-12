package com.android.volley.toolbox;


import com.android.volley.DefaultRetryPolicy;
import com.cylee.androidlib.util.NetUtils;

/**
 * 各种场景下网络重试策略的定义
 */
public class RetryPolicyFactory{
    /**************************WIFI网络下的重试策略定义 ********************/
    /**
     * 普通请求socket超时
     */
    private static final int TIMEOUT_NORMAL_WIFI = 10000;
    /**
     * 图片请求socket超时
     */
    private static final int TIMEOUT_IMAGE_WIFI = 10000;
    /**
     * MULTIPART请求超时
     */
    private static final int TIMEOUT_MULTIPART_WIFI = 30000;
    /**
     * 下载文件请求超时
     */
    private static final int TIMEOUT_DOWNLOAD_WIFI = 30000;

    /**
     * wifi网络请求重试次数
     */
    private static final int MAX_RETRIES_WIFI = 0;

    /** 请求backoff multiplier 失败后超时时间的乘数 */

    private static final float BACKOFF_MULT_WIFI = 1f;

    /**************************移动网路下的重试策略定义 ********************/
    /**
     * 普通请求socket超时
     */
    private static final int TIMEOUT_NORMAL_MOBILE = 20000;
    /**
     * 图片请求socket超时
     */
    private static final int TIMEOUT_IMAGE_MOBILE = 20000;
    /**
     * MULTIPART请求超时
     */
    private static final int TIMEOUT_MULTIPART_MOBILE = 30000;
    /**
     * 下载文件请求超时
     */
    private static final int TIMEOUT_DOWNLOAD_MOBILE = 30000;
    /**
     * 请求重试次数
     */
    private static final int MAX_RETRIES_MOBILE = 0;
    /**
     * 请求backoff multiplier 失败后超时时间的乘数
     */
    private static final float BACKOFF_MULT_MOBILE = 1f;


    public enum RETRY_POLICY{
        NORMAL,
        IMAGE,
        MULTIPART,
        DOWNLOAD
    }
    public static DefaultRetryPolicy getRetryPolicy(RETRY_POLICY policy){
        boolean isWifi = NetUtils.isWifiConnected();
        int timeout,retries;
        float backoff;
        switch (policy){
            case NORMAL:
                if(isWifi){
                    timeout = TIMEOUT_NORMAL_WIFI;
                    retries = MAX_RETRIES_WIFI;
                    backoff = BACKOFF_MULT_WIFI;
                }else{
                    timeout = TIMEOUT_NORMAL_MOBILE;
                    retries = MAX_RETRIES_MOBILE;
                    backoff = BACKOFF_MULT_MOBILE;
                }
                break;
            case IMAGE:
                if(isWifi){
                    timeout = TIMEOUT_IMAGE_WIFI;
                    retries = MAX_RETRIES_WIFI;
                    backoff = BACKOFF_MULT_WIFI;
                }else{
                    timeout = TIMEOUT_IMAGE_MOBILE;
                    retries = MAX_RETRIES_MOBILE;
                    backoff = BACKOFF_MULT_MOBILE;
                }
                break;
            case MULTIPART:
                if(isWifi){
                    timeout = TIMEOUT_MULTIPART_WIFI;
                    retries = MAX_RETRIES_WIFI;
                    backoff = BACKOFF_MULT_WIFI;
                }else{
                    timeout = TIMEOUT_MULTIPART_MOBILE;
                    retries = MAX_RETRIES_MOBILE;
                    backoff = BACKOFF_MULT_MOBILE;
                }
                break;
            case DOWNLOAD:
                if(isWifi){
                    timeout = TIMEOUT_DOWNLOAD_WIFI;
                    retries = MAX_RETRIES_WIFI;
                    backoff = BACKOFF_MULT_WIFI;
                }else{
                    timeout = TIMEOUT_DOWNLOAD_MOBILE;
                    retries = MAX_RETRIES_MOBILE;
                    backoff = BACKOFF_MULT_MOBILE;
                }
                break;
            default:
                timeout = isWifi?TIMEOUT_NORMAL_WIFI:TIMEOUT_NORMAL_MOBILE;
                retries = isWifi?MAX_RETRIES_WIFI:MAX_RETRIES_MOBILE;
                backoff = isWifi?BACKOFF_MULT_WIFI:BACKOFF_MULT_MOBILE;
        }
        return new DefaultRetryPolicy(timeout, retries, backoff);
    }
}
