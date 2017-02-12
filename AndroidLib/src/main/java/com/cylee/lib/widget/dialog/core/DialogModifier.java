package com.cylee.lib.widget.dialog.core;

import android.view.Gravity;
import android.view.View;

import com.cylee.lib.R;

/**
 * Created by cylee on 2015/11/26.
 */
public class DialogModifier {
    boolean mTitleDividerVisible;
    int mButtonOrientation;
    int mRightTitleIconRes;
    int mLeftTitleIconRes;
    int mGravity = Gravity.CENTER;

    View.OnClickListener mLeftTitleIconClickListener;
    View.OnClickListener mRightTitleIconClickListener;

    /** 根据PM设计 AlertDialog list 分两种情况， 导航类与非导航类， 非导航类如性别选择对话框，选择某一分类时不跳转页面，其UI表现为文字居中，无向右箭头*/
    boolean mListNavigate = true;

    boolean mAnimFromBottom;
    boolean mUseSkin = true;

    public boolean isTitleDividerVisible() {
        return mTitleDividerVisible;
    }

    public DialogModifier setTitleDividerVisible(boolean titleDividerVisible) {
        mTitleDividerVisible = titleDividerVisible;
        return this;
    }

    public int getButtonOrientation() {
        return mButtonOrientation;
    }

    public DialogModifier setButtonOrientation(int buttonOrientation) {
        mButtonOrientation = buttonOrientation;
        return this;
    }

    public int getRightTitleIconRes() {
        return mRightTitleIconRes;
    }

    public DialogModifier setRightTitleIconRes(int rightTitleIconRes, View.OnClickListener listener) {
        mRightTitleIconRes = rightTitleIconRes;
        mRightTitleIconClickListener = listener;
        return this;
    }

    public int getLeftTitleIconRes() {
        return mLeftTitleIconRes;
    }

    public DialogModifier setLeftTitleIconRes(int leftTitleIconRes, View.OnClickListener listener) {
        mLeftTitleIconRes = leftTitleIconRes;
        mLeftTitleIconClickListener = listener;
        return this;
    }

    public DialogModifier setRightTitleIconAsClose() {
        mRightTitleIconRes = R.drawable.scrape_card_close_selector;
        mRightTitleIconClickListener = null;
        return this;
    }

    public boolean isListNavigate() {
        return mListNavigate;
    }

    public DialogModifier setListNavigate(boolean listNavigate) {
        mListNavigate = listNavigate;
        return this;
    }

    public boolean isAnimFromBottom() {
        return mAnimFromBottom;
    }

    public DialogModifier setAnimFromBottom(boolean animFromBottom) {
        mAnimFromBottom = animFromBottom;
        return this;
    }

    public boolean isUseSkin() {
        return mUseSkin;
    }

    public DialogModifier setUseSkin(boolean useSkin) {
        mUseSkin = useSkin;
        return this;
    }

    public DialogModifier setGravity(int gravity) {
        mGravity = gravity;
        return this;
    }


    final void innerModify(AlertController controller, View contentView) {
        controller.setTitleDividerVisible(mTitleDividerVisible);
        controller.setButtonOrientation(mButtonOrientation);
        controller.setTitleLeftIconRes(mLeftTitleIconRes, mLeftTitleIconClickListener);
        controller.setTitleRightIconRes(mRightTitleIconRes, mRightTitleIconClickListener);
        customModify(controller, contentView);
        controller.confirmModify();
    }

    protected void customModify(AlertController controller, View contentView) {

    }
}
