package com.cylee.lib.widget.dialog;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cylee.androidlib.util.ScreenUtil;
import com.cylee.lib.R;
public class ReportPopupWindow {
    private PopupWindow popupWindow;
    public interface ItemClickListener{
        /**
         *
         * @param index 选中了第几项
         */
        public void onItemClick(int index);
    }

    /**
     *
     * @param activity
     * @param asDropDownView
     * @param contents 每一个选项的内容文本
     * @param icons 每一个选项的icon
     * @param listener
     */
    public void showReportWindow(final Activity activity, View asDropDownView,String[] contents,int[] icons, final ItemClickListener listener){
        if(popupWindow == null){
            LayoutInflater inflater = LayoutInflater.from(activity);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.common_vw_right_more_dialog, null);

            int dW = ViewGroup.LayoutParams.WRAP_CONTENT;
            float per = 0.34f;
            if(contents.length > 1) {
                DisplayMetrics m = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(m);
                dW = (int)(m.widthPixels * per);
            }

            for(int i=0;i<contents.length;i++){
                final int index = i;
                View reportItemView =  inflater.inflate(R.layout.common_report_window_item,null);
                TextView textView = (TextView) reportItemView.findViewById(R.id.reportItemName);
                textView.setText(contents[i]);
                textView.setCompoundDrawablesWithIntrinsicBounds(icons[i], 0, 0, 0);
                reportItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(index);
                    }
                });
                setBackgroundResWithCorner(reportItemView,i,contents.length);
                layout.addView(reportItemView);

                if (contents.length > 1 && i >= 0 && i < (contents.length - 1)) {
                    int width = dW;
                    View lineView = new View(activity);
                    lineView.setBackgroundColor(0xfff2f2f2);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, 2);
                    lineView.setLayoutParams(layoutParams);
                    layout.addView(lineView);
                }
            }
            popupWindow = new PopupWindow(layout, dW, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
        }

        popupWindow.showAsDropDown(asDropDownView, 0, ScreenUtil.dp2px(9));
    }

    private void setBackgroundResWithCorner(View view,int position ,int listSize){
        if(view != null && position>=0 && listSize>0){
            if(listSize == 1){
                view.setBackgroundResource(R.drawable.common_selector_report_window_item_bg);
            }else{
                if(position == 0){
                    view.setBackgroundResource(R.drawable.common_selector_report_window_item_top_bg);
                }else if(position == (listSize-1)){
                    view.setBackgroundResource(R.drawable.common_selector_report_window_item_bottom_bg);
                }else{
                    view.setBackgroundResource(R.drawable.common_selector_report_window_item_middle_bg);
                }
            }
        }
    }

    public void dismissReportWindow(){
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow = null;
        }
    }


}
