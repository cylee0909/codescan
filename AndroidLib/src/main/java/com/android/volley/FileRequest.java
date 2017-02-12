package com.android.volley;

import android.text.TextUtils;

import com.android.volley.toolbox.HttpHeaderParser;
import com.cylee.androidlib.util.DirectoryManager;
import com.cylee.androidlib.util.FileUtils;
import com.cylee.androidlib.util.TextUtil;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-10-25
 * Time: 下午5:29
 * To change this template use File | Settings | File Templates.
 */
public class FileRequest extends Request<File> {
    private final String mUrl;
    private final Response.Listener<File> mListener;
    private final String mStorePath;

    public FileRequest(int method,String url, Response.Listener<File> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mUrl = url;
        mStorePath = "";
    }

    public FileRequest(int method,String url, Response.Listener<File> listener, Response.ErrorListener errorListener, String storePath) {
        super(method, url, errorListener);
        mListener = listener;
        mUrl = url;
        mStorePath = storePath;
    }

    @Override
    protected Response<File> parseNetworkResponse(NetworkResponse response) {
        String fileName = TextUtil.md5(mUrl);
        File outFile = new File(DirectoryManager.getDirectory(DirectoryManager.DIR.DATA),fileName);
        FileUtils.writeFile(outFile.getAbsolutePath(), response.data);
        File resultFile = outFile;
        if (!TextUtils.isEmpty(mStorePath)) {
            resultFile = new File(mStorePath);
            if (resultFile.exists()) {
                resultFile.delete();
            }
            try {
                FileUtils.cut(outFile, resultFile);
            } catch (Exception e) {
                resultFile = outFile;
            }
        }
        return Response.success(resultFile, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(File response) {
        if(mListener != null){
            mListener.onResponse(response);
        }
    }
}
