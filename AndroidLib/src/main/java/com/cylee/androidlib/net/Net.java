package com.cylee.androidlib.net;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.android.volley.ErrorCode;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ResponseContentError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.DrawableLoader;
import com.android.volley.toolbox.FileDownloader;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.RetryPolicyFactory;
import com.cylee.androidlib.base.BaseActivity;
import com.cylee.androidlib.base.Callback;
import com.cylee.androidlib.thread.Worker;
import com.cylee.androidlib.util.DirectoryManager;
import com.cylee.androidlib.util.FileUtils;
import com.cylee.androidlib.util.NetUtils;
import com.cylee.androidlib.util.TaskUtils;
import com.cylee.androidlib.util.TextUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Net {
    private static final String DEFAULT_CACHE_DIR = "volley";
    private static AtomicInteger REQUEST_ID = new AtomicInteger(0);
    private static RequestQueue requestQueue;
    private static HttpStack stack;
    private static HashMap<String, String> COMMON_PARAMS = new HashMap<String, String>();
    private static FileDownloader mFileDownloader;
    private static Network network;

    public static synchronized FileDownloader getFileDownloader() {
        return mFileDownloader;
    }

    /**
     * 自定义Listener，实现回调的弱引用
     *
     * @param <T>
     */
    public abstract static class SuccessListener<T> implements Response.Listener<T> {
        WeakReference<SuccessListener<T>> successListenerWeakReference;
        WeakReference<Context> contextWeakReference;

        public abstract void onResponse(T response);

        public void onCacheResponse(T response) {
        }
    }

    /**
     * 自定义error listner
     */
    public abstract static class ErrorListener implements Response.ErrorListener {
        WeakReference<ErrorListener> errorListenerWeakReference;
        WeakReference<Context> contextWeakReference;

        public abstract void onErrorResponse(NetError e);

        final public void onErrorResponse(VolleyError error) {
            if (error != null) {
                if (error instanceof TimeoutError) {
                    onErrorResponse(new NetError(ErrorCode.CLIENT_TIMEOUT_EXCEPTION, error));
                } else if (error instanceof ResponseContentError) {
                    onErrorResponse(new NetError(((ResponseContentError) error).getErrorCode(), error));
                } else {
                    onErrorResponse(new NetError(ErrorCode.CLIENT_NET_EXCEPTION, error));
                }
            }
        }
    }

    /**
     * 初始化网络模块
     *
     * @param context
     */
    public static synchronized void init(Context context) {
        COMMON_PARAMS.put("os", "android");
        COMMON_PARAMS.put("sdk", String.valueOf(Build.VERSION.SDK_INT));
        if (requestQueue != null) {
            requestQueue.stop();
        }
        requestQueue = getRequestQueue(context);
        mFileDownloader = new FileDownloader(requestQueue);
        ImageViewUtils.init();
    }

    /**
     * 给URL添加通用参数
     *
     * @param url
     * @return
     */
    public static String appendCommonParams(String url) {
        StringBuilder sb = new StringBuilder();
        Iterator<HashMap.Entry<String, String>> iterator = COMMON_PARAMS.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey()).append("=").append(TextUtil.encode(entry.getValue())).append("&");
        }
        if (url.contains("?")) {
            url += (url.endsWith("?")) ? sb.toString() : "&" + sb.toString();
        } else {
            url += "?" + sb.toString();
        }
        return url;
    }


    /**
     * 同步提交数据，在当前线程执行网络请求，返回结果，initAntispam时调用，不检查缓存，直接进行请求
     * 这样做，可能导致进程中有多于volley网络线程的线程同时执行网络请求，但现在只有在初始化antispam时才会调用这个方法，
     * 而antispam
     *
     * @param input
     * @param clazz
     * @param <T>
     * @return 如果请求失败，返回null
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static <T> T postSync(InputBase input, Class<T> clazz) throws ExecutionException, InterruptedException {
        // 这样做将一个请求交给volley，等待一个volley网络线程去进行请求，然后当前线程陷入等待。
        // 一般来说这样没什么问题，但如果在volley网络线程调用这个方法的话，就可能陷入这样一种情况：
        // 所有的volley网络线程都在同一时刻调用了这个方法，它们都等待着其他volley线程来处理请求，于是全部陷入等待。
//        RequestFuture<T> future = RequestFuture.newFuture();
//        Request<T> request = HWRequest.newRequest(input, future, future);
//        request.setUrl(appendCommonParams(input.toString()));
//        request.setRetryPolicy(RetryPolicyFactory.getRetryPolicy(RetryPolicyFactory.RETRY_POLICY.NORMAL));
//        requestQueue.add(request);
//        return future.get();

        // 这里将原本由NetworkDispatcher线程做的事情拿了出来，直接在这里来做，
        // 于是当前线程不再等待volley网络线程，而是自己做了volley网络线程做的事。
        HWRequest<T> request = HWRequest.newRequest(input, null, null);
        request.setUrl(appendCommonParams(input.toString()));
        request.setRetryPolicy(RetryPolicyFactory.getRetryPolicy(RetryPolicyFactory.RETRY_POLICY.NORMAL));
        NetworkResponse networkResponse = null;
        try {
            networkResponse = network.performRequest(request);
        } catch (VolleyError volleyError) {
            volleyError.printStackTrace();
            return null;
        }

        if (networkResponse == null) {
            return null;
        }

        Response<T> response = request.parseNetworkResponse(networkResponse);
        if (!response.isSuccess()) {
            return null;
        } else {
            return response.result;
        }
    }

    /**
     * 发送一个普通的文本请求
     *
     * @param context
     * @param input
     * @param success
     * @param error
     * @param <T>
     * @return
     */
    public static <T> Request<?> post(final Context context, InputBase input, SuccessListener<T> success, ErrorListener error) {
        return postRequest(context, input, null, null, null, success, error);
    }


    /**
     * 发送一个文件上传请求
     *
     * @param context
     * @param input
     * @param filename
     * @param file
     * @param success
     * @param error
     * @param <T>
     * @return
     */
    public static <T> Request<?> post(final Context context, InputBase input, String filename, File file, SuccessListener<T> success, ErrorListener error) {
        return postRequest(context, input, filename, file, null, success, error);
    }


    /**
     * 发送一个byte数组请求
     *
     * @param context
     * @param input
     * @param filename
     * @param fileBytes
     * @param success
     * @param error
     * @param <T>
     * @return
     */
    public static <T> Request<?> post(final Context context, InputBase input, String filename, byte[] fileBytes, SuccessListener<T> success, ErrorListener error) {
        return postRequest(context, input, filename, null, fileBytes, success, error);
    }

    /**
     * 判断URL的合法性
     *
     * @param input
     * @return
     */
    private static boolean isUrlValid(InputBase input) {
        if (input == null) {
            return false;
        }
        String url = input.toString();
        return !(url == null || (!url.startsWith("http") && !url.startsWith("https")));
    }


    /**
     * 统一发送接口函数，不对外调用
     *
     * @param context
     * @param input
     * @param filename
     * @param file
     * @param fileBytes
     * @param success
     * @param error
     * @param <T>
     * @return
     */
    private static <T> Request<?> postRequest(final Context context, final InputBase input, String filename, File file, byte[] fileBytes, SuccessListener<T> success, ErrorListener error) {
        // 检查URL合法性
        if (!isUrlValid(input)) {
            if (error != null) {
                error.onErrorResponse(new NetError(ErrorCode.CLIENT_URL_INVALID_EXCEPTION, ""));
            }
        }

        Object[] listeners = createDelegateListener(context, success, error, input, null);
        final SuccessListener<T> successListener = (SuccessListener<T>) listeners[0];
        final ErrorListener errorListener = (ErrorListener) listeners[1];

        // 有缓存第一个页面，调onCahceResponse
        if (input.needCache) {
            Entity<T> entity = new Entity<T>(input);
            T data = entity.read();
            if (data != null) {
                successListener.onCacheResponse(data);
            }
        }

        // 区分不同的请求类型，创建request
        final HWRequest<?> request;
        if (file != null) {
            request = HWRequest.newFileRequest(input, filename, file, successListener, errorListener);
        } else if (fileBytes != null) {
            request = HWRequest.newByteRequest(input, filename, fileBytes, successListener, errorListener);
        } else {
            // 普通的文本请求，需要考虑第一页缓存
            request = HWRequest.newRequest(input, new Response.Listener<T>() {
                @Override
                public void onResponse(T response) {
                    if (input.needCache) {
                        Entity<T> entity = new Entity<T>(input);
                        entity.save(response, null);
                    }
                    if (successListener != null) {
                        successListener.onResponse(response);
                    }
                }
            }, errorListener);
        }
        request.setTag(new WeakReference<Context>(context));
        // 发送请求前，需要判断AntiSpam是否初始化成功了,另外，获取host配置的接口不需要判断是否成功
        request.setUrl(appendSign(input));
        requestQueue.add(request);
        return request;
    }

    /**
     * 生成获取签名
     */
    private static String appendSign(InputBase input) {
        StringBuilder fullUrl = new StringBuilder();
        fullUrl.append(appendCommonParams(input.toString()));
        //用来计算sign的所有参数数组
        ArrayList<String> signCalcParams = new ArrayList<String>(COMMON_PARAMS.size() + input.getParams().size() + 2);
        Iterator<Map.Entry<String, Object>> iter = input.getParams().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            Object value = entry.getValue();
            if (value instanceof Enum) {
                value = ((Enum) value).ordinal();
            }
            signCalcParams.add(entry.getKey() + "=" + value);
        }
        Iterator<HashMap.Entry<String, String>> iterator = COMMON_PARAMS.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            signCalcParams.add(entry.getKey() + "=" + entry.getValue());
        }
        String nt = "nt=" + (NetUtils.isWifiConnected() ? "wifi" : "mobile");
        signCalcParams.add(nt);
        fullUrl.append("&").append(nt);
        return fullUrl.toString();
    }


    /**
     * 根据调用请求的context自动生成对应的代理Listener方法，当context为BaseActivity或者BaseFragmentActivity时，建立弱引用，当context为BaseApplication时，建立强引用
     *
     * @param context
     * @param success
     * @param error
     * @param input
     * @param inputUrl download时的url
     * @param <T>
     * @return
     */
    private static <T> Object[] createDelegateListener(Context context, SuccessListener<T> success, ErrorListener error, final InputBase input, final String inputUrl) {
        final int requestId1 = REQUEST_ID.addAndGet(1);
        final int requestId2 = REQUEST_ID.addAndGet(1);
        //当请求使用的context不是Activity时，直接底层强行持有listener，保证函数回调的执行
        if (!(context instanceof BaseActivity)) {
            return new Object[]{success, error};
        }

        //创建一个代理listener，内部用弱引用持有listener对象，这样底层网络库不会强行持有listener
        final SuccessListener<T> successListener = new SuccessListener<T>() {
            @Override
            public void onResponse(T response) {
                Context context = this.contextWeakReference.get();
                SuccessListener<T> successListener = this.successListenerWeakReference.get();
                if (context != null) {
                    if (context instanceof BaseActivity) {
                        ((BaseActivity) context).removeListenerRef(requestId1);
                        ((BaseActivity) context).removeListenerRef(requestId2);
                    }
                }

                //网络请求返回后，如果有第一页缓存刚好对应，替换它
                if (input != null) {//非download才需要缓存
                    Entity<T> entity = new Entity<T>(input);
                    if (entity.exists()) {
                        entity.save(response, null);
                    }
                }

                if (context != null && successListener != null) {
                    if (context instanceof Activity && ((Activity) context).isFinishing()) {
                        return;
                    }
                    try {
                        successListener.onResponse(response);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCacheResponse(T response) {
                Context context = this.contextWeakReference.get();
                SuccessListener<T> successListener = this.successListenerWeakReference.get();
                if (input != null && context != null && successListener != null) {
                    if (context instanceof Activity && ((Activity) context).isFinishing()) {
                        return;
                    }
                }
            }
        };
        final ErrorListener errorListener = new ErrorListener() {
            @Override
            public void onErrorResponse(NetError e) {
                Context context = this.contextWeakReference.get();
                ErrorListener errorListener = this.errorListenerWeakReference.get();
                if (context != null) {
                    if (context instanceof BaseActivity) {
                        ((BaseActivity) context).removeListenerRef(requestId1);
                        ((BaseActivity) context).removeListenerRef(requestId2);
                    }
                }
                if (context != null && errorListener != null) {
                    if (context instanceof Activity && ((Activity) context).isFinishing()) {
                        return;
                    }
                    try {
                        errorListener.onErrorResponse(e);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        };
        errorListener.errorListenerWeakReference = new WeakReference<ErrorListener>(error);
        errorListener.contextWeakReference = new WeakReference<Context>(context);
        successListener.successListenerWeakReference = new WeakReference<SuccessListener<T>>(success);
        successListener.contextWeakReference = new WeakReference<Context>(context);
        ((BaseActivity) context).addListenerRef(requestId1, success);
        ((BaseActivity) context).addListenerRef(requestId2, error);
        return new Object[]{successListener, errorListener};
    }

    /**
     * 取消某个请求
     *
     * @param context
     */
    public static void cancelByContextRef(final Context context) {
        if (requestQueue != null) {
            requestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    if (request.getTag() instanceof WeakReference) {
                        try {
                            WeakReference<Context> contextWeakReference = (WeakReference<Context>) request.getTag();
                            return contextWeakReference.get() == context;
                        } catch (ClassCastException e) {
                            return false;
                        }
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 设置proxy
     *
     * @param proxy
     */
    public static void setProxy(Proxy proxy) {
        if (stack != null) {
            stack.setProxy(proxy);
        }
    }

    private static RequestQueue getRequestQueue(Context context) {
        Proxy proxy = NetUtils.getProxy();
        File cacheDir = new File(DirectoryManager.getDirectory(DirectoryManager.DIR.CACHE), DEFAULT_CACHE_DIR);
        stack = new HurlStack(proxy);
        network = new BasicNetwork(stack);

        final DiskBasedCache diskCache = new DiskBasedCache(cacheDir);
        //监听目录的变化，切换cache目录
        DirectoryManager.addSdCardListener(new DirectoryManager.SdcardStatusListener() {
            @Override
            public void onChange(SDCARD_STATUS status) {
                TaskUtils.doRapidWork(new Worker() {
                    @Override
                    public void work() {
                        diskCache.switchCache(new File(DirectoryManager.getDirectory(DirectoryManager.DIR.CACHE), DEFAULT_CACHE_DIR));
                    }
                });
            }
        });

        RequestQueue queue = new RequestQueue(diskCache, network);
        queue.start();
        return queue;
    }

    public static DrawableLoader getDrawableLoader() {
        return ImageViewUtils.getDrawableLoader();
    }

    /**
     * 销毁NET对象
     */
    public static void destroy() {
        if (requestQueue != null) {
            requestQueue.stop();
            requestQueue = null;
        }
        ImageViewUtils.destroy();
    }

    public static synchronized void emptyImageCache() {
        ImageViewUtils.emptyCache();
    }

    /**
     * 本地缓存实体对象
     */
    public static class Entity<T> {

        private InputBase input;

        public Entity(InputBase ipt) {
            input = ipt;
        }

        /**
         * 获取实体key
         *
         * @return
         */
        private String createKey() {
            return TextUtil.md5(appendCommonParams(input.toString()));
        }

        /**
         * 读取当前url的本地实体内容
         *
         * @return
         */
        public T read() {
            String key = createKey();
            String cacheFilePath = new File(DirectoryManager.getDirectory(DirectoryManager.DIR.ENTITY), key).getAbsolutePath();
            return (T) FileUtils.readEntity(input.aClass, cacheFilePath);
        }

        /**
         * 异步读取本地实体内容
         *
         * @return
         */
        public void readAsyn(final Callback<T> callback) {
            AsyncTask<Object, Object, T> task = new AsyncTask<Object, Object, T>() {
                @Override
                protected T doInBackground(Object... params) {
                    return (T) read();
                }

                @Override
                protected void onPostExecute(T o) {
                    if (null != callback) {
                        callback.callback(o);
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }

        /**
         * 是否有本地缓存实体
         *
         * @return
         */
        public boolean exists() {
            return new File(DirectoryManager.getDirectory(DirectoryManager.DIR.ENTITY), createKey()).exists();
        }


        /**
         * 保存缓存实体到本地文件
         *
         * @param entity
         * @param callback
         */
        public void save(final Object entity, final Callback<Boolean> callback) {
            AsyncTask<Object, Object, Boolean> task = new AsyncTask<Object, Object, Boolean>() {
                @Override
                protected Boolean doInBackground(Object... params) {
                    String entityKey = createKey();
                    File tmpFile = new File(DirectoryManager.getDirectory(DirectoryManager.DIR.ENTITY), entityKey + ".tmp");
                    File cacheFile = new File(DirectoryManager.getDirectory(DirectoryManager.DIR.ENTITY), entityKey);
                    if (entity != null) {
                        FileUtils.writeEntity(entity, tmpFile.getAbsolutePath());
                        if (tmpFile.exists()) {
                            tmpFile.renameTo(cacheFile);
                        }
                        FileUtils.delete(tmpFile.getAbsolutePath());
                    } else {
                        return FileUtils.delFile(cacheFile);
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean o) {
                    if (null != callback) {
                        callback.callback(o);
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        }
    }


    /**
     * 处理与HTTP加载图片相关操作
     */
    private static class ImageViewUtils {
        private static DrawableLoader.ImageCache memoryCache;
        private static DrawableLoader drawableLoader;

        public static DrawableLoader getDrawableLoader() {
            return drawableLoader;
        }

        public static void init() {
            memoryCache = new ImageLruCache();
            drawableLoader = new DrawableLoader(requestQueue, memoryCache);
        }

        public static void destroy() {
            drawableLoader = null;
            ((ImageLruCache) memoryCache).evictAll();
            memoryCache = null;
        }

        public static void emptyCache() {
            if (memoryCache != null) {
                ((ImageLruCache) memoryCache).evictAll();
            }
        }
    }

}
