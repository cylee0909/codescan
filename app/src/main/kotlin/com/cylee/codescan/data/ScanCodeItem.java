package com.cylee.codescan.data;

import java.io.Serializable;

/**
 * Created by cylee on 16/9/9.
 */
public class ScanCodeItem implements Serializable{
    // 流水号
    private String mId;
    private float mCount;
    private String mDate;
    private String mPositionCode;

    public float getCount() {
        return mCount;
    }

    public void setCount(float mCount) {
        this.mCount = mCount;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    public String getPositionCode() {
        return mPositionCode;
    }

    public void setPositionCode(String mPositionCode) {
        this.mPositionCode = mPositionCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScanCodeItem) {
            return mId.equals(((ScanCodeItem) o).getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mId == null ? super.hashCode() : mId.hashCode();
    }
}
