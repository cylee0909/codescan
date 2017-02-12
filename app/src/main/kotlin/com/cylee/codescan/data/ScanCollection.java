package com.cylee.codescan.data;

import com.cylee.androidlib.util.PreferenceUtils;
import com.cylee.codescan.CommonPreference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylee on 16/9/10.
 */
public class ScanCollection implements Serializable{
    List<ScanCodeItem> mData = new ArrayList<>();

    public List<ScanCodeItem> getData() {
        return mData;
    }

    public void setData(List<ScanCodeItem> mData) {
        this.mData = mData;
    }

    public static ScanCollection readFromPreference() {
        return PreferenceUtils.getObject(CommonPreference.CODE_COLLECTION, ScanCollection.class);
    }

    public void writeToPreference() {
        PreferenceUtils.setObject(CommonPreference.CODE_COLLECTION, this);
    }
}
