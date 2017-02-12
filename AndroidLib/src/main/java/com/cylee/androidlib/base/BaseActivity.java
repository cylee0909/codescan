package com.cylee.androidlib.base;

import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;

public class BaseActivity extends FragmentActivity {
    /**
     * 将网络请求的listener的强引用保存在activity内部，这样底层网络库不再强引用持有listener对象
     */
    private SparseArray<Object> listenerRef = new SparseArray<Object>();
    public void addListenerRef(int key,Object listener){
        this.listenerRef.put(key,listener);
    }
    public boolean removeListenerRef(int key){
        boolean ret = this.listenerRef.get(key) != null;
        this.listenerRef.remove(key);
        return ret;
    }
}
