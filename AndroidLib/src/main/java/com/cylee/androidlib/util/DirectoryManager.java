package com.cylee.androidlib.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.cylee.androidlib.base.BaseApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class DirectoryManager {
    public enum DIR {
        IMAGE("image", 10),
        DATA("data", 20),
        ENTITY("entity", 10),
        TMP("tmp", 0),
        CACHE("cache", -1),
        VOICE("voice", 10),
        VIDEO("video", 0),
        LIVE("live", 0),
        VOICE_TMP("voice_tmp", -1),
        AT_BITMAP("askteacher", -1),
        LOG("log", -1),
        SKIN("skin", -1),;
        private String name;
        private int shrinkToPercent;

        /**
         * @param name            目录名称
         * @param shrinkToPercent 该目录被清理时最多占用总存储空间10%*shrinkToPercent/100的字节数（需要手动调用{@link #shrinkDir(DIR...)} 回收空间）
         */
        DIR(String name, int shrinkToPercent) {
            this.name = name;
            this.shrinkToPercent = shrinkToPercent;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    static List<SdcardStatusListener> sdcardStatusListener;
    private static File cacheDirectory;
    private static File baseDirectory;

    public static void init() {
        sdcardStatusListener = new ArrayList<SdcardStatusListener>();
        //监听sd卡卸载和装载，并及时切换缓存目录
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        try {
            BaseApplication.getApplication().getApplicationContext().registerReceiver(new SDCardListenerReceiver(), filter);
        } catch (Exception e) { // catch all exception
            e.printStackTrace();
        }
        shrinkAll();
    }

    public static void addSdCardListener(SdcardStatusListener listener) {
        sdcardStatusListener.add(listener);
    }

    private synchronized static void update(Context context) {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && context.getExternalFilesDir(null) != null) {
                baseDirectory = context.getExternalFilesDir(null);
                cacheDirectory = context.getExternalCacheDir();
                if (cacheDirectory == null || baseDirectory == null) {
                    baseDirectory = context.getFilesDir();
                    cacheDirectory = context.getCacheDir();
                }
            } else {
                baseDirectory = context.getFilesDir();
                cacheDirectory = context.getCacheDir();
            }
        }
        //context.getExternalFilesDir可能抛出NullPointerException
        catch (NullPointerException e) {
            baseDirectory = context.getFilesDir();
            cacheDirectory = context.getCacheDir();
        }
    }

    /**
     * 获得内部定义好的一个文件路径
     *
     * @param dir
     * @return
     */
    public synchronized static File getDirectory(DIR dir) {
        if (cacheDirectory == null || baseDirectory == null) {
            update(BaseApplication.getApplication());
        }
        File file;
        if (dir == DIR.CACHE) {
            file = cacheDirectory;
        } else {
            file = new File(baseDirectory, dir.toString());
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * SD卡状态发生变化时的监听函数
     */
    public interface SdcardStatusListener {
        enum SDCARD_STATUS {
            MEDIA_MOUNTED,
            MEDIA_REMOVED,
            MEDIA_UNMOUNTED,
            MEDIA_BAD_REMOVAL
        }

        void onChange(SDCARD_STATUS status);
    }

    /**
     * 监听SD卡的状态
     */
    static class SDCardListenerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null != action) {
                DirectoryManager.update(context);
                SdcardStatusListener.SDCARD_STATUS status = null;
                if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                    status = SdcardStatusListener.SDCARD_STATUS.MEDIA_MOUNTED;
                } else if (action.equals("android.intent.action.MEDIA_REMOVED")) {
                    status = SdcardStatusListener.SDCARD_STATUS.MEDIA_REMOVED;
                } else if (action.equals("android.intent.action.MEDIA_UNMOUNTED")) {
                    status = SdcardStatusListener.SDCARD_STATUS.MEDIA_UNMOUNTED;
                } else if (action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
                    status = SdcardStatusListener.SDCARD_STATUS.MEDIA_BAD_REMOVAL;
                }
                if (null != status && sdcardStatusListener.size() > 0) {
                    for (SdcardStatusListener listener : sdcardStatusListener) {
                        listener.onChange(status);
                    }
                }
            }
        }
    }

    public static void shrinkAll() {
        shrinkDir(DIR.values());
    }

    /**
     * 检查对应的目录，保证其大小不超过对应的size值，删除旧的文件
     *
     * @param dir
     */
    public static void shrinkDir(final DIR... dir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (DIR di : dir) {
                    synchronized (di) {
                        final int remain = (int) (getDirectory(di).getFreeSpace() * 0.1f * di.shrinkToPercent / 100);
                        if (remain > 0) {
                            File d = getDirectory(di);
                            if (d.isDirectory()) {
                                int totalSize = 0;
                                ArrayList<File> allFiles = new ArrayList<File>();
                                LinkedList<File> filesList = new LinkedList<File>();
                                try {
                                    filesList.addAll(Arrays.asList(d.listFiles()));
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                                while (!filesList.isEmpty()) {
                                    File file = filesList.removeFirst();
                                    if (file.isFile()) {
                                        totalSize += file.length();
                                        allFiles.add(file);
                                    } else {
                                        try {
                                            filesList.addAll(Arrays.asList(file.listFiles()));
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                filesList.clear();
                                if (totalSize > remain) {
                                    Collections.sort(allFiles, new Comparator<File>() {
                                        @Override
                                        public int compare(File lhs, File rhs) {
                                            long offset = rhs.lastModified() - lhs.lastModified();
                                            return offset == 0 ? 0 : (offset > 0 ? 1 : -1);
                                        }
                                    });
                                    while (totalSize > remain && allFiles.size() > 0) {
                                        File file = allFiles.remove(allFiles.size() - 1);
                                        long length = file.length();
                                        if (file.delete()) {
                                            totalSize -= length;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }).start();
    }
}