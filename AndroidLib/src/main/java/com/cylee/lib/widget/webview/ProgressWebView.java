package com.cylee.lib.widget.webview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cylee.lib.R;

/**
 * Created by lichongyang on 2014/12/5.
 */
public class ProgressWebView extends RelativeLayout implements OnWebViewEventListener {
    protected View mErrorView = null; //自定义错误提示view
    protected ProgressBarOnline mProgressBar;
    protected CustomWebView mWebView;
    protected PullToRefreshBase mPullToRefresh;

    private OnWebViewEventListener mWebListener;
    private Context mContext;

    private int mProgressIndex = 0;
    private int mRealProgressIndex = 0;
    private boolean mShowProgressBar;

    public ProgressWebView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    protected void initWidgets() {
        //        下拉刷新控件
        mPullToRefresh = new PullToRefreshBase(mContext);
        LayoutParams pullParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        addView(mPullToRefresh, pullParams);

        //进度条
        mProgressBar = new ProgressBarOnline(mContext);
        LayoutParams progressParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        progressParams.addRule(ALIGN_PARENT_TOP, TRUE);
        mProgressBar.setVisibility(GONE);
        addView(mProgressBar, progressParams);

        FrameLayout webViewContainer = new FrameLayout(mContext);
        LinearLayout.LayoutParams webViewContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                .FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        mPullToRefresh.addView(webViewContainer, webViewContainerParams);

        // 为了兼容 ActivityOnlinBase#refreshWebIfInError方法，为WebView添加父控件
        mWebView = new CustomWebView(mContext);
        FrameLayout.LayoutParams webViewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT
        );
        webViewContainer.addView(mWebView, webViewParams);
    }

    private void init() {
        initWidgets();
        mShowProgressBar = true;
        mPullToRefresh.setRefreshableView(mWebView);
        mPullToRefresh.setBackgroundColor(Color.WHITE);

        mPullToRefresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.loadUrl(mWebView.getOriginalUrl());
            }
        });

        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setShowImage(true);
        mWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        mWebView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        mWebView.init(this);
    }

    public void setWebListener(OnWebViewEventListener webListener) {
        mWebListener = webListener;
    }

    public CustomWebView getWebView() {
        return mWebView;
    }

    /**
     * 显示自定义错误提示页面，用一个View覆盖在WebView
     */
    protected void showErrorPage() {
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacks(errorPageRunnable);
        }
        ViewGroup webParentView = (ViewGroup) mWebView.getParent();
        if(webParentView.getChildCount() > 1) return;
        initErrorPage();
        webParentView.addView(mErrorView);
        webParentView.postInvalidate();
    }

    protected void hideErrorPage() {
        Handler handler = getHandler();
        if (handler != null) {
            handler.postDelayed(errorPageRunnable, 200);
        }
    }

    protected void initErrorPage() {
        if (mErrorView == null) {
            mErrorView = View.inflate(mContext, R.layout.online_error, null);
            Button button = (Button)mErrorView.findViewById(R.id.online_error_btn_retry);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mWebView.stopLoading();
                    mWebView.reload();
                }
            });
        }
    }

    private Runnable errorPageRunnable = new Runnable() {
        @Override
        public void run() {
            ViewGroup webParentView = (ViewGroup) mWebView.getParent();
            if (webParentView.getChildCount() > 1) {
                webParentView.removeView(mErrorView);
            }
            webParentView.postInvalidate();
        }
    };

    @Override
    public void onWebViewEvent(CustomWebView webView, int eventType, Object data) {
        switch (eventType) {
            case OnWebViewEventListener.EVENT_ON_HIDE_ERROR:
                hideErrorPage();
                return;
            case OnWebViewEventListener.EVENT_ON_LOAD_URL:
                if(mShowProgressBar && mProgressBar != null&& !mProgressBar.isShown()){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                startChangeProgress();
                return;
            case OnWebViewEventListener.EVENT_ON_START:
                if(mShowProgressBar && mProgressBar != null&& !mProgressBar.isShown()){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                startChangeProgress();
                return;
            case OnWebViewEventListener.EVENT_ON_CHANGE_PROGRESS:
                final int progress = (Integer)data;
                if(mProgressBar!=null){
                    if(progress >= 100){
                        mRealProgressIndex = 100;
                        mProgressBar.setProgress(mRealProgressIndex);
                        mProgressBar.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopChangeProgress();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        }, 500);
						mPullToRefresh.onRefreshComplete();
                    }else{
                        mRealProgressIndex = progress;
                    }
                }
                return;
            case OnWebViewEventListener.EVENT_ON_FINISH:
                if(mProgressBar != null&& mProgressBar.isShown()){
                    mRealProgressIndex = 100;
                    mProgressBar.setProgress(mRealProgressIndex);
                    mProgressBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopChangeProgress();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }, 500);
                }
                return;
            case OnWebViewEventListener.EVENT_ON_ERROR:
                if(mProgressBar != null&& mProgressBar.isShown()){
                    mRealProgressIndex = 100;
                    mProgressBar.setProgress(mRealProgressIndex);
                    mProgressBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopChangeProgress();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }, 500);
                }
                showErrorPage();
                return;
            case OnWebViewEventListener.EVENT_ON_RECV_TITLE:
                break;
        }

        if (mWebListener != null) {
            mWebListener.onWebViewEvent(webView, eventType, data);
        }
    }

    public void clearWebView() {
        mWebView.clearView();
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    /**
     * 设置缓存的级别
     * @param mode
     */
    public void setCacheMode(int mode) {
        mWebView.setCacheMode(mode);
    }

    private void startChangeProgress(){
        mProgressIndex = 20;
        mRealProgressIndex = 0;
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacks(changeProgressRunable);
            handler.post(changeProgressRunable);
        }
    }

    public void stopChangeProgress(){
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacks(changeProgressRunable);
        }
        mProgressIndex = 20;
        mRealProgressIndex = 0;
    }

    public void setShouldShowProgressBar(boolean show) {
        mShowProgressBar = show;
        if (mProgressBar == null) {
            if (show && !mProgressBar.isShown()) {
                mProgressBar.setVisibility(VISIBLE);
            } else if (!show && mProgressBar.isShown()) {
                mProgressBar.setVisibility(GONE);
            }
        }
    }

    /**
     * 隐藏进度，并停止加载，如果调用时进度条是显示状态，则返回True
     * @return
     */
    public boolean hideLoadProgress(){
        mWebView.stopLoading();
        if(mProgressBar != null && mProgressBar.isShown()){
            mProgressBar.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private Runnable changeProgressRunable = new Runnable() {
        @Override
        public void run() {
            if(mProgressBar != null){
                int progress = mProgressIndex > mRealProgressIndex ? mProgressIndex:mRealProgressIndex;
                mProgressBar.setProgress(progress);
                if(progress < 80){
                    mProgressIndex++;
                    postDelayed(this, 25);
                }else if(progress < 95){
                    mProgressIndex++;
                    postDelayed(this, 100);
                }
            }
        }
    };
}
