package com.cylee.lib.widget.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylee.lib.R;

public class ProgressBarOnline extends RelativeLayout{
	private static int HIGHTLIGHT_DELAY_TIME = 1000;//亮光之间的间隔为1000毫秒
	private static int DEFAULT_REFRESH_TIME = 10;//默认光亮刷新时间为10毫秒，即最高100帧的刷新率，保证不闪烁
	private static int MAX_SPEED = 20;//亮光最快运行速度为每10毫秒20个像素
	private static int MIN_SPEED = 15;//亮光最慢运行速度为每10毫秒15个像素
	private LayoutInflater 	mLayoutInflater;
	DisplayMetrics outMetrics = null;
	private int mProgress = 0;
	private Handler mHandler;
	private ImageView mProgressImg;
	private ImageView mHighlight;
	private Runnable highlight = new  Runnable() {
		public void run() {
			int time = DEFAULT_REFRESH_TIME;
			int currentSpeed = MAX_SPEED*(outMetrics.widthPixels+ mHighlight.getScrollX())/outMetrics.widthPixels;
			int deltX = MIN_SPEED > currentSpeed? MIN_SPEED : currentSpeed;
			
			if(mHighlight.getScrollX()-deltX > -outMetrics.widthPixels *(mProgress-5)/100){
				mHighlight.scrollBy(-deltX, 0);
			}else{
				mHighlight.scrollTo(mHighlight.getMeasuredWidth(), 0);
				time = HIGHTLIGHT_DELAY_TIME;
			}
			postInvalidate();
			mHandler.postDelayed(highlight, time);
		}
	};
	
	public ProgressBarOnline(Context context) {
		super(context);
		init();
	}
	public ProgressBarOnline(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public ProgressBarOnline(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		outMetrics 	= getContext().getResources().getDisplayMetrics();
		mHandler = new Handler();
		mLayoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutInflater.inflate(R.layout.online_progress_layout, this);
		mProgressImg = (ImageView)findViewById(R.id.online_progress_img);
		mHighlight = (ImageView)findViewById(R.id.online_progress_highlight);
	}
	public void setProgress(int progress){
		mProgress = progress;
		mProgressImg.scrollTo(outMetrics.widthPixels *(100-mProgress)/100, 0);
		postInvalidate();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if(!mHighlight.isShown()){
			mHandler.removeCallbacks(highlight);
		}
		super.dispatchDraw(canvas);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mHighlight.scrollTo(mHighlight.getMeasuredWidth(), 0);
	}
	@Override
	public void setVisibility(int visibility) {
		if(visibility == View.VISIBLE){
			mHighlight.scrollTo(mHighlight.getMeasuredWidth(), 0);
			mHandler.removeCallbacks(highlight);
			mHandler.postDelayed(highlight,200);
		}else{
			mHandler.removeCallbacks(highlight);
		}
		super.setVisibility(visibility);
	}
}
