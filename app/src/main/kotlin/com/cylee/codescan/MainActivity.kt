package com.cylee.codescan

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.csnbgsh.herbarium.bind
import com.cylee.androidlib.base.BaseActivity
import com.cylee.androidlib.thread.Worker
import com.cylee.androidlib.util.TaskUtils
import com.cylee.codescan.data.ScanCodeItem
import com.cylee.codescan.data.ScanCollection
import com.cylee.codescan2.R
import com.cylee.lib.widget.dialog.DialogUtil
import java.util.*

class MainActivity : BaseActivity() {
    var mPositonCode : String? = "";
    var mScanList:ListView? = null
    var mAdapter : ScanAdapter? = null;
    var mScanCollection : ScanCollection? = null;
    var mCodeEdit : EditText? = null
    var mGoodsEdit : EditText? = null
    var confirm : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newcode)
        bind<TextView>(R.id.qcs_title).text = "扫码"
        var clear = bind<TextView>(R.id.qcs_right_text)
        clear.text = "清空"
        mCodeEdit = bind(R.id.code)
        mGoodsEdit = bind(R.id.goods)
        confirm = bind(R.id.confirm)
        clear.setOnClickListener {
            v ->
            DialogUtil().showDialog(this, "确认清空", "取消", "确认", object : DialogUtil.ButtonClickListener {
                override fun OnLeftButtonClick() {
                }

                override fun OnRightButtonClick() {
                    mCodeEdit?.setText("")
                    mGoodsEdit?.setText("")
                    mScanCollection?.data?.clear()
                    mAdapter?.notifyDataSetChanged()
                }
            }, "确认全部已扫描的数据？")
        }
        init(bind(R.id.ext))
        confirm?.setOnClickListener {
            handleDecode()
        }
        bind<View>(R.id.qcs_exit).setOnClickListener {
            finish()
        }

        var watcher = Watcher()
        mCodeEdit?.addTextChangedListener(watcher)
        mGoodsEdit?.addTextChangedListener(watcher)


    }

    var checkTask = object : Worker() {
        override fun work() {
            handleDecode()
        }
    }

    inner class Watcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!TextUtils.isEmpty(mCodeEdit?.text.toString()) && !TextUtils.isEmpty(mGoodsEdit?.text.toString())) {
                TaskUtils.removePostedWork(checkTask)
                TaskUtils.postOnMain(checkTask, 500)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private fun init(v:View) {
        mScanList = v.bind(R.id.codes)
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
    }

    fun handleDecode() {
        mPositonCode = mCodeEdit?.text.toString()
        var goodsInfo = mGoodsEdit?.text.toString()

        if (mPositonCode?.length ?: 0 > 20) {
            DialogUtil.showToast("货位号长度不能大于20", false)
            return
        }

        if (goodsInfo?.length < 20) {
            DialogUtil.showToast("货物码长度不能小于20", false)
            return
        }

        val v = View.inflate(this, R.layout.confirm_code, null)
        val text = v.bind<TextView>(R.id.code_info_text)
        val kuweiText = v.bind<TextView>(R.id.kuwei_text)
        val codeItem = formatCodeResult(goodsInfo)
        if (codeItem != null) {
            var exist = mScanCollection?.data?.contains(codeItem) ?: false
            if (exist) {
                DialogUtil.showToast("数据重复", false)
                return;
            }
            kuweiText.text = "库位码" + mPositonCode
            text.text = "流水号：" + codeItem.id
            val numEdit = v.bind<EditText>(R.id.num_edit)
            numEdit.setText(codeItem.count.toString())
            numEdit.setSelection(numEdit.text.length)
            DialogUtil().showViewDialog(this, "确认信息", "取消", "确认", object : DialogUtil.ButtonClickListener {
                override fun OnRightButtonClick() {
                    codeItem.count = numEdit.text.toString().toFloat()
                    mScanCollection?.data?.add(codeItem)
                    mCodeEdit?.setText("")
                    mGoodsEdit?.setText("")
                    mAdapter?.notifyDataSetChanged()
                }

                override fun OnLeftButtonClick() {
                }
            }, v)
        } else {
            DialogUtil.showToast("无效的货物码", false);
        }
    }

    private fun formatCodeResult(str:String?) : ScanCodeItem? {
        if (str != null ) {
            var item = str.subSequence(1, str.length -1)
            var items = item.split(",")
            if (items != null && items.size == 7) {
                var result = ScanCodeItem()
                result.id = items[6]
                try {
                    result.count = items[3].toFloat()
                } catch (e:Exception) {
                    result.count = 1f
                }
                result.positionCode = mPositonCode
                result.date = ScanUtil.formatCurrent()
                return result
            }
        }
        return null
    }

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
