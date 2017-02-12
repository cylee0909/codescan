package com.cylee.lib.widget.webview;


import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylee.lib.R;


public class LoadingLayout extends FrameLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

	private final TextView headerText;

	private String pullLabel;
	private String refreshingLabel;
	private String releaseLabel;

	private ImageView mPullAnimPic;
	private AnimationDrawable animationDrawable;


	public LoadingLayout(Context context, final int mode, String releaseLabel, String pullLabel, String
			refreshingLabel) {
		super(context);
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
		headerText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		mPullAnimPic = (ImageView) header.findViewById(R.id.pull_anim_pic);

		this.releaseLabel = releaseLabel;
		this.pullLabel = pullLabel;
		this.refreshingLabel = refreshingLabel;
	}

	public void reset() {
		headerText.setText(pullLabel);
		endLoadAnim();
	}

	public void releaseToRefresh() {
		headerText.setText(releaseLabel);
	}

	public void setPullLabel(String pullLabel) {
		this.pullLabel = pullLabel;
	}

	public void refreshing() {
		headerText.setText(refreshingLabel);
		startLoadAnim();
	}

	public void setRefreshingLabel(String refreshingLabel) {
		this.refreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
		this.releaseLabel = releaseLabel;
	}

	public void pullToRefresh() {
		headerText.setText(pullLabel);
	}

	public void setTextColor(int color) {
		headerText.setTextColor(color);
	}
	
	public void startLoadAnim(){
		mPullAnimPic.setImageResource(R.drawable.pull_list_anim);
		animationDrawable = (AnimationDrawable) mPullAnimPic.getDrawable();  
		animationDrawable.start();
	}
	
	public void endLoadAnim(){
		try {
			animationDrawable = (AnimationDrawable) mPullAnimPic.getDrawable();  
			animationDrawable.stop();
		} catch (Exception e) {
		}
		mPullAnimPic.setImageResource(R.drawable.pull_anim_pic0);
	}
	
	public void setPullPreLabel(int scale){
		if(scale < 40)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic0);
		else if(scale < 46)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic01);
		else if(scale < 52)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic02);
		else if(scale < 58)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic03);
		else if(scale < 64)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic04);
		else if(scale < 70)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic05);
		else if(scale < 76)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic06);
		else if(scale < 82)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic07);
		else if(scale < 88)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic08);
		else if(scale < 92)
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic09);
		else
			mPullAnimPic.setImageResource(R.drawable.pull_anim_pic09);
	}
	
}
