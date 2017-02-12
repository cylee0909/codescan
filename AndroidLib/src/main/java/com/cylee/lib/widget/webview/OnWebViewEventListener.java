package com.cylee.lib.widget.webview;

/**
 * WebView事件监听器
 * @author ydyd
 *
 */
public interface OnWebViewEventListener {
	public static final int EVENT_ON_HIDE_ERROR = 6;
	
	/** 错误，data为错误String */
	public static final int EVENT_ON_ERROR = 0;
	
	/** 加载页面，data为页面URL */
	public static final int EVENT_ON_START = 1;
	
	/** 加载页面所包含的资源（css，javascript，图片等），data为资源URL */
	public static final int EVENT_ON_LOAD_RESOURCE = 2;
	
	/** 页面加载完成，data为页面URL */
	public static final int EVENT_ON_FINISH = 3;
	
	/** 收到页面标题，data为标题 */
	public static final int EVENT_ON_RECV_TITLE = 4;
	
	/** 页面加载到了隐藏进度框的阶段 */
	public static final int EVENT_ON_HIDE_PROGRESS = 5;
	
	/** 加载的进度改变了 */
	public static final int EVENT_ON_CHANGE_PROGRESS = 7;
	
	/** 跳转URL */
	public static final int EVENT_ON_LOAD_URL = 8;
	
	/**
	 * 事件处理
	 * @param webView 当前CustomWebView
	 * @param eventType 事件ID
	 * @param data 事件数据，不同的事件ID对应不同的数据
	 */
	public void onWebViewEvent(CustomWebView webView, int eventType, Object data);
}
