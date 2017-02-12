package com.cylee.lib.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylee.androidlib.base.BaseApplication;
import com.cylee.androidlib.util.ScreenUtil;
import com.cylee.lib.R;
import com.cylee.lib.widget.dialog.core.AlertController;
import com.cylee.lib.widget.dialog.core.AlertDialog;
import com.cylee.lib.widget.dialog.core.DialogModifier;
import com.cylee.lib.widget.dialog.core.WaitingDialog;

import java.util.ArrayList;
import java.util.List;

public class DialogUtil{
	
	private final static String TAG = "UIUtil";
	private boolean mUseSkin=true;

	public boolean isUseSkin() {
		return mUseSkin;
	}

	public void setUseSkin(boolean useSkin) {
		mUseSkin = useSkin;

	}
	/**
	 * 监听对话框按钮点击事件
	 * @author chenmiao
	 *
	 */
	public static interface ButtonClickListener{
		/**
		 * This method will be invoked when the left button in the dialog is clicked.
		 */
		public void OnLeftButtonClick();
		/**
		 * This method will be invoked when the right button in the dialog is clicked.
		 */
		public void OnRightButtonClick();
	}
	
	/********************************Toast相关********************************/
	
	private static android.widget.Toast mToast = null;
	/**
	 * UI和非UI线程都可以调用此方法
	 * 
	 * @param text
	 *            显示的内容
	 * @param longToast
	 *            长时间显示还是短时间显示
	 */
	public static void showToast(CharSequence text, boolean longToast) {
		int duration = longToast ? android.widget.Toast.LENGTH_LONG: android.widget.Toast.LENGTH_SHORT;
		showToast(text, duration);
	}
	
	/**
	 * UI和非UI线程都可以调用此方法
	 * 
	 * @param textId
	 *            显示的内容
	 * @param longToast
	 *            长时间显示还是短时间显示
	 */
	public static void showToast(int textId, boolean longToast) {
		showToast(BaseApplication.getApplication().getString(textId), longToast);
	}
	
	/**
	 * UI和非UI线程都可以调用此方法
	 * 
	 * @param text
	 *            显示的内容
	 * @param duration
	 *            长时间显示还是短时间显示
	 */
	 public static void showToast(final CharSequence text, final int duration) {
		if (Looper.myLooper() != Looper.getMainLooper()) {//不在主线程
			new Handler(BaseApplication.getApplication().getMainLooper()).post(new Runnable() {

				@Override
				public void run() {
					toast(BaseApplication.getApplication(), text, duration, 0, 0, 0);
				}
			});
		} else {//在主线程里面
			toast(BaseApplication.getApplication(), text, duration, 0, 0, 0);
		}
	}

	/**
	 * UI和非UI线程都可以调用此方法

	 * @param text 显示的内容

	 * @param gravity  gravity是输入Toast需要显示的位置，0,是默认值，例如CENTER_VERTICAL（垂直居中）、CENTER_HORIZONTAL（水平居中）、TOP（顶部）等等。
	 * @param xOffset  则是决定Toast在水平方向（x轴）的偏移量，偏移量单位为，大于0向右偏移，小于0向左偏移。
	 * @param yOffset  决定Toast在垂直方向（y轴）的偏移量，大于0向下偏移，小于0向上偏移
	 */
	public static void showToast(final Context context, final CharSequence text, final int duration, final int gravity, final int xOffset,final  int yOffset) {
		if (Looper.myLooper() != Looper.getMainLooper()) {//不在主线程
			new Handler(context.getMainLooper()).post(new Runnable() {

				@Override
				public void run() {
					toast(context, text, duration, gravity, xOffset, yOffset);
				}
			});
		} else {//在主线程里面
			toast(context, text, duration, gravity, xOffset, yOffset);
		}
	}

	/**
	 * 外部不能调用此方法（应该调用showToast方法）
	 * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showToast方法
	 *
	 * @param context
	 * @param text
	 * @param duration
	 */
	private static void toast(Context context, CharSequence text, int duration, int gravity, int xOffset, int yOffset) {
		if (mToast != null && mToast.getView() != null) {
			TextView textView = (TextView) mToast.getView().findViewById(R.id.common_toast_message);
			textView.setText(text);
			mToast.setDuration(duration);
			mToast.show();
			return;
		}
		mToast = null;
		android.widget.Toast toast;
		try {
			toast = new android.widget.Toast(context.getApplicationContext());
		} catch (Exception e) {
			/**
			 * java.lang.NullPointerException
			 at android.widget.Toast.<init>(Toast.java:105)
			 at com.baidu.homework.common.ui.dialog.DialogUtil.a(Unknown Source)
			 at com.baidu.homework.common.ui.dialog.DialogUtil.showToast(Unknown Source)
			 at com.baidu.homework.common.ui.dialog.DialogUtil.showToast(Unknown Source)
			 at com.baidu.homework.activity.askteacher.AskTeacherLauncher.a(Unknown Source)
			 */
			return;
		}
		if (gravity != 0) {
			toast.setGravity(gravity, xOffset, yOffset);
		}
		View view = View.inflate(context.getApplicationContext(), R.layout.common_transient_toast, null);
		TextView textView = (TextView)view.findViewById(R.id.common_toast_message);
		textView.setText(text);
		textView.setVisibility(View.VISIBLE);
		toast.setView(view);
		toast.setDuration(duration);
		mToast = toast;
		toast.show();
	 }
	
	/***************************************列表对话框相关*************************************/
	
	/**
	 * 监听列表对话框点击列表点击事件
	 * @author chenmiao
	 *
	 */
	public static interface ListItemClickListener{
		/**
		 * This method will be invoked when a listItem in the dialog is clicked.
		 * @param position the position of the item clicked.
		 */
		public void onItemClick(int position);
	}
	
	private AlertDialog mListAlertDialog;

	public void showListDialog(Activity context,
							   String title,
							   List<KeyValuePair<Integer, String>> list,
							   final ListItemClickListener listItemListener,
							   OnCancelListener cancelListener) {
		showListDialog(context, title, list, listItemListener, cancelListener, null);
	}

    public void showListDialog(Activity context,
                               String title,
                               List<KeyValuePair<Integer, String>> list,
                               final ListItemClickListener listItemListener,
                               OnCancelListener cancelListener, DialogModifier modifier) {
        final List<Integer> keyList = new ArrayList<Integer>();
        List<String> valList = new ArrayList<String>();

        for (KeyValuePair<Integer, String> item : list) {
            keyList.add(item.getKey());
            valList.add(item.getValue());
        }

        showListDialog(context, title, null, null, null, valList, new ListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (listItemListener != null) {
                    listItemListener.onItemClick(keyList.get(position));
                }
            }
        }, cancelListener, modifier);
    }

	/**
	 * 重载了该方法 修改了 item 回调参数 处理
	 * @param context
	 * @param title
	 * @param list
	 * @param listItemListener
	 * @param cancelListener
	 */
	public void showListDialogForCallback(Activity context,
							   String title,
							   List<String> list,
							   final ListItemClickListener listItemListener,
							   OnCancelListener cancelListener) {
		showListDialog(context, title, null, null, null, list, new ListItemClickListener() {
			@Override
			public void onItemClick(int position) {
				if (listItemListener != null) {
					listItemListener.onItemClick(position);
				}
			}
		}, cancelListener);
	}

	/**
	 * 显示含有标题的列表对话框（调用dismissListDialog()关闭对话框）
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * @param buttonListener 监听对话框button事件
	 * @param list 显示列表内容
	 * @param listItemListener 列表点击监听器
	 */
	public void showListDialog(final Activity context, final String title, final String leftButton, final String rightButton,
							   final ButtonClickListener buttonListener, final List<String> list,
							   final ListItemClickListener listItemListener, final OnCancelListener cancelListener){
		showListDialog(context, title, leftButton, rightButton, buttonListener, list, listItemListener, cancelListener, null);
	}

	/**
	 * 显示含有标题的列表对话框（调用dismissListDialog()关闭对话框）
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * @param buttonListener 监听对话框button事件
	 * @param list 显示列表内容
	 * @param listItemListener 列表点击监听器
	 * @param modifier
	 */
	public void showListDialog(final Activity context, final String title, final String leftButton, final String rightButton,
			final ButtonClickListener buttonListener, final List<String> list, 
			final ListItemClickListener listItemListener, final OnCancelListener cancelListener, final DialogModifier modifier){
		if (Looper.myLooper() != Looper.getMainLooper()) {
			new Handler(context.getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					listDialg(context, title, leftButton, rightButton, buttonListener, list, listItemListener, cancelListener, modifier);
				}
			});
		}else{
			listDialg(context, title, leftButton, rightButton, buttonListener, list, listItemListener, cancelListener, modifier);
		}
	}
	
	/**
	 * 外部不能调用此方法（应该调用showListDialg方法）
	 * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showListDialg方法
	 * @param activity
	 * @param title
	 * @param leftButton
	 * @param rightButton
	 * @param buttonListener
	 * @param list
	 * @param listItemListener
	 */
	private void listDialg(Activity activity, String title, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, List<String> list, 
			final ListItemClickListener listItemListener, OnCancelListener cancelListener, DialogModifier modifier){
        if(activity.isFinishing()){
            return;
        }
		dismissListDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setItems(list.toArray(new CharSequence[list.size()]),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (listItemListener != null) {
							listItemListener.onItemClick(which);
						}
					}
				});
		OnClickListener dialogListener = new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					if(buttonListener != null){
						buttonListener.OnLeftButtonClick();
					}
				}else if(which == DialogInterface.BUTTON_NEGATIVE){
					if(buttonListener != null){
						buttonListener.OnRightButtonClick();
					}
				}
			}
		};
		builder.setTitle(title);
		builder.setPositiveButton(leftButton, dialogListener);
		builder.setNegativeButton(rightButton, dialogListener);
		builder.setOnCancelListener(cancelListener);
		if (modifier == null) {
			builder.setModifier(new DialogModifier().setUseSkin(mUseSkin));
		} else {
			builder.setModifier(modifier.setUseSkin(mUseSkin));
		}
		mListAlertDialog = builder.show();
	}

	/**
	 * 显示无标题的列表对话框（调用dismissListDialog()关闭对话框）
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * @param buttonListener 监听对话框button事件
	 * @param list 显示列表内容
	 * @param listItemListener 列表点击监听器
	 */
	public void showListDialog(Activity context, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, List<String> list, final ListItemClickListener listItemListener){
		showListDialog(context, null, leftButton, rightButton, buttonListener, list, listItemListener, null);
	}
	
	/**
	 * 关闭列表对话框
	 */
	public void dismissListDialog(){
        try {
            if(mListAlertDialog!= null && mListAlertDialog.isShowing()){
                mListAlertDialog.dismiss();
            }
            mListAlertDialog = null;
        } catch (Exception e) {}
    }
	
	/**
	 * 创建并显示列表对话框(需要自己手动调用dialog。dismiss（）关闭对话框，调用dismissListDialog()方法无效)
	 * @param context
	 * @param title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * @param buttonListener 监听对话框button事件
	 * @param list 显示列表内容
	 * @param listItemListener 列表点击监听器
	 * @return
	 */
	public AlertDialog createListDialog(Context context, String title, String leftButton, String rightButton, 
			final ButtonClickListener buttonListener, List<String> list, final ListItemClickListener listItemListener){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setItems(list.toArray(new CharSequence[list.size()]),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (listItemListener != null) {
							listItemListener.onItemClick(which);
						}
					}
				});
		OnClickListener dialogListener = new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					if(buttonListener != null){
						buttonListener.OnLeftButtonClick();
					}
				}else if(which == DialogInterface.BUTTON_NEGATIVE){
					if(buttonListener != null){
						buttonListener.OnRightButtonClick();
					}
				}
			}
		};
		builder.setTitle(title);
		builder.setPositiveButton(leftButton, dialogListener);
		builder.setNegativeButton(rightButton, dialogListener);
		AlertDialog dialog = builder.show();
		return dialog;
	}
	
	/***************************************普通文字对话框*************************************/
	
	private AlertDialog mTextDialog;

	/**
	 * 显示无标题文字对话框 (调用dismissDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param message 显示内容
	 */
	public void showDialog(Activity context, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, CharSequence message){
		showDialog(context, null, leftButton, rightButton, buttonListener, message);
	}
	
	/**
	 * 显示有标题文字对话框 (调用dismissDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title 对话框title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param message 显示内容
	 */
	public void showDialog(Activity context, String title, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, CharSequence message){
		showDialog(context, title, leftButton, rightButton, buttonListener, message, true, true, null);
	}

	/**
	 * 显示有标题文字对话框 (调用dismissDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title 对话框title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param message 显示内容
	 * @param cancelable 按返回键是否关闭dialog
	 * @param isCanceledOnTouchOutside 按对话框外部是否关闭dialog
	 */
	public void showDialog(final Activity context, final String title, final String leftButton, final String rightButton,
						   final ButtonClickListener buttonListener, final CharSequence message, final boolean cancelable, final boolean isCanceledOnTouchOutside
			,final OnCancelListener onCancelListener){
		showDialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener, null);
	}

	/**
	 * 显示有标题文字对话框 (调用dismissDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title 对话框title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param message 显示内容
	 * @param cancelable 按返回键是否关闭dialog
	 * @param isCanceledOnTouchOutside 按对话框外部是否关闭dialog
	 * @param modifier
	 */
	public void showDialog(final Activity context, final String title, final String leftButton, final String rightButton,
			final ButtonClickListener buttonListener, final CharSequence message, final boolean cancelable, final boolean isCanceledOnTouchOutside
			,final OnCancelListener onCancelListener, final DialogModifier modifier){
		if (Looper.myLooper() != Looper.getMainLooper()) {
			new Handler(context.getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					dialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener, modifier);
				}
			});
		}else{
			dialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener, modifier);
		}
	}

	public void showModiferDialog(final Activity context, final String title, final String leftButton, final String rightButton,
						   final ButtonClickListener buttonListener, final CharSequence message, final boolean cancelable, final boolean isCanceledOnTouchOutside
			,final OnCancelListener onCancelListener, final DialogModifier modifier){
		if (Looper.myLooper() != Looper.getMainLooper()) {
			new Handler(context.getMainLooper()).post(new Runnable() {

				@Override
				public void run() {
					dialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener, modifier);
				}
			});
		}else{
			dialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener, modifier);
		}
	}

    /**
     * 展示小样式dialog
     * @param context
     * @param title
     * @param leftButton
     * @param rightButton
     * @param buttonListener
     * @param message
     * @param cancelable
     * @param isCanceledOnTouchOutside
     * @param onCancelListener
     */

    public void showSmallDialog(final Activity context, final String title, final String leftButton, final String rightButton,
                                final ButtonClickListener buttonListener, final CharSequence message, final boolean cancelable, final boolean isCanceledOnTouchOutside
            ,final OnCancelListener onCancelListener){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(context.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    dialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener,(int)(ScreenUtil.getScreenWidth()/1.2),WindowManager.LayoutParams.WRAP_CONTENT);
                }
            });
        }else{
            dialog(context, title, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener,(int)(ScreenUtil.getScreenWidth()/1.2),WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public void showSmallDialog(Activity context, String title, String leftButton, String rightButton,
                           final ButtonClickListener buttonListener, CharSequence message){
        showSmallDialog(context, title, leftButton, rightButton, buttonListener, message, true, true, null);
    }
    public void showSmallDialog(Activity context, String leftButton, String rightButton,
                           final ButtonClickListener buttonListener, CharSequence message){
        showSmallDialog(context, null, leftButton, rightButton, buttonListener, message);
    }

    /**
     *     自适应横竖屏dialog
     */
    public void showOrientDialog(final Activity context,
                                 final String leftButton,
                                 final String rightButton,
                                 final ButtonClickListener buttonListener,
                                 int  message){
        showOrientDialog(context, leftButton, rightButton, buttonListener, context.getString(message));
    }
	/**
	 *     自适应横竖屏dialog
	 */
	public void showOrientDialog(final Activity context,
								 final String leftButton,
								 final String rightButton,
								 final ButtonClickListener buttonListener,
								 final CharSequence  message){
		showOrientDialog(context, leftButton, rightButton, buttonListener, message,true,false,null);
	}
    /**
     *     自适应横竖屏dialog
     */
    public void showOrientDialog(final Activity context,
                                    final String leftButton,
                                    final String rightButton,
                                    final ButtonClickListener buttonListener,
                                    final CharSequence message,final boolean cancelable, final boolean isCanceledOnTouchOutside
			,final OnCancelListener onCancelListener){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(context.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    if(context.getRequestedOrientation()== android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                        dialog(context, null, leftButton, rightButton, buttonListener, message, cancelable, isCanceledOnTouchOutside, onCancelListener,ScreenUtil.getScreenWidth()*2/3,WindowManager.LayoutParams.WRAP_CONTENT);
                    }else{
                        dialog(context, null, leftButton, rightButton, buttonListener, message,  cancelable, isCanceledOnTouchOutside, onCancelListener, null);
                    }
                }
            });
        }else{
            if(context.getRequestedOrientation()== android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                dialog(context, null, leftButton, rightButton, buttonListener, message,  cancelable, isCanceledOnTouchOutside, onCancelListener,ScreenUtil.getScreenWidth()*2/3,WindowManager.LayoutParams.WRAP_CONTENT);
            }else{
                dialog(context, null, leftButton, rightButton, buttonListener, message,  cancelable, isCanceledOnTouchOutside, onCancelListener, null);
            }
        }
    }
	
	/**
	 * 外部不能调用此方法（应该调用showDialg方法）
	 * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showDialg方法
	 * @param activity
	 * @param title
	 * @param leftButton
	 * @param rightButton
	 * @param buttonListener
	 * @param message
	 * @param cancelable
	 * @param isCanceledOnTouchOutside
	 */
	private void dialog(Activity activity, String title, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, CharSequence message, boolean cancelable, boolean isCanceledOnTouchOutside
			,OnCancelListener onCancelListener, final DialogModifier modifier){
        if(activity.isFinishing()){
            return;
        }
		dismissDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setOnCancelListener(onCancelListener);
		if (modifier == null) {
			builder.setModifier(new DialogModifier().setUseSkin(mUseSkin));
		} else {
			builder.setModifier(modifier.setUseSkin(mUseSkin));
		}

		OnClickListener dialogListener = new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					if(buttonListener != null){
						buttonListener.OnLeftButtonClick();
					}
				}else if(which == DialogInterface.BUTTON_NEGATIVE){
					if(buttonListener != null){
						buttonListener.OnRightButtonClick();
					}
				}
			}
			
		};
		builder.setPositiveButton(leftButton, dialogListener);
		builder.setNegativeButton(rightButton, dialogListener);
		mTextDialog = builder.show();
		mTextDialog.setCancelable(cancelable);
		mTextDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
	}

    /**
     * 外部不能调用此方法（应该调用showDialg方法）
     * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showDialg方法
     * @param activity
     * @param title
     * @param leftButton
     * @param rightButton
     * @param buttonListener
     * @param message
     * @param cancelable
     * @param isCanceledOnTouchOutside
     */
    private void dialog(Activity activity, String title, String leftButton, String rightButton,
                        final ButtonClickListener buttonListener, CharSequence message, boolean cancelable, boolean isCanceledOnTouchOutside
            ,OnCancelListener onCancelListener,int width,int height){
        if(activity.isFinishing()){
            return;
        }
        dismissDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
		//if (modifier == null) {
			builder.setModifier(new DialogModifier().setUseSkin(mUseSkin));
//		} else {
//			builder.setModifier(modifier.setUseSkin(mUseSkin));
//		}
        builder.setOnCancelListener(onCancelListener);
        OnClickListener dialogListener = new OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    if(buttonListener != null){
                        buttonListener.OnLeftButtonClick();
                    }
                }else if(which == DialogInterface.BUTTON_NEGATIVE){
                    if(buttonListener != null){
                        buttonListener.OnRightButtonClick();
                    }
                }
            }

        };
        builder.setPositiveButton(leftButton, dialogListener);
        builder.setNegativeButton(rightButton, dialogListener);
        mTextDialog = builder.show(width,height);
        mTextDialog.setCancelable(cancelable);
        mTextDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
    }
	
	/**
	 * 关闭所有文字对话框
	 */
	public void dismissDialog(){
        try {
            if(mTextDialog != null && mTextDialog.isShowing()){
                mTextDialog.dismiss();
            }
            mTextDialog = null;
        } catch (Exception e) {}
    }
	
	/**
	 * 创建并显示文字对话框(需要自己手动调用dialog。dismiss（）关闭对话框，调用dismissDialog()方法无效)
	 * @param context
	 * @param title 对话框标题（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param message 显示内容
	 * @return
	 */
	public AlertDialog createDialog(Context context, String title, String leftButton, String rightButton, 
			final ButtonClickListener buttonListener, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		OnClickListener dialogListener = new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					if(buttonListener != null){
						buttonListener.OnLeftButtonClick();
					}
				}else if(which == DialogInterface.BUTTON_NEGATIVE){
					if(buttonListener != null){
						buttonListener.OnRightButtonClick();
					}
				}
			}
			
		};
		builder.setPositiveButton(leftButton, dialogListener);
		builder.setNegativeButton(rightButton, dialogListener);
		AlertDialog dialog = builder.show();
		return dialog;
	}
	
	/***************************************自定义View对话框相关*************************************/
	private AlertDialog mViewDialog;
	/**
	 * 显示有title的自定义view对话框(调用dismissViewDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title 对话框title（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param view 对话框显示view
	 */
	public void showViewDialog(Activity context, CharSequence title, String leftButton, String rightButton, final ButtonClickListener buttonListener, View view){
		showViewDialog(context, title, leftButton, rightButton, buttonListener, view, true, true, null);
	}
	
	/**
	 * 显示无title的自定义view对话框(调用dismissViewDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param view 对话框显示view
	 */
	public void showViewDialog(Activity context, String leftButton, String rightButton,
			ButtonClickListener buttonListener, View view){
		showViewDialog(context, null, leftButton, rightButton, buttonListener, view);
	}

	/**
	 * 显示无title的自定义view对话框(调用dismissViewDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param view 对话框显示view
	 * @param cancelable 按返回键是否关闭dialog
	 * @param isCanceledOnTouchOutside 按对话框外部是否关闭dialog
	 * @param backgroundColor dialog背景框颜色（目前是为了适应私密QB）
	 * @param isFrameTransparent
	 */
	public void showViewDialog(final Activity context, final CharSequence title, final String leftButton, final String rightButton,
							   final ButtonClickListener buttonListener, final View view, final boolean cancelable,
							   final boolean isCanceledOnTouchOutside,
							   final OnCancelListener cancelListener, final int backgroundColor,
							   final boolean isFrameTransparent){
		showViewDialog(context, title, leftButton, rightButton, buttonListener, view, cancelable, isCanceledOnTouchOutside, cancelListener, backgroundColor, isFrameTransparent, null);
	}

	public void showRawViewDialog(final Activity context, View view, final boolean cancelable,
								  final boolean isCanceledOnTouchOutside,
								  final OnCancelListener cancelListener, int gravity) {
		showViewDialog(context, null, null, null, null, view, cancelable, isCanceledOnTouchOutside, cancelListener, 0, false, new DialogModifier(){
			@Override
			protected void customModify(AlertController controller, View contentView) {
				super.customModify(controller, contentView);
				View root = contentView.findViewById(R.id.iknow_alert_dialog_panel_wrapper);
				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)root.getLayoutParams();
				params.leftMargin = params.rightMargin = 0;
				View contentContainer = contentView.findViewById(R.id.iknow_alert_dialog_custom_content);
				contentContainer.setPadding(0, 0, 0, 0);
			}
		}.setGravity(gravity));
	}

	/**
	 * 显示无title的自定义view对话框(调用dismissViewDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param view 对话框显示view
	 * @param cancelable 按返回键是否关闭dialog
	 * @param isCanceledOnTouchOutside 按对话框外部是否关闭dialog
	 * @param backgroundColor dialog背景框颜色（目前是为了适应私密QB）
     * @param isFrameTransparent
	 * @param modifier
	 */
	public void showViewDialog(final Activity context, final CharSequence title, final String leftButton, final String rightButton,
			final ButtonClickListener buttonListener, final View view, final boolean cancelable, 
			final boolean isCanceledOnTouchOutside, 
			final OnCancelListener cancelListener, final int backgroundColor,
            final boolean isFrameTransparent, final DialogModifier modifier){
		if(Looper.myLooper() != Looper.getMainLooper()){
			new Handler(context.getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					viewDialog(context, title, leftButton, rightButton, buttonListener, view, cancelable, isCanceledOnTouchOutside, cancelListener, backgroundColor, isFrameTransparent, modifier);
				}
			});
		}else{
			viewDialog(context, title, leftButton, rightButton, buttonListener, view, cancelable, isCanceledOnTouchOutside, cancelListener, backgroundColor, isFrameTransparent, modifier);
		}
	}
	
	/**
	 * 显示无title的自定义view对话框(调用dismissViewDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param view 对话框显示view
	 * @param cancelable 按返回键是否关闭dialog
	 * @param isCanceledOnTouchOutside 按对话框外部是否关闭dialog
	 */
	public void showViewDialog(final Activity context, final CharSequence title, final String leftButton, final String rightButton,
			final ButtonClickListener buttonListener, final View view, final boolean cancelable, 
			final boolean isCanceledOnTouchOutside, final OnCancelListener cancelListener){
		showViewDialog(context, title, leftButton, rightButton, buttonListener, view, cancelable, isCanceledOnTouchOutside, cancelListener, -1, false);
	}

	/**
	 * 外部不能调用此方法（应该调用showViewDialog方法）
	 * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showViewDialog方法
	 * @param activity
	 * @param title
	 * @param leftButton
	 * @param rightButton
	 * @param buttonListener
	 * @param view
	 * @param cancelable
	 * @param isCanceledOnTouchOutside
	 * @param isFrameTransparent
	 */
	private void viewDialog(Activity activity, CharSequence title, String leftButton, String rightButton,
							final ButtonClickListener buttonListener, View view, boolean cancelable,
							boolean isCanceledOnTouchOutside, OnCancelListener cancelListener,
							int backgroundColor,
							boolean isFrameTransparent){
		viewDialog(activity, title, leftButton, rightButton, buttonListener, view, cancelable, isCanceledOnTouchOutside, cancelListener, backgroundColor, isFrameTransparent, null);
	}

	/**
	 * 外部不能调用此方法（应该调用showViewDialog方法）
	 * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showViewDialog方法
	 * @param activity
	 * @param title
	 * @param leftButton
	 * @param rightButton
	 * @param buttonListener
	 * @param view
	 * @param cancelable
	 * @param isCanceledOnTouchOutside
     * @param isFrameTransparent
	 * @param modifier
	 */
	private void viewDialog(Activity activity, CharSequence title, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, View view, boolean cancelable, 
			boolean isCanceledOnTouchOutside, OnCancelListener cancelListener,
			int backgroundColor,
            boolean isFrameTransparent, DialogModifier modifier){
        if(activity.isFinishing()){
            return;
        }
		dismissViewDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		if(!TextUtils.isEmpty(title)){
			builder.setTitle(title);
		}
		builder.setView(view);
		OnClickListener dialogListener = new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					if(buttonListener != null){
						buttonListener.OnLeftButtonClick();
					}
				}else if(which == DialogInterface.BUTTON_NEGATIVE){
					if(buttonListener != null){
						buttonListener.OnRightButtonClick();
					}
				}
			}
			
		};
		builder.setPositiveButton(leftButton, dialogListener);
		builder.setNegativeButton(rightButton, dialogListener);
		if (modifier == null) {
			builder.setModifier(new DialogModifier().setUseSkin(mUseSkin));
		} else {
			builder.setModifier(modifier.setUseSkin(mUseSkin));
		}
        if(isFrameTransparent){
            mViewDialog = builder.show(R.style.common_alert_dialog_theme_transparent);
        }else{
            mViewDialog = builder.show();
        }
		mViewDialog.setCancelable(cancelable);
		mViewDialog.setOnCancelListener(cancelListener);
		mViewDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
		if(backgroundColor >= 0){
			mViewDialog.setBackgroudColor(backgroundColor);
		}
	}
	/**
	 * 关闭所有View对话框
	 */
	public void dismissViewDialog(){
		if(mViewDialog != null && mViewDialog.isShowing()){
			mViewDialog.dismiss();
		}
		mViewDialog = null;
	}

	/**
	 * 获取当前的View对话框
	 * @return
	 */
	public Dialog getViewDialog() {
		return mViewDialog;
	}
	
	/**
	 * 是否正在显示view对话框
	 * @return
	 */
	public boolean isShowViewDialog(){
		if (mViewDialog == null) {
			return false;
		}
		return mViewDialog.isShowing();
	}
	
	/**
	 * 创建并显示自定义View对话框(需要自己手动调用dialog。dismiss（）关闭对话框，调用dismissViewDialog()方法无效)
	 * @param context
	 * @param title 对话框标题（如果为空则不显示）
	 * @param leftButton 对话框左下面的button文字
	 * @param rightButton 对话框右下面的button文字
	 * 说明：两个button都可以为空，如果只有一个按钮不为空，则此按钮居中显示；如果两个按钮都为空
	 * @param buttonListener 监听对话框button事件
	 * @param view 对话框显示view
	 * @return
	 */
	public AlertDialog createViewDialog(Activity context, String title, String leftButton, String rightButton,
			final ButtonClickListener buttonListener, View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if(!TextUtils.isEmpty(title)){
			builder.setTitle(title);
		}
		builder.setView(view);
		OnClickListener dialogListener = new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					if(buttonListener != null){
						buttonListener.OnLeftButtonClick();
					}
				}else if(which == DialogInterface.BUTTON_NEGATIVE){
					if(buttonListener != null){
						buttonListener.OnRightButtonClick();
					}
				}
			}
			
		};
		builder.setPositiveButton(leftButton, dialogListener);
		builder.setNegativeButton(rightButton, dialogListener);
		AlertDialog dialog = builder.show();
		return dialog;
	}


    public AlertDialog createSmallViewDialog(Activity context, String title, String leftButton, String rightButton,
                                        final ButtonClickListener buttonListener, View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(!TextUtils.isEmpty(title)){
            builder.setTitle(title);
        }
        builder.setView(view);
        OnClickListener dialogListener = new OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    if(buttonListener != null){
                        buttonListener.OnLeftButtonClick();
                    }
                }else if(which == DialogInterface.BUTTON_NEGATIVE){
                    if(buttonListener != null){
                        buttonListener.OnRightButtonClick();
                    }
                }
            }

        };
        builder.setPositiveButton(leftButton, dialogListener);
        builder.setNegativeButton(rightButton, dialogListener);
        AlertDialog dialog = builder.show((int)(ScreenUtil.getScreenWidth()/1.2),WindowManager.LayoutParams.WRAP_CONTENT);
        return dialog;
    }
	
	/***************************************等待对话框相关*************************************/
	
	private WaitingDialog mWaitingDialog;
	
	/**
	 * 显示等待对话框(调用dismissWaitingDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param title 对话框title（如果为空则不显示）
	 * @param message 显示内容
	 * @param cancelable 按返回键是否关闭dialog
	 * @param isCanceledOnTouchOutside 按对话框外部是否关闭dialog
	 * @return
	 */
	public void showWaitingDialog(final Activity context, final CharSequence title, final CharSequence message, final boolean cancelable, final boolean isCanceledOnTouchOutside, final OnCancelListener onCancelListener){
        if(Looper.myLooper() != Looper.getMainLooper()){
			new Handler(context.getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					waitingDialog(context, title, message, cancelable, isCanceledOnTouchOutside, onCancelListener);
				}
			});
		}else{
			waitingDialog(context, title, message, cancelable, isCanceledOnTouchOutside, onCancelListener);
		}
	}
	
	/**
	 * 外部不能调用此方法（应该调用showWaitingDialog方法）
	 * 因为此方法只能在UI线程中调用，为了满足可以在UI和非UI都可以调用，对此方法进行了一层封装，即showWaitingDialog方法
	 * @param activity
	 * @param title
	 * @param message
	 * @param cancelable
	 * @param isCanceledOnTouchOutside
	 */
	private void waitingDialog(Activity activity, CharSequence title, CharSequence message, boolean cancelable, boolean isCanceledOnTouchOutside, OnCancelListener onCancelListener){
		if(activity.isFinishing()){
            return;
        }
        dismissWaitingDialog();
		mWaitingDialog = WaitingDialog.show(activity, title, message);
		mWaitingDialog.setCancelable(cancelable);
		mWaitingDialog.setOnCancelListener(onCancelListener);
		mWaitingDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
	}
	
	/**
	 * 显示等待对话框(调用dismissWaitingDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * 点击返回按钮或者点击对话框外部不会关闭对话框
	 * @param context
	 * @param message 显示内容
	 */
	public void showWaitingDialog(Activity context, CharSequence message){
		showWaitingDialog(context, null, message, false, false, null);
	}
	
	/**
	 * 显示等待对话框(调用dismissWaitingDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param message dialog显示文字内容
	 * @param cancelable true:点击返回按钮或者Dialog外部，dialog关闭
	 */
	public void showWaitingDialog(Activity context, CharSequence message, boolean cancelable){
		showWaitingDialog(context, null, message, cancelable, false, null);
	}

    public void showWaitingDialog(Activity context, CharSequence message, OnCancelListener cancelListener) {
        showWaitingDialog(context, null, message, true, false, cancelListener);
    }
	
	/**
	 * 显示等待对话框(调用dismissWaitingDialog()方法关闭对话框)
	 * UI和非UI线程都可以调用此方法
	 * @param context
	 * @param messageId dialog显示文字内容Id
	 * @param cancelable true:点击返回按钮或者Dialog外部，dialog关闭
	 */
	public void showWaitingDialog(Activity context, int messageId, boolean cancelable){
		showWaitingDialog(context, null, context.getString(messageId), cancelable, false, null);
	}

    public void showWaitingDialog(Activity context, int messageId, OnCancelListener cancelListener) {
        showWaitingDialog(context, null, context.getString(messageId), true, false, cancelListener);
    }
	
	/**
	 * 显示等待对话框(调用dismissWaitingDialog()方法关闭对话框)
	 * 点击返回按钮或者点击对话框外部不会关闭对话框
	 * @param context
	 * @param messageId 显示内容资源id
	 */
	public void showWaitingDialog(Activity context, int messageId){
		showWaitingDialog(context, null, context.getString(messageId), false, false, null);
	}
	
	/**
	 * 是否正在显示等待对话框
	 * @return
	 */
	public boolean isShowWaitingDialog(){
		if(mWaitingDialog == null){
			return false;
		}
		return mWaitingDialog.isShowing();
	}
	
	/**
	 * 关闭所有等待对话框
	 */
	public void dismissWaitingDialog(){
		try {
			if(mWaitingDialog != null
			        && mWaitingDialog.isShowing()){
				mWaitingDialog.dismiss();
			}
			mWaitingDialog = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 延迟关闭对话框
	 * @param delay 延迟时间
	 */
	public void dismissWaitingDialogDelay(long delay){
		if(mWaitingDialog != null){
			try {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mWaitingDialog.dismiss();
					}
				}, delay);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mWaitingDialog = null;
	}
	
	/**
	 *创建并显示等待对话框(需要自己手动调用dialog。dismiss（）关闭对话框，调用dismissWaitingDialog()方法无效)
	 * @param context
	 * @param title 对话框title（如果为空则不显示）
	 * @param message （显示内容）
	 * @param cancelable （按返回键是否可删除）
	 * @param isCanceledOnTouchOutside （按对话框外部是否可删除）
	 * @return
	 */
	public WaitingDialog createWaitingDialog(Activity context, CharSequence title, CharSequence message, boolean cancelable, boolean isCanceledOnTouchOutside){
		WaitingDialog dialog = WaitingDialog.show(context, title, message);
		dialog.setCancelable(cancelable);
		dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
		return dialog;
	}
}