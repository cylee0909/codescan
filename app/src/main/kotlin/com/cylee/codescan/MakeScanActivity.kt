package com.cylee.codescan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.csnbgsh.herbarium.bind
import com.cylee.androidlib.base.BaseActivity
import com.cylee.androidlib.util.ScreenUtil
import com.cylee.androidlib.util.ZxingUtil
import com.cylee.codescan.data.ScanCodeItem
import com.cylee.codescan.data.ScanCollection
import com.cylee.codescan2.R

/**
 * Created by cylee on 16/9/11.
 */
class MakeScanActivity : BaseActivity() {
    companion object {
        const val INPUT_DATA : String = "INPUT_DATA";
        const val INPUT_POSITION : String = "INPUT_POSITION";

        fun createIntent(context: Activity, scanCollection: ScanCollection?, position: Int) : Intent {
            var intent = Intent(context, MakeScanActivity::class.java)
            intent.putExtra(INPUT_DATA, scanCollection)
            intent.putExtra(INPUT_POSITION, position)
            return intent
        }
    }

    private var mPager : ViewPager? = null
    private var mData : ScanCollection? = null;
    private var mPosition : Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.make_scan)
        getIntent(intent)
        initView()
    }

    private fun getIntent(intent : Intent) {
        if (intent != null) {
            mPosition = intent.getIntExtra(INPUT_POSITION, -1);
            mData = intent.getSerializableExtra(INPUT_DATA) as ScanCollection;
        }
    }

    private fun initView() {
        mPager = bind(R.id.ms_pager)
        bind<TextView>(R.id.ms_exit).setOnClickListener{v -> onBackPressed()}
        mPager?.adapter = InnerAdapter()
        mPager?.setCurrentItem(mPosition, false)
    }

    private fun makeScan(pos:Int, v:View, item:ScanCodeItem) {
        var img = v.bind<ImageView>(R.id.msi_img)
        var width = ScreenUtil.getScreenWidth() - ScreenUtil.dp2px(40f)
        var params = img.layoutParams as LinearLayout.LayoutParams
        params.topMargin = ScreenUtil.dp2px(20f)
        params.width = width
        params.height = width
        var encodeContent = "{"+item.id+","+item.positionCode+","+item.count+"}"
        try {
            var bitmap = ZxingUtil.create2DCode(encodeContent, width)
            if (bitmap != null) {
                img.setImageBitmap(bitmap);
            }
        } catch (e : Throwable) {
        }

        var text = v.bind<TextView>(R.id.msi_text)
        text.text = "["+(pos + 1).toString()+"/"+(mData?.data?.size ?: "-") +
                "] 货物编码:"+item.id+"\n"+
                "货位号:"+item.positionCode+"\n"+
                "数量:"+item.count
    }


    inner class InnerAdapter : PagerAdapter() {
        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view == `object`;
        }

        override fun getCount(): Int {
            return mData?.data?.size ?: 0
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
            var v = View.inflate(this@MakeScanActivity, R.layout.mk_scan_item, null)
            container?.addView(v)
            var item = mData?.data?.get(position)
            if (item != null) {
                makeScan(position, v, item)
            }
            return v
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            container?.removeView(`object` as View)
        }
    }
}