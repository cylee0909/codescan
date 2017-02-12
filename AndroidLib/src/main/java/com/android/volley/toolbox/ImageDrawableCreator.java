package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.android.volley.NetworkResponse;
import com.cylee.androidlib.base.BaseApplication;
import com.cylee.androidlib.net.GifDrawable;
import com.cylee.androidlib.net.RecyclingBitmapDrawable;

import java.io.IOException;

/**
 * Created by sn on 13-11-4.
 * 一个能够处理JPG,PNG,GIF图片格式的DrawableCreator
 */
public class ImageDrawableCreator implements DrawableRequest.DrawableCreator{
    private static String TAG = "ImageDrawableCreator";

    /**
     * @param maxWidth Maximum width to decode this bitmap to, or zero for none
     * @param maxHeight Maximum height to decode this bitmap to, or zero for none
     * @param decodeConfig Format to decode the bitmap to
     * @param transformer A transformer to process the output bitmap
     */
    public ImageDrawableCreator(int maxWidth, int maxHeight, Bitmap.Config decodeConfig, BitmapTransformerFactory.BitmapTransformer transformer){
        this.mDecodeConfig = decodeConfig;
        this.mMaxHeight = maxHeight;
        this.mMaxWidth = maxWidth;
        this.mTransformer = transformer;
    }
    private final Bitmap.Config mDecodeConfig;
    private final int mMaxWidth;
    private final int mMaxHeight;
    private BitmapTransformerFactory.BitmapTransformer mTransformer;
    @Override
    public Drawable doParse(NetworkResponse response) {
        String contentType = response.headers.get("Content-Type");
        //当有transformer的时候，把gif当做bitmap直接处理
        if(contentType != null && contentType.toLowerCase().equals("image/gif") && mTransformer == null){
            return parseGif(response.data);
        }
        else{
            return parseBitmap(response.data);
        }
    }
    @Override
    public String getId() {
        if(null != mTransformer){
            return "ImageDrawableCreator"+mTransformer.getId();
        }
        return "ImageDrawableCreator";
    }
    /**
     * 处理jpg,png等静态图片
     * @param data
     * @return
     */
    private BitmapDrawable parseBitmap(byte[] data){
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap;
        decodeOptions.inPreferredConfig = mDecodeConfig;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
            // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
            decodeOptions.inSampleSize =
                    findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap =
                    BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (desiredWidth>0&&desiredHeight>0&&tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                    tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap,
                        desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        return bitmap2Drawable(bitmap);
    }
    private BitmapDrawable bitmap2Drawable(Bitmap bitmap){
        if (bitmap == null) {
            return null;
        } else {
            //apply transformer if exsists
            if(mTransformer != null){
                bitmap = mTransformer.transform(bitmap);
            }
            //只有3.0以下创建自动回收的BitmapDrawable
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                return new BitmapDrawable(BaseApplication.getApplication().getResources(),bitmap);
            }else{
                return new RecyclingBitmapDrawable(BaseApplication.getApplication().getResources(),bitmap);
            }
        }
    }
    /**
     * 处理GIF动态图片
     * @param data
     * @return
     */
    private static GifDrawable parseGif(byte[] data){
        try {
            return new GifDrawable(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }
    /**
     * Scales one side of a rectangle to fit aspect ratio.
     *
     * @param maxPrimary Maximum size of the primary dimension (i.e. width for
     *        max width), or zero to maintain aspect ratio with secondary
     *        dimension
     * @param maxSecondary Maximum size of the secondary dimension, or zero to
     *        maintain aspect ratio with primary dimension
     * @param actualPrimary Actual size of the primary dimension
     * @param actualSecondary Actual size of the secondary dimension
     */
    static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                   int actualSecondary) {
        // If no dominant value at all, just return the actual.
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling ratio.
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }
}
