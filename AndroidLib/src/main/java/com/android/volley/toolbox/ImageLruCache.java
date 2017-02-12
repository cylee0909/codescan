package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import com.cylee.androidlib.net.GifDrawable;
import com.cylee.androidlib.net.RecyclingBitmapDrawable;

public class ImageLruCache extends LruCache<String, Drawable> implements DrawableLoader.ImageCache {
    public static int getDefaultLruCacheSize() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/10th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 20;

        return cacheSize;
    }
    public ImageLruCache() {
        this(getDefaultLruCacheSize());
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue) {
        //RecyclingBitmapDrawable类型的Drawable需要回收内存
        if(RecyclingBitmapDrawable.class.isInstance(oldValue)){
            // The removed entry is a recycling drawable, so notify it
            // that it has been removed from the memory cache
            ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
        }
        super.entryRemoved(evicted, key, oldValue, newValue);
    }

    public ImageLruCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }
    @Override
    protected int sizeOf(String key, Drawable value) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        if(value instanceof BitmapDrawable){
            Bitmap bitmap = ((BitmapDrawable)value).getBitmap();
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }else if(value instanceof GifDrawable){
            return ((GifDrawable)value).getSize();
        }else{
            return 0;
        }
    }

    @Override
    public Drawable getDrawable(String url) {
        Drawable o = get(url);
        if(o == null || (o instanceof BitmapDrawable && (
            ((BitmapDrawable)o).getBitmap() == null ||
            ((BitmapDrawable)o).getBitmap().isRecycled()
        ))){
            return null;
        }
        return o;
    }

    @Override
    public void putDrawable(String url, Drawable drawable) {
        if(drawable != null){
            if(RecyclingBitmapDrawable.class.isInstance(drawable)){
                ((RecyclingBitmapDrawable)drawable).setIsCached(true);
            }
            put(url,drawable);
        }
    }
}
