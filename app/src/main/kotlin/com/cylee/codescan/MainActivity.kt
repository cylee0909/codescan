package com.cylee.codescan

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.csnbgsh.herbarium.bind
import com.cylee.codescan.data.ScanCodeItem
import com.cylee.codescan.data.ScanCollection
import com.cylee.codescan2.R
import com.cylee.lib.widget.dialog.DialogUtil
import com.google.zxing.CaptureActivity
import com.google.zxing.Result
import com.google.zxing.camera.CameraManager
import java.util.*

class MainActivity : CaptureActivity() , View.OnClickListener{
    var mScanPositionBn : Button? = null;
    var mScanOtherBn : Button? = null;
    var mPositionTipText : TextView? = null;
    var mPositonCode : String? = "";
    var mScanList:ListView? = null
    var mAdapter : ScanAdapter? = null;
    var mScanCollection : ScanCollection? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = bind<FrameLayout>(R.id.qcs_root)
        val extraView : View = layoutInflater.inflate(R.layout.code_ext, null)
        val rect: Rect = CameraManager.get().framingRect
        val params : FrameLayout.LayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = rect.bottom
        root?.addView(extraView, params)
        bind<TextView>(R.id.qcs_title).text = "扫码"
        var clear = bind<TextView>(R.id.qcs_right_text)
        clear.text = "清空"
        clear.setOnClickListener {
            v ->
            DialogUtil().showDialog(this, "确认清空", "取消", "确认", object : DialogUtil.ButtonClickListener {
                override fun OnLeftButtonClick() {
                }

                override fun OnRightButtonClick() {
                    mScanCollection?.data?.clear()
                    mAdapter?.notifyDataSetChanged()
                }
            }, "确认全部已扫描的数据？")
        }
        init(extraView);
    }

    private fun init(v:View) {
        mScanPositionBn = v.bind(R.id.scan_position);
        mScanOtherBn = v.bind(R.id.scan_other);
        mPositionTipText = v.bind(R.id.position_code_tip);
        mScanList = v.bind(R.id.codes)
        mScanPositionBn?.isSelected = true;
        mScanPositionBn?.setOnClickListener(this)
        mScanOtherBn?.setOnClickListener(this)

        mAdapter = ScanAdapter()
        mScanList!!.adapter = mAdapter
        mScanList!!.setOnItemLongClickListener { adapterView, view, i, l ->
            DialogUtil().showDialog(this, "确认删除", "取消", "删除", object : DialogUtil.ButtonClickListener {
                override fun OnLeftButtonClick() {
                }

                override fun OnRightButtonClick() {
                    var item = mAdapter?.getItem(i) as ScanCodeItem;
                    mScanCollection?.data?.remove(item)
                    mAdapter?.notifyDataSetChanged()
                }
            }, "确认删除该条信息？")
            true
        }

        mScanList!!.setOnItemClickListener { adapterView, view, i, l ->
            startActivity(MakeScanActivity.createIntent(this@MainActivity, mScanCollection, i))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.scan_position -> {
                mScanPositionBn?.isSelected = true;
                mScanOtherBn?.isSelected = false;
                reCapture();
            }
            R.id.scan_other -> {
                if (TextUtils.isEmpty(mPositonCode)) {
                    DialogUtil.showToast("请先扫描货位码", false);
                    return
                }
                reCapture();
                mScanPositionBn?.isSelected = false;
                mScanOtherBn?.isSelected = true;
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mScanCollection = ScanCollection.readFromPreference()
        if (mScanCollection == null) {
            mScanCollection = ScanCollection()
        }
        mAdapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        mScanCollection?.writeToPreference()
        mHandler.removeCallbacks(recaptureRunnable)
    }

    override fun handleDecode(result: Result?, barcode: Bitmap?) {
        playBeepSoundAndVibrate()
        if (mScanPositionBn!!.isSelected) {
            mPositonCode = result?.text
            mPositionTipText!!.setText("当前货位码："+mPositonCode);
        } else if (mScanOtherBn!!.isSelected){
            val v = View.inflate(this, R.layout.confirm_code, null);
            val text = v.bind<TextView>(R.id.code_info_text);
            val codeItem = formatCodeResult(result?.text);
            if (codeItem != null) {
                var exist = mScanCollection?.data?.contains(codeItem) ?: false;
                if (exist) {
                    DialogUtil.showToast("数据重复", false)
                    mHandler.postDelayed({
                        reCapture()
                    }, 1000)
                    return;
                }
                text.text = "流水号："+codeItem.id
                val numEdit = v.bind<EditText>(R.id.num_edit)
                numEdit.setText(codeItem.count.toString())
                numEdit.setSelection(numEdit.text.length)
                DialogUtil().showViewDialog(this, "确认信息", "取消", "确认",object : DialogUtil.ButtonClickListener {
                    override fun OnRightButtonClick() {
                        mHandler.postDelayed({
                            reCapture()
                        }, 1000)
                        codeItem.count = numEdit.text.toString().toFloat()
                        mScanCollection?.data?.add(codeItem)
                        mAdapter?.notifyDataSetChanged()
                    }
                    override fun OnLeftButtonClick() {
                        mHandler.postDelayed({
                            reCapture()
                        }, 1000)
                    }
                }, v)
            } else {
                DialogUtil.showToast("无效的货物码", false);
                mHandler.postDelayed({
                    reCapture()
                }, 1000)
            }
        }
    }

    private fun formatCodeResult(str:String?) : ScanCodeItem? {
        if (str != null) {
            var result = ScanCodeItem()
            result.id = str
            result.count = 1f
            result.positionCode = mPositonCode
            result.date = ScanUtil.formatCurrent()
            return result
        }
        return null
    }

    private val recaptureRunnable = Runnable() {
        run {
            reCapture()
        }
    }

    private val mHandler = Handler(Looper.getMainLooper());

    inner class ScanAdapter:BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var holder : Holder
            var v:View? = convertView
            if (v == null) {
                v = View.inflate(this@MainActivity, R.layout.scan_item, null)
                holder = Holder()
                holder.text = v?.bind(R.id.si_text)
                v.setTag(holder)
            } else{
                holder = v.tag as Holder
            }
            var item : ScanCodeItem = mScanCollection!!.data.get(position)
            if (item != null) {
                holder.text?.text = "流水码："+item.id + "\n货位码"+item.positionCode+"  数量："+item.count
            }
            return v;
        }

        override fun getItem(position: Int): Any? {
            if (position < count) {
                return mScanCollection!!.data.get(position)
            }
            return null;
        }

        override fun getItemId(position: Int): Long {
            return position.toLong();
        }

        override fun getCount(): Int {
            return mScanCollection?.data?.size ?: 0
        }

        override fun notifyDataSetChanged() {
            Collections.sort(mScanCollection?.data,  {t1, t2 -> t2.date.compareTo(t1.date)} )
            super.notifyDataSetChanged()
        }

        private inner class Holder {
            var text : TextView? = null
        }
    }
}
