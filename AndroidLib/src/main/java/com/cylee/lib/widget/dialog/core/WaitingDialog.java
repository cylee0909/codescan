package com.cylee.lib.widget.dialog.core;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.cylee.lib.R;

public class WaitingDialog extends android.app.AlertDialog implements DialogInterface {
	
	private CharSequence mMessage;
    private boolean isAttached = false;
	public WaitingDialog(Context context) {
		this(context, R.style.common_alert_dialog_theme);
	}

	public WaitingDialog(Context context, int theme) {
		super(context, theme);
	}

	public WaitingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_alert_dialog_waiting);
		setCancelable(false);
		TextView mMessageView = (TextView) findViewById(R.id.iknow_alert_dialog_waiting_message);
		mMessageView.setText(mMessage);
	}

	@Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
	public void setMessage(CharSequence message) {
    	if(!TextUtils.isEmpty(message)){
    		((TextView) findViewById(R.id.iknow_alert_dialog_waiting_message)).setText(message);           
    	}
	}
	
	public static WaitingDialog show(Context context, CharSequence title,
            CharSequence message, OnCancelListener cancelListener) {       
        return show(context, title, message, cancelListener, null, null, null, null);
    }
	
	public static WaitingDialog show(Context context, CharSequence title,
            CharSequence message) {
        return show(context, title, message, null);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }

    public boolean isAttached() {
        return isAttached;
    }

    public static WaitingDialog show(Context context, CharSequence title, CharSequence message,OnCancelListener cancelListener ,
			int positiveTextId,	int neutralTextId, int negativeTextId, 
			OnClickListener onClickListener) {
        try {
            CharSequence positiveText = null;
            CharSequence neutralText = null;
            CharSequence negativeText = null;
            if (positiveTextId > 0) {
               positiveText = context.getResources().getText(positiveTextId);
            }

            if (neutralTextId > 0) {
                neutralText = context.getResources().getText(neutralTextId);
            }

            if (negativeTextId > 0) {
                negativeText = context.getResources().getText(negativeTextId);
            }


            return show(context, title, message, cancelListener, positiveText, neutralText, negativeText, onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
	
	
	public static WaitingDialog show(Context context, CharSequence title, CharSequence message,OnCancelListener cancelListener ,
			CharSequence positiveText,	CharSequence neutralText, CharSequence negativeText, 
			OnClickListener onClickListener) {
		
		WaitingDialog dialog = null;
        try {
            dialog = new WaitingDialog(context);
            dialog.show();
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setOnCancelListener(cancelListener);
            if (positiveText != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveText, onClickListener);
            }

            if (neutralText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, neutralText, onClickListener);
            }

            if (negativeText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, negativeText, onClickListener);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return dialog;
	}

}
