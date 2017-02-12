package com.cylee.lib.widget.webview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 主要用于下载回调服务器的webView
 * <br>
 *     1.图书下载回调接口注册
 *     2.APK下载回调接口注册
 * </br>
 */
public abstract class AbsDownloadWebView extends WebView {
    /**
     * 是否启用图书下载状态回调服务器的开关
     */
    private boolean             mEnableDownloadBookJS;
    /**
     * 是否启用APK下载状态回调服务器的信息（包名，apk名称，函数名称）
     */
    private RegisterApkMethod   mRegisterApkMethod;

	public AbsDownloadWebView(Context context){
		super(context);
	}

	public AbsDownloadWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    /**
     * 是否启用图书下载回调
     * @return
     */
    public synchronized boolean isEnableDownloadBookJS() {
        return mEnableDownloadBookJS;
    }

    /**
     * 注册下载图书时回调开关的开启
     * @param isEnable ：true开启，false 关闭
     */
    public synchronized void registerDownloadBookJS(boolean isEnable) {
        mEnableDownloadBookJS = isEnable;
    }

    /**
     * 注册APK下载回调属性 注册RegisterApkMethod 回调的信息例如：包名，APK的文件名称，回调给PHP的函数名称
     * @param method @see AbsDownloadWebView.RegisterApkMethod
     */
    public synchronized void registerApkMethod(RegisterApkMethod method) {
        mRegisterApkMethod = method;
    }

    /**
     * 获取到注册APK回调的信息
     * @return
     */
    public synchronized RegisterApkMethod getRegisterApkMethod() {
        return mRegisterApkMethod;
    }

    /**
     * 是否启用回调信息
     * @return
     */
    public synchronized boolean isEnableDownloadApkJS() {
        return mRegisterApkMethod == null ? false : mRegisterApkMethod.canCallBack();
    }

    /**
     * 注销回调JS
     * <BR>
     *     1. 图书下载回调
     *     2. APK下载回调
     *     3. 注销主要用于ac的生命周期onDestroy/或者 WebView 加载获取到title的时候调用
     * </BR>
     */
    public synchronized void unregisterDownloadJS() {
        mEnableDownloadBookJS = false;
        mRegisterApkMethod = null;
    }
    /**
     * APK下载服务器注册的方法
     */
    public final static class RegisterApkMethod {
        public String mCallbackMethod ;    //回调PHP的方法名称
        public String mPackName ;          //回调服务器的参数 apk的包名
        public String mApkName ;           //回调服务器的参数 apk对应的apk名称

        public void onParser(JSONObject jsonData) {
            try {
                mCallbackMethod  = jsonData.getString("Callback");
                mPackName        = jsonData.optString("PackageName","");
                mApkName         = jsonData.optString("ApkName","");
            } catch (JSONException e){

            }
        }
        public boolean canCallBack() {
            return !TextUtils.isEmpty(mCallbackMethod);
        }
    }
}
