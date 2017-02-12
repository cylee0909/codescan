package com.cylee.androidlib.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.google.jtm.Gson;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * create by cylee
 */
public class FileUtils {

    private static final int BUFFER_SIZE = 8 * 1024; // 8 KB
    public static final String APP_FOLDER_NAME = "Eyespage";
    public static final String DOWNLOAD_FOLDER_NAME = "Download";
    public static final String CACHE_FOLDER_NAME = "Cache";
    public final static int BYTE_IN_SIZE = 4096;
    public final static String APK_SUFIX_EXT = "i";                  // apk文件扩展符 为了防止第三软件清理掉我们下载的apk
    public final static String APK_SUFIX_I = "apk" + APK_SUFIX_EXT;  // 我们识别的APK后缀13717547184
    public final static String APK_SUFIX = "apk";
    public static final String[] APKS = new String[]{APK_SUFIX_I, APK_SUFIX};

    public static final int MIN_SDCARD_AVIABLE_MIN_FREESPACE = 10;
    private static int isHasSdcard = -1;

    /**
     * 通过文件路劲获取保存到本地的的Model
     *
     * @param path
     * @return
     */
    public static Object unserializeObject(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        ObjectInputStream ois = null;
        Object o = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            o = ois.readObject();
        } catch (Exception e) {
            file.delete();
            return null;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }
        return o;
    }

    /**
     * 将文件序列化并且保存到本地
     *
     * @param path
     * @param o
     * @return
     */
    public static boolean serializeObject(String path, Object o) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(o);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否用写sd卡目录的权限
     *
     * @return
     */
    public static boolean isExternalStorageCanWrite() {
        return Environment.getExternalStorageDirectory().canWrite();
    }

    /**
     * check whether the sd card is mounted on the phone
     *
     * @return whether the sd card is mounted on the phone
     * @author jiayuan
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * get the free space of a specified file path
     *
     * @param path: the specified file path
     * @return the free space size or 0 if the path is not exist or is not a
     * real file path
     */
    public static long getFreeSpace(File path) {
        long size = 0;
        try {
            if (path.exists() && path.isDirectory()) {
                StatFs stat = new StatFs(path.getAbsolutePath());
                size = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
            }
        } catch (Throwable e) {
        }
        return size;
    }

    public static boolean writeFile(String path, byte[] data) {
        if (data == null) {
            return false;
        } else {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(path));
                bos.write(data, 0, data.length);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static boolean writeJsonRecord(String path, ArrayList<JSONObject> content) {
        if (null == content || content.isEmpty()) {
            return false;
        }
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        BufferedWriter writer = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            int len = content.size();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = content.get(i);
                writer.write(jsonObject.toString() + "\n");
            }
            return true;
        } catch (IOException e) {
            //todo  Common Execption Handler;
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeUtfStrings(String path, List<String> content) {

        if (null == content || content.isEmpty()) {
            return false;
        }
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        DataOutputStream writer = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            writer = new DataOutputStream(fos);

            int len = content.size();
            writer.writeInt(len);
            for (int i = 0; i < len; i++) {
                writer.writeUTF(TextUtils.isEmpty(content.get(i)) ? "" : content.get(i));
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeFile(String path, String content) {
        if (null == content) {
            return false;
        }
        BufferedWriter writer = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(content);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] readFile(File file) {
        byte[] result = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            int fileSize = (int) file.length();
            result = new byte[fileSize];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(result, 0, fileSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static boolean delFile(File file) {
        if (null == file) {
            return false;
        }
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static boolean delFile(String path) {
        return delFile(new File(path));
    }

    /**
     * 清空目录的内容（不包括目录本身)
     *
     * @param dir
     */
    public static void clearDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(dir);
                    } else {
                        delFile(file);
                    }
                }
            }
        }
    }

    /**
     * 清空整个目录（包括目录本身)
     *
     * @param dir
     */
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                dir.delete();
                return;
            }
            for (int i = 0; i < files.length; i++) {
                deleteDir(files[i]);
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    public static byte[] readInputStream(InputStream input) {
        byte[] data = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 复制IO流
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {

        if (is == null || os == null) {
            throw new IOException("Argument is null.");
        }

        byte[] bytes = new byte[BUFFER_SIZE];
        while (true) {
            int count = is.read(bytes, 0, BUFFER_SIZE);
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

    /**
     * 复制文件
     *
     * @param src 源文件
     * @param des 目的文件
     * @throws IOException
     */
    public static void copy(File src, File des) throws IOException {
        if (des.exists()) {
            des.delete();
        }
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(des);
        try {
            FileUtils.copyStream(fis, fos);
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
            fos.close();
        }
    }

    /**
     * 剪切文件
     *
     * @param src 源文件
     * @param des 目的文件
     * @throws IOException
     */
    public static void cut(File src, File des) throws IOException {
        copy(src, des);
        delFile(src);
    }

    /**
     * 从Raw资源中读取一张图片到外部存储卡的一个文件
     *
     * @param context
     * @param rawResId
     * @param file
     * @return 读取失败或者外部存储卡不存在返回false
     */
    public static boolean readRawIntoFile(Context context, int rawResId, File file) {
        OutputStream os = null;
        InputStream is = null;
        if (file.exists()) {
            FileUtils.delFile(file);
        }
        try {
            os = new FileOutputStream(file);
            is = context.getResources().openRawResource(rawResId);
            FileUtils.copyStream(is, os);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean unzip(String filePath, String outPath) {
        ZipInputStream inputStream = null;
        FileOutputStream out = null;
        try {
            inputStream = new ZipInputStream(new FileInputStream(filePath));
            File outDirectory = new File(outPath);
            if (!outDirectory.isDirectory()) {
                if (!outDirectory.mkdirs()) {
                    throw new FileNotFoundException();
                }
            }
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = inputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (!entry.isDirectory()) {
                    File file = new File(outPath, name);
                    File folder = file.getParentFile();
                    if (folder == null || (!folder.exists() && !folder.mkdirs())) {
                        throw new FileNotFoundException();
                    }
                    if (file.exists()) {
                        file.delete();
                    }
                    if (!file.createNewFile()) {
                        throw new FileNotFoundException();
                    }
                    out = new FileOutputStream(file);
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                }
            }

            return true;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileUtils.delFile(filePath);
        }
        return false;
    }

    public static boolean writeEntity(Object entity, String filePath) {
        Gson gson = new Gson();
        try {
            if (entity instanceof String) {
                return FileUtils.writeFile(filePath, (String) entity);
            } else {
                String json = gson.toJson(entity);
                return FileUtils.writeFile(filePath, json);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T readEntity(Class<T> clazz, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (clazz == String.class) {
                return (T) FileUtils.readFile(file);
            } else {
                Gson gson = new Gson();
                try {
                    String json = new String(FileUtils.readFile(file));
                    return gson.fromJson(json, clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public static boolean isApk(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze;
            while ((ze = zipInputStream.getNextEntry()) != null) {
                if (ze.getName().equals("AndroidManifest.xml")) {
                    return true;
                }
            }
        } catch (Exception e) {

        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            } catch (IOException e) {
            }
        }
        return false;
    }

    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

    /**
     * 批量压缩文件（夹）
     *
     * @param resFileList 要压缩的文件（夹）列表
     * @param zipFile     生成的压缩文件
     * @param comment     压缩文件的注释
     * @throws IOException 当压缩过程出错时抛出
     */
    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment)
            throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                zipFile), BUFF_SIZE));
        for (File resFile : resFileList) {
            zipFile(resFile, zipout, "");
        }
        zipout.setComment(comment);
        zipout.close();
    }

    /**
     * 批量压缩文件（夹）
     *
     * @param src     要压缩的文件（夹）
     * @param zipFile 生成的压缩文件
     * @param comment 压缩文件的注释
     * @throws IOException 当压缩过程出错时抛出
     */
    public static void zipFile(File src, File zipFile, String comment)
            throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                zipFile), BUFF_SIZE));
        zipFile(src, zipout, "");
        zipout.setComment(comment);
        zipout.close();
    }

    /**
     * 压缩文件
     *
     * @param resFile  需要压缩的文件（夹）
     * @param zipout   压缩的目的文件
     * @param rootpath 压缩的文件路径
     * @throws FileNotFoundException 找不到文件时抛出
     * @throws IOException           当压缩过程出错时抛出
     */
    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath)
            throws FileNotFoundException, IOException {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator)
                + resFile.getName();
        rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            for (File file : fileList) {
                zipFile(file, zipout, rootpath);
            }
        } else {
            byte buffer[] = new byte[BUFF_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(resFile),
                    BUFF_SIZE);
            zipout.putNextEntry(new ZipEntry(rootpath));
            int realLength;
            while ((realLength = in.read(buffer)) != -1) {
                zipout.write(buffer, 0, realLength);
            }
            in.close();
            zipout.flush();
            zipout.closeEntry();
        }
    }


    //  ================================= new added ====================================//

    /**
     * 是否安装了SDCARD
     */
    public static boolean hasSdcard() {
        return hasSdcard(true);
    }

    public static boolean hasSdcard(boolean isGetCache) {
        if (isGetCache) {
            if (isHasSdcard > 0) {
                return true;
            } else if (isHasSdcard == -1) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    isHasSdcard = 1;
                    return true;
                } else {
                    isHasSdcard = 0;
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                isHasSdcard = 1;
                return true;
            } else {
                isHasSdcard = 0;
                return false;
            }
        }
    }

    public static boolean hasFreeSpace() {
        return getStorageFreeSpace()
                > MIN_SDCARD_AVIABLE_MIN_FREESPACE;//<= Device.MIN_SDCARD_AVIABLE_MIN_FREESPACE;
    }

    /**
     * 获取SDCARD的根目录
     */
    private static String getSDCardDir() {
        String mSDCardDir = "";
        if (hasSdcard()) {
            mSDCardDir = Environment.getExternalStorageDirectory().toString();
        } else {
            mSDCardDir = "/mnt/sdcard";
        }
        return mSDCardDir + "/";
    }

    public static String getSdcardDir() {
        File file = new File(getStorageDir() + APP_FOLDER_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getDownloadFilePath() {
        File file = new File(getSdcardDir() + File.separator + DOWNLOAD_FOLDER_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getCacheFilePath() {
        File file = new File(getSdcardDir() + File.separator + CACHE_FOLDER_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取剩余存储空间，单位M
     */
    public static int getStorageFreeSpace() {
        if (getSDCardDir().equals("")) return 0;
        try {
            StatFs stat = new StatFs(getStorageDir());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return (int) (availableBlocks * blockSize / 1024 / 1024);
        } catch (Exception e) {
            return MIN_SDCARD_AVIABLE_MIN_FREESPACE + 1;
        }
    }

    /**
     * 存储空间小于200M时不进行默认下载
     */
    public final static boolean canDownloadApk(double totalSize) {
        return getStorageFreeSpace() >= totalSize + 100;
    }

    /**
     * 获取数据存储目录（末尾带'/'），可能是外接T卡，也可能是内置存储空间
     * 每次调用都会根据规则进行判断，满足用户的存储需求
     */
    public static String getStorageDir() {
        return getSDCardDir(); //??? 暂时不做判断直接用系统返回的
    }

    /**
     * 判断是否为apk
     */
    public final static boolean isApk(String fileName) {
        if (TextUtils.isEmpty(fileName)) return false;
        String sufix = getExt(fileName).toLowerCase();
        return Arrays.asList(APKS).contains(sufix);
    }

    /**
     * 关闭流
     */
    public final static void close(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件是否存在
     */
    public static boolean isExist(String filePathName) {
        if (TextUtils.isEmpty(filePathName)) return false;
        File file = new File(filePathName);
        return (!file.isDirectory() && file.exists());
    }

    public static boolean isDirExist(String filePathName) {
        if (!filePathName.endsWith("/")) filePathName += "/";
        File file = new File(filePathName);
        return (file.isDirectory() && file.exists());
    }

    /**
     * 创建目录，整个路径上的目录都会创建
     */
    public static void createDirWithFile(String path) {
        File file = new File(path);
        if (!path.endsWith("/")) {
            file = file.getParentFile();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 获取路径，不带文件名，末尾带'/'
     */
    public static String getPath(String filePathName) {
        try {
            return filePathName.substring(0, filePathName.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取目录的名称 注意：只能获取如：/aaaa/ssss/ 或 /aaaa/dsddd
     */
    public static String getDirPathName(String filePathName) {
        try {
            if (filePathName.endsWith("/")) {
                filePathName = filePathName.substring(0, filePathName.lastIndexOf('/'));
            }
            return filePathName.substring(filePathName.lastIndexOf("/") + 1, filePathName.length());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取文件名，带后缀
     */
    public static String getName(String filePathName) {
        try {
            return filePathName.substring(filePathName.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取文件名，不带后缀
     */
    public static String getNameNoPostfix(String filePathName) {
        try {
            return filePathName.substring(filePathName.lastIndexOf('/') + 1,
                    filePathName.lastIndexOf('.'));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getExt(String filePathName) {
        if (filePathName == null) return null;
        final int index = filePathName.lastIndexOf('.');
        return (index != -1) ? filePathName.substring(index + 1).toLowerCase().intern() : "";
    }

    /**
     * 设置文件的修改时间
     *
     * @param filePathName 文件路径
     * @param gmtTime      GMT时间串
     */
    public static boolean setLastModified(String filePathName, String gmtTime) {
        try {
            long millstime = getMillsTime(gmtTime);
            if (millstime < 0) return false;
            File file = new File(filePathName);
            return file.setLastModified(millstime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取文件的最后修改时间（GMT)
     *
     * @param filePathName 文件
     * @return 文件不存在直接返回null，否则返回GMT时间
     */
    public static String getLastModifiedGMT(String filePathName) {
        File file = new File(filePathName);
        if (file.exists()) {
            long millsTime = file.lastModified();
            return getGMTTimeString(millsTime);
        } else {
            return null;
        }
    }

    /**
     * 获取文件最后修改时间
     *
     * @return 文件不存在直接返回-1,else return milliseconds since January 1st, 1970
     */
    public static long getLastModified(String filePathName) {
        File file = new File(filePathName);
        if (file != null && file.exists()) {
            return file.lastModified();
        } else {
            return -1;
        }
    }

    /**
     * 重命名
     */
    public static boolean rename(String filePathName, String newPathName) {
        if (TextUtils.isEmpty(filePathName)) return false;
        if (TextUtils.isEmpty(newPathName)) return false;

        delete(newPathName);
        File file = new File(filePathName);
        File newFile = new File(newPathName);
        File parentFile = newFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        return file.renameTo(newFile);
    }

    /**
     * 删除文件
     */
    public static void delete(String filePathName) {
        if (TextUtils.isEmpty(filePathName)) return;
        File file = new File(filePathName);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    /**
     * 创建目录，整个路径上的目录都会创建
     */
    public static void createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 尝试创建空文件
     * <br> 如果文件已经存在不操作,返回true
     *
     * @param path 路径
     * @return 如果创建失败(Exception) 返回false，否则true
     */
    public static boolean createEmptyFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取文件大小
     */
    public static long getSize(String filePathName) {
        if (TextUtils.isEmpty(filePathName)) return 0;
        File file = new File(filePathName);
        if (file.isFile()) return file.length();
        return 0;
    }

    /**
     * 获取文件大小 M
     */
    public static String getFileSize(String filePathName) {
        long fileSize = getSize(filePathName);
        String size = "";
        double kSize = fileSize / 1024.0f;
        kSize = kSize / 1024.0f;
        DecimalFormat df = new DecimalFormat("0.00");
        return String.valueOf(df.format(kSize)) + "MB";
    }

    /**
     * 读取文件数据到byte数组
     *
     * @param filePathName 文件名
     * @param readOffset   从哪里开始读
     * @param readLength   读取长度
     * @param dataBuf      保存数据的缓冲区
     * @param bufOffset    从哪里保存
     */
    public static boolean readData(String filePathName, int readOffset, int readLength,
                                   byte[] dataBuf, int bufOffset) {
        try {
            int readedTotalSize = 0;
            int onceReadSize = 0;

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePathName));
            in.skip(readOffset);
            while (readedTotalSize < readLength
                    && (onceReadSize =
                    in.read(dataBuf, bufOffset + readedTotalSize, readLength - readedTotalSize)) >= 0) {
                readedTotalSize += onceReadSize;
            }
            in.read(dataBuf, bufOffset, readLength);
            in.close();
            in = null;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 将某个流的内容输出到文件
     *
     * @param in           输入流
     * @param filePathName 目标文件
     */
    public static boolean writeFile(InputStream in, String filePathName) {
        boolean flag = false;
        OutputStream outStream = null;
        try {
            File destFile = new File(filePathName);
            if (destFile.exists()) {
                destFile.delete();
            } else {
                destFile.createNewFile();
            }
            outStream = new BufferedOutputStream(new FileOutputStream(filePathName));
            byte[] buffer = new byte[1024];
            int count = 0;
            while (true) {
                int length = in.read(buffer, 0, 1024);
                if (length > 0) {
                    outStream.write(buffer, 0, length);
                } else {
                    break;
                }
                count += length;
            }
            if (count > 0) {
                flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    /**
     * 将字节数据写入到文件
     * <br>覆盖
     */
    public static boolean writeFile(byte[] data, String filePathName) {
        OutputStream outStream = null;
        try {
            File destFile = new File(filePathName);
            if (destFile.exists()) {
                destFile.delete();
            } else {
                destFile.createNewFile();
            }
            outStream = new BufferedOutputStream(new FileOutputStream(filePathName));
            outStream.write(data);
            outStream.flush();
        } catch (Exception ex) {
            return false;
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    /**
     * 判断当前字符串是否为空
     */
    public static boolean isNullString(String str) {
        if (str == null || str.equals("")) return true;
        return false;
    }

    /**
     * 复制文件
     */
    public static int copy(String fromPathName, String toPathName) {
        try {
            InputStream from = new FileInputStream(fromPathName);
            return copy(from, toPathName);
        } catch (FileNotFoundException e) {
            return -1;
        }
    }

    /**
     * 复制文件
     */
    public static int copy(InputStream from, String toPathName) {
        OutputStream to = null;
        try {
            delete(toPathName);
            to = new BufferedOutputStream(new FileOutputStream(toPathName));
            byte buf[] = new byte[1024];
            int c;
            while ((c = from.read(buf)) > 0) {
                to.write(buf, 0, c);
            }
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            close(to);
            close(from);
        }
    }

    /**
     * 根据zip文件路径转换为文件路径
     *
     * @param zipFullPath 必须带.zip
     */
    public static String zip2FileFullPath(String zipFullPath) {
        int zipIndex = zipFullPath.lastIndexOf(".zip");
        int zipIndexTmp = zipFullPath.lastIndexOf(".ZIP");
        String tmp = "";
        if (zipIndex > -1) {
            tmp = zipFullPath.substring(0, zipIndex);
        } else if (zipIndexTmp > -1) {
            tmp = zipFullPath.substring(0, zipIndexTmp);
        }
        return tmp;
    }

    /**
     * 改变文件权限
     */
    public static void chmod(String permission, String filePathName) {
        try {
            String command = "chmod " + permission + " " + filePathName;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过NIO的方式拷贝文件
     * <br>提高性能
     */
    public static void copyByNIO(File source, File target) {
        try {
            FileInputStream fin = new FileInputStream(source);
            FileOutputStream fout = new FileOutputStream(target);
            ByteBuffer buffer = ByteBuffer.allocate(512);
            FileChannel ch1 = fin.getChannel();
            FileChannel ch2 = fout.getChannel();
            while (ch1.read(buffer) != -1) { // 从通道中读取内容到缓冲区
                buffer.flip(); // 让缓冲区回到初始位置才能进行写操作
                ch2.write(buffer);
                buffer.clear(); // 清空缓冲区
            }
            ch1.close();
            ch2.close();
            fin.close();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFilesInDirectory(String dirPath) {
        File dirF = new File(dirPath);
        File[] files = dirF.listFiles();
        for (File fileF : files) {
            if (fileF.isFile()) {
                fileF.delete();
            }
        }
    }

    public synchronized static void deleteDirectory(File file) {
        if (!file.isDirectory()) {
            return;
        }
        File[] paths = file.listFiles();
        for (File pathF : paths) {
            if (pathF.isDirectory()) {
                deleteDirectory(pathF);
            } else {
                pathF.delete();
            }
        }
        file.delete();
    }

    /**
     * 获取某个文件夹下某种文件的最大版本号
     * <br>把文件名作为版本号的话返回最大版本号，
     * 如:1.zip、2.zip、3.zip 返回3
     *
     * @param dir 文件夹
     * @param ext 被查找的文件扩展名 如.zip
     * @return 最大的那个版本号, 如果没有找到返回 -1
     */
    public static int getMaxVersion(String dir, final String ext) {
        File path = new File(dir);
        int maxVersion = -1;
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    if (filename != null && filename.endsWith(ext)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        String fileName = getNameNoPostfix(files[i].getAbsolutePath());
                        int ver = Integer.parseInt(fileName);
                        if (ver > maxVersion) maxVersion = ver;
                    } catch (Exception ex) {

                    }
                }
            }
        }
        return maxVersion;
    }

    /**
     * 获取某个文件夹下某种文件的最大版本号文件
     * <br>把文件名作为版本号的话返回最大版本号文件，
     * 如:1.zip、2.zip、3.zip 返回/xx/xx/3.zip
     *
     * @param dir 文件夹
     * @param ext 被查找的文件扩展名 如.zip
     * @return 最大的那个版本号的文件路径, 如果没有找到返回 null
     */
    public static String getMaxVersionFile(String dir, final String ext) {
        File path = new File(dir);
        String maxVerFile = null;
        int maxVersion = -1;
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.endsWith(ext)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        String fileName = getNameNoPostfix(files[i].getAbsolutePath());
                        int ver = Integer.parseInt(fileName);
                        if (ver > maxVersion) {
                            maxVersion = ver;
                            maxVerFile = files[i].getAbsolutePath();
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        }

        return maxVerFile;
    }

    /**
     * 获取某个文件夹下某种文件的最大版本号文件夹
     * <br>把文件名作为版本号的话返回最大版本号文件夹，
     * 如:/xx/xx/1/、/xx/xx/2/、/xx/xx/3/ 返回/xx/xx/3/
     *
     * @param dir 文件夹
     * @return 最大的那个版本号的文件夹路径, 如果没有找到返回 null
     */
    public static String getMaxVersionDir(String dir) {
        File path = new File(dir);
        String maxVerDir = null;
        int maxVersion = -1;
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        String fileName = files[i].getName();
                        int ver = Integer.parseInt(fileName);
                        if (ver > maxVersion) {
                            maxVersion = ver;
                            maxVerDir = files[i].getAbsolutePath() + "/";
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        }

        return maxVerDir;
    }

    public static int getMaxVersion(String dir) {
        File path = new File(dir);
        int maxVersion = -1;
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        String fileName = files[i].getName();
                        int ver = Integer.parseInt(fileName);
                        if (ver > maxVersion) {
                            maxVersion = ver;
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        }

        return maxVersion;
    }

    public final static String read(String filePath) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) return "";
            baos = new ByteArrayOutputStream();
            bis = new BufferedInputStream(new FileInputStream(file));
            int length = 0;
            byte[] buffer = new byte[BYTE_IN_SIZE];
            while ((length = bis.read(buffer, 0, BYTE_IN_SIZE)) > 0) {
                baos.write(buffer, 0, length);
            }
            if (baos.size() > 0) {
                return new String(baos.toByteArray(), "UTF-8");
            }
        } catch (Exception e) {
        } finally {
            close(bis);
            close(baos);
        }
        return "";
    }

    /**
     * 获得GMT时间字符串
     *
     * @param timemills 1970以来的毫秒数
     */
    public static final String getGMTTimeString(long timemills) {
        Date date = new Date(timemills);
        return date.toGMTString();
    }

    /**
     * 获取1970以来的毫秒数
     *
     * @param gmtTime GMT时间字符串
     * @throws Exception
     */
    public static final long getMillsTime(String gmtTime) throws Exception {
        Date date = org.apache.http.impl.cookie.DateUtils.parseDate(gmtTime);
        return date.getTime();
    }

    public static String getImagePath(Context context, Uri uri) {
        String filePath = getPath(context, uri);
        if (filePath != null && !MediaFileUtils.isImageFileType(filePath)) filePath = null;
        return filePath;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri =
                        ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
