package com.cylee.androidlib.net;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.BitmapTransformerFactory;
import com.android.volley.toolbox.DrawableLoader;
import com.android.volley.toolbox.ImageDrawableCreator;

/**
 * Sub-class of ImageView which automatically notifies the drawable when it is
 * being displayed.
 */
public class RecyclingImageView extends ImageView {
    private BindCallback bindCallback;
    private boolean isBindCallbackInvoked;
    private boolean isNormalImageView = false;
    /**
     * 内部存储绑定的url地址
     */
    private String url;
    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int defaultImageId;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int errorImageId;

    /** Local copy of the ImageLoader. */
    private DrawableLoader drawableLoader;
    private DrawableLoader.ImageContainer imageContainer;
    private BitmapTransformerFactory.BitmapTransformer transformer;
    private ScaleType defaultScaleType,errorScaleType,successScaleType;
    private boolean resizeBitmap = true;

    public void setScaleTypes(ScaleType defaultScaleType,ScaleType errorScaleType,ScaleType successScaleType){
        this.defaultScaleType = defaultScaleType;
        this.errorScaleType = errorScaleType;
        this.successScaleType = successScaleType;
    }

    public RecyclingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs,defStyle);
        init(attrs,context);
    }
    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,context);
    }
    public RecyclingImageView(Context context) {
        super(context);
        init(null,context);
    }
    private void init(AttributeSet attrs,Context context){
        //3.0的硬件加速会使某些手机显示黑色
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            setLayerType(LAYER_TYPE_SOFTWARE,null);
        }*/
        if(attrs != null){
//            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecyclingImageView);
//            if (typedArray != null) {
//                resizeBitmap = typedArray.getBoolean(R.styleable.RecyclingImageView_resize_bitmap,resizeBitmap);
//                typedArray.recycle();
//            }
        }
    }
    /**
     * 绑定一个图片到该ImageView上
     * @param url 图片地址
     * @param defaultImageId 默认图片资源，如果不需要，传0
     * @param errorImageId 出错的图片资源，如果不需要，传0
     * @param transformer 图片转换器
     */
    public void bind(String url,int defaultImageId,int errorImageId,BitmapTransformerFactory.BitmapTransformer transformer){
        bind(url, defaultImageId, errorImageId, transformer, null);
    }
    /**
     * 绑定一个图片到该ImageView上
     * @param url 图片地址
     * @param defaultImageId 默认图片资源，如果不需要，传0
     * @param errorImageId 出错的图片资源，如果不需要，传0
     */
    public void bind(String url,int defaultImageId,int errorImageId){
        bind(url,defaultImageId,errorImageId,null);
    }
    /**
     * 绑定一个图片到该ImageView上
     * @param url 图片地址(支持网络图片http,本地图片file:,asset图片file:///android_asset)
     * @param defaultImageId 默认图片资源，如果不需要，传0
     * @param errorImageId 出错的图片资源，如果不需要，传0
     * @param transformer 图片转换器
     * @param bindCallback 一个绑定图片的回调函数,只会在第一次绑定成功或失败后调用一次
     */
    public void bind(String url, int defaultImageId, int errorImageId, BitmapTransformerFactory.BitmapTransformer transformer, BindCallback bindCallback){
        this.drawableLoader = Net.getDrawableLoader();
        this.isBindCallbackInvoked = false;
        this.url = url;
        this.defaultImageId = defaultImageId;
        this.errorImageId = errorImageId;
        this.transformer = transformer;
        this.bindCallback = bindCallback;
        this.isNormalImageView = false;
        loadImageIfNecessary(false);
    }

    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        if(isNormalImageView || getLayoutParams() == null){
            return;
        }
        final int width = getWidth()-getPaddingLeft()-getPaddingRight();
        final int height = getHeight()-getPaddingTop()-getPaddingBottom();

        ViewGroup.LayoutParams lp = getLayoutParams();
        boolean isFullyWrapContent = (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT || lp.height == ViewGroup.LayoutParams.MATCH_PARENT)
                && (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT || lp.width == ViewGroup.LayoutParams.MATCH_PARENT);
        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        final ImageDrawableCreator creator;
        if (width <= 0 && height <= 0 && !isFullyWrapContent) {
            return;
        }else{
            if(!resizeBitmap || (getLayoutParams().height == LinearLayout.LayoutParams.WRAP_CONTENT || getLayoutParams().width == LinearLayout.LayoutParams.WRAP_CONTENT)){
                creator = new ImageDrawableCreator(0,0, Bitmap.Config.RGB_565,transformer);
            }else{
                creator = new ImageDrawableCreator(width,height, Bitmap.Config.RGB_565,transformer);
            }
        }
        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(url)) {
            clearImageContainer();
            // Remove this for supporting displaying local images
            if(errorScaleType != null){
                setScaleType(errorScaleType);
            }
            if(errorImageId != 0){
                internalSetImageResource(errorImageId);
            }
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (imageContainer != null && imageContainer.getRequestUrl() != null) {
            if (imageContainer.getRequestUrl().equals(url)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                imageContainer.cancelRequest();
                internalSetImageDrawable(null);
            }
        }

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        DrawableLoader.ImageContainer newContainer = drawableLoader.get(url,creator,
                new DrawableLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(errorScaleType != null){
                            setScaleType(errorScaleType);
                        }
                        if (errorImageId != 0) {
                            internalSetImageResource(errorImageId);
                            invokeCallback(false,null);
                        }
                    }

                    @Override
                    public void onResponse(final DrawableLoader.ImageContainer response, boolean isImmediate) {
                        final Drawable d = response.getDrawable();
                        //立即返回的结果(可能是直接命中缓存或者没有命中缓存)
                        if(isImmediate){
                            handleImmediate(d,isInLayoutPass);
                        }
                        //网络返回的结果
                        else{
                            handleNetwork(d,isInLayoutPass);
                        }
                    }
                },width,height);
        // update the ImageContainer to be the new bitmap container.
        imageContainer = newContainer;
    }
    //回调绑定函数
    private void invokeCallback(boolean isSuccess,Drawable drawable){
        if(null == bindCallback){
            return;
        }
        if(!isBindCallbackInvoked){
            isBindCallbackInvoked = true;
            if(isSuccess){
                bindCallback.onSuccess(drawable,this);
            }else{
                bindCallback.onError(this);
            }
        }
    }
    /**
     * @see ImageView#onDetachedFromWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        clearImageContainer();
        // This has been detached from Window, so clear the drawable
        if (!isNormalImageView)
            internalSetImageDrawable(null);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        loadImageIfNecessary(false);
    }

    private void clearImageContainer() {
        if (imageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            imageContainer.cancelRequest();
            // also clear out the container so we can reload the image if necessary.
            imageContainer = null;
        }
    }

    private void internalSetImageDrawable(Drawable drawable){
        // Keep hold of previous Drawable
        final Drawable previousDrawable = getDrawable();
        // Call super to set new Drawable
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                setLayerType(drawable instanceof GifDrawable ?LAYER_TYPE_SOFTWARE:LAYER_TYPE_NONE,null);
            }
            super.setImageDrawable(drawable);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }

        if(drawable == previousDrawable){
            return;
        }
        // Notify new Drawable that it is being displayed
        notifyDrawable(drawable, true);

        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

    private void internalSetImageResource(int resId){
        final Drawable previousDrawable = getDrawable();
        try{
            super.setImageResource(resId);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

    /**
     * @see ImageView#setImageDrawable(Drawable)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setImageDrawable(Drawable drawable) {
        this.url = null;
        isNormalImageView = true;
        clearImageContainer();
        // Keep hold of previous Drawable
        final Drawable previousDrawable = getDrawable();
        // Call super to set new Drawable
        try{
            super.setImageDrawable(drawable);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        if(drawable == previousDrawable){
            return;
        }
        // Notify new Drawable that it is being displayed
        notifyDrawable(drawable, true);

        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

    @Override
    public void setImageResource(int resId) {
        isNormalImageView = true;
        clearImageContainer();
        final Drawable previousDrawable = getDrawable();
        try{
            super.setImageResource(resId);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

    @Override
    public void setImageURI(Uri uri) {
        isNormalImageView = true;
        clearImageContainer();
        final Drawable previousDrawable = getDrawable();
        // Notify old Drawable so it is no longer being displayed
        try{
            super.setImageURI(uri);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }
        notifyDrawable(previousDrawable,false);
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     *
     * @param drawable
     * @param isDisplayed
     */
    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            // The drawable is a CountingBitmapDrawable, so notify it
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            // The drawable is a LayerDrawable, so recurse on each layer
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    /**
     * 处理来自网络的响应结果
     * @param d
     * @param isInLayoutPass
     */
    private void handleNetwork(final Drawable d,boolean isInLayoutPass){
        //成功返回图片
        if (null != d) {
            if(!isInLayoutPass){
                if(successScaleType != null){
                    setScaleType(successScaleType);
                }
                internalSetImageDrawable(d);
                invokeCallback(true,d);
            }else{
                post(new Runnable() {
                    @Override
                    public void run() {
                        if(successScaleType != null){
                            setScaleType(successScaleType);
                        }
                        internalSetImageDrawable(d);
                        invokeCallback(true,d);
                    }
                });
            }
        } else if (errorImageId != 0) {
            if(!isInLayoutPass){
                if(errorScaleType != null){
                    setScaleType(errorScaleType);
                }
                internalSetImageResource(errorImageId);
                invokeCallback(false,null);
            }else{
                post(new Runnable() {
                    @Override
                    public void run() {
                        if(errorScaleType != null){
                            setScaleType(errorScaleType);
                        }
                        internalSetImageResource(errorImageId);
                        invokeCallback(false,null);
                    }
                });
            }
        }else{
            invokeCallback(false,null);
        }
    }

    /**
     * 处理来自缓存的响应结果
     * @param d
     * @param isInLayoutPass
     */
    private void handleImmediate(final Drawable d,boolean isInLayoutPass){
        //命中缓存
        if(null != d){
            // If this was an immediate response that was delivered inside of a layout
            // pass do not set the image immediately as it will trigger a requestLayout
            // inside of a layout. Instead, defer setting the image by posting back to
            // the main thread.
            if(!isInLayoutPass){
                if(successScaleType != null){
                    setScaleType(successScaleType);
                }
                internalSetImageDrawable(d);
                invokeCallback(true,d);
            }else{
                post(new Runnable() {
                    @Override
                    public void run() {
                        if(successScaleType != null){
                            setScaleType(successScaleType);
                        }
                        internalSetImageDrawable(d);
                        invokeCallback(true,d);
                    }
                });
            }
        }
        //没有命中缓存，需要显示默认图片
        else{
            if(!isInLayoutPass){
                if(defaultImageId != 0){
                    if(defaultScaleType != null){
                        setScaleType(defaultScaleType);
                    }
                    internalSetImageResource(defaultImageId);
                }
            }else{
                post(new Runnable() {
                    @Override
                    public void run() {
                        if(defaultScaleType != null){
                            setScaleType(defaultScaleType);
                        }
                        if(defaultImageId != 0){
                            internalSetImageResource(defaultImageId);
                        }
                    }
                });
            }
        }
    }
    /**
     * 绑定图片的回调函数，用于判断绑定是否成功
     */
    public interface BindCallback{
        void onSuccess(Drawable drawable, RecyclingImageView recyclingImageView);
        void onError(RecyclingImageView recyclingImageView);
    }
}