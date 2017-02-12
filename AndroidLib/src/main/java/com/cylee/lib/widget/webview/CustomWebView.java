package com.cylee.lib.widget.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cylee.lib.R;

public class CustomWebView extends AbsDownloadWebView {
	private OnWebViewEventListener mEventListener; //事件监听器
	private boolean mIsErrorPage; //是否错误页面
	private int mPageStartCount; //是否第一次调用onPageStarted；如果遇到错误页面，会在onPageFinished之前再调用一次onPageStarted，这时候需要特殊处理
	private ILoadUrlProcesser mLoadProcesser;

	private int mCacheMode ;
	public CustomWebView(Context context) {
		super(context);
	}
	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 设置是否显示图片
	 * @param isShowImage
	 */
	public void setShowImage(boolean isShowImage) {
		getSettings().setBlockNetworkImage(!isShowImage);
	}

	public void setLoadUrlProcesser(ILoadUrlProcesser processer) {
		mLoadProcesser = processer;
	}

	/**
	 * 设置缓存的级别
	 * @param mode
	 */
	public void setCacheMode(int mode) {
		mCacheMode = mode;
	}
	
	@Override
	public void loadUrl(String url) {
		mIsErrorPage = false;
		initCacheMode();
		super.loadUrl(url);
	}
	
	/**
	 * 初始化缓存
	 */
	private void initCacheMode() {
		int mode = 0;
		switch (mCacheMode) {
		case WebSettings.LOAD_NO_CACHE:
			mode = WebSettings.LOAD_NO_CACHE;
			break;
		case WebSettings.LOAD_CACHE_ELSE_NETWORK:
			mode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
			break;
		default:
			mode = WebSettings.LOAD_DEFAULT;
			break;
		}
		try{
			getSettings().setCacheMode(mode);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void reload() {
		mIsErrorPage = false;
		super.reload();
	}

	/**
	 * 设置定制特性，注册Javascript
	 */
	public void init(OnWebViewEventListener eventListener) {
		mEventListener = eventListener;
		
		mPageStartCount = 0;
		mIsErrorPage = false;
		
		initJavaScript();
		
		//不显示滚动条白色底图
//		setScrollbarFadingEnabled(true);
		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); 
		//使用setWebViewClient进行页面请求，响应，渲染等的定制
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return CustomWebView.this.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (mPageStartCount < 0) { //liuwp 20120905 这里不应该小于0
					mPageStartCount = 0;
				}
				mPageStartCount += 1;
				cookie(url);
				mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_START, url);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				String title = view.getTitle();
				mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_RECV_TITLE, title);
				mPageStartCount -= 1;
				
				if (!mIsErrorPage) {
					mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_HIDE_ERROR, url);
				}
				if (mIsErrorPage && mPageStartCount <= 0) {
					//错误消息最后发出，以免提前显示出来的错误页面覆盖改写的错误页面
					mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_ERROR, Integer.valueOf(-1));
				}
				mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_FINISH, url);
				super.onPageFinished(view, url);
				
				Log.e("LOG", "page Finish:"+mPageStartCount);
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_LOAD_RESOURCE, url);
				if (url.indexOf(".jpg") > 0 || url.indexOf(".png") > 0) {
					mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_HIDE_PROGRESS, view.getUrl());
				}
				super.onLoadResource(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				mIsErrorPage = true; //错误页面
				
				if (mPageStartCount == 0) {
					mPageStartCount = 2;
				}	
				if (mPageStartCount > 0) {
					mPageStartCount -= 1;
				}
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}
			
		});
		
		//使用setWebChromeClient进行UI交互的定制
		this.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				
				mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_CHANGE_PROGRESS, newProgress*5/3);
				
				Log.e("LOG", "percent:"+newProgress);
				if (newProgress > 40) {
//					hideProgress();
					mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_HIDE_PROGRESS, view.getUrl());
				}
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				if (mIsErrorPage) {
					title = getResources().getString(R.string.tip_online_internet_error);
				}
                unregisterDownloadJS();
				mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_RECV_TITLE, title);
				super.onReceivedTitle(view, title);
			}
		});
	}

	protected boolean shouldOverrideUrlLoading(WebView view, String url) {
		//url需要预处理在这进行

		if (mLoadProcesser != null && mLoadProcesser.processLoadUrl(view, url)) {
			return true;
		}

		view.loadUrl(url);
		mEventListener.onWebViewEvent(CustomWebView.this, OnWebViewEventListener.EVENT_ON_LOAD_URL, url);
		return true;
	}

	protected void initJavaScript() {
		//注册javascript调用
		Context c = getContext();
//		//解决在某些android系统中activityGroup与webView中的tts冲突
//		if( c!= null&& c instanceof ActivityOnline){
//			ActivityOnline ac = (ActivityOnline)c;
//			if(ac.getParent()!=null){
//				Util.setAccessibilityManagerDisable();
//			}
//		}
		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT); //优先使用缓存
		settings.setDomStorageEnabled(true);
		// 用于解决页面中密码保存提示的问题
		settings.setSavePassword(false);
		settings.setSaveFormData(false);
		settings.setAppCacheEnabled(true);
		settings.setAppCacheMaxSize(50*1024*1024);
		settings.setSupportZoom(true);
	}
	

	private void cookie(String url) {
		try {
			CookieSyncManager.createInstance(getContext());
	        CookieManager cookieManager = CookieManager.getInstance();
	        String cookie = cookieManager.getCookie(url);
	        cookieManager.setCookie(url, cookie);
	        CookieSyncManager.getInstance().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public interface ILoadUrlProcesser {
		boolean processLoadUrl(WebView view, String url);
	}
}
