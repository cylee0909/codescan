package com.cylee.lib.widget.webview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cylee.lib.R;


public abstract class BaseWebActivity extends Activity {

	private TextView back;
	private ProgressWebView      mWebView;
	private TextView mTitle;
	private View.OnClickListener onClickListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_content);
		readIntent();
		findViews();
		initWorks();
	}

	private void findViews() {
		back = (TextView) findViewById(R.id.wc_back);
		mWebView = (ProgressWebView) findViewById(R.id.wc_webview);
		mTitle = (TextView) findViewById(R.id.wc_title);
	}

	private void initWorks() {
		onClickListener=new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.wc_back) {
					BaseWebActivity.this.finish();
				}
			}
		};
		back.setOnClickListener(onClickListener);
		mTitle.setText(getTopTitle());
		String contentUrl = getContentUrl();
		mWebView.loadUrl(contentUrl);
	}

	protected abstract String getContentUrl();
	protected abstract String getTopTitle();

	protected void readIntent() {}
}
