package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.cylee.androidlib.base.BaseApplication;

public class BitmapTransformerFactory {
    public static interface BitmapTransformer{
        Bitmap transform(Bitmap inBitmap);
        String getId();
    }
    public static class RoundBitmapTransformer implements BitmapTransformer{
        private static final int densityDpi = BaseApplication.getApplication().getResources().getDisplayMetrics().densityDpi;

        public RoundBitmapTransformer(int radius) {
            this.radius = radius;
        }
        int radius = 5;
        int shadowRadius = 0;
        int shadowDx = 0;
        int shadowDy = 0;
        int borderWidth;
        int borderColor;
        int shadowColor;

        public void setShadow(float shadowRadius, float shadowDx, float shadowDy,
                int shadowColor) {
            this.shadowRadius = (int) shadowRadius;
            this.shadowDx = (int) shadowDx;
            this.shadowDy = (int) shadowDy;
            this.shadowColor = shadowColor;
        }

        public void setBorder(float borderWidth, int color) {
            this.borderWidth = (int) borderWidth;
            this.borderColor = color;
        }
      
        public Bitmap createBoxBitmapAndCleanRaw(Bitmap bitmap) {

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int x = computeOffset(shadowDx);
            int y = computeOffset(shadowDy);

            int left = x + borderWidth;
            int top = y + borderWidth;

            int outerWidth = computeWidth(
                    bitmap.getScaledWidth(densityDpi),//DisplayMetrics.DENSITY_DEFAULT),
                    shadowRadius, shadowDx);// this.getIntrinsicWidth();
            int outerHeight = computeWidth(
                    bitmap.getScaledHeight(densityDpi),
                    shadowRadius, shadowDy);
            // 这里的Bitmap没有任何回收策略
            Bitmap output = Bitmap.createBitmap(outerWidth, outerHeight, Config.ARGB_8888);
            Canvas tmpCanvas = new Canvas(output);
            {
                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect(left, top, left + w, top + h);

                paint.setAntiAlias(true);
                tmpCanvas.drawARGB(0, 0, 0, 0);
                paint.setColor(color);
                tmpCanvas.drawRoundRect(new RectF(rect), radius, radius, paint);
                paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                tmpCanvas.drawBitmap(bitmap, left, top, paint);
            }

            Paint paint2 = new Paint();
            if (shadowRadius != 0 || shadowDx != 0 || shadowDy != 0) {
                paint2.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
            }

            if (borderWidth > 0) {
                final RectF rectF = new RectF(x, y, x + w + borderWidth * 2, y + h
                        + borderWidth * 2);
                paint2.setColor(borderColor);
                tmpCanvas.drawRoundRect(rectF, radius, radius, paint2);
            }
            bitmap.recycle();
            return output;
        }
        int computeWidth(int w, int r, int d) {
            return w + borderWidth * 2 + Math.max(0, r + d) + Math.max(0, r - d);
        }
        int computeOffset(int offset) {
            return Math.max(0, shadowRadius - offset);
        }
        @Override
        public Bitmap transform(Bitmap bitmap ) {
            return createBoxBitmapAndCleanRaw(bitmap);
        }

        @Override
        public String getId() {
            return "R"+radius;
        }
    }

    public static class TopRoundBitmapTransformer implements BitmapTransformer{
        private int radius;
        public TopRoundBitmapTransformer(int radius){
            this.radius = radius;
        }
        @Override
        public Bitmap transform(Bitmap inBitmap) {
            return getTopRoundCornerBitmap(inBitmap,radius);
        }

        @Override
        public String getId() {
            return null;
        }
        private static Bitmap getTopRoundCornerBitmap(Bitmap bitmap, int radius) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, w, h);
            final RectF rectF = new RectF(rect);
            final float roundPx = radius;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            final Rect rectBottom = new Rect(0, radius, w, h);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            paint.setColor(color);
            canvas.drawRect(rectBottom, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            bitmap.recycle();
            return output;
        }
    }
    public static class CircleBitmapTransformer implements BitmapTransformer{

        @Override
        public Bitmap transform(Bitmap inBitmap) {
            int size = (inBitmap.getWidth() < inBitmap.getHeight()) ? inBitmap.getWidth() : inBitmap
                    .getHeight();
            Bitmap outBitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
            Canvas c = new Canvas(outBitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            c.drawCircle(size/2f,size/2f,size/2f,paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            int left = (size == inBitmap.getWidth()) ? 0 : -(inBitmap.getWidth() - size) / 2 ;
            int top = (size == inBitmap.getHeight()) ? 0 : -(inBitmap.getHeight() - size) / 2;
            c.drawBitmap(inBitmap,left,top,paint);
            inBitmap.recycle();
            return outBitmap;
        }

        @Override
        public String getId() {
            return "CircleBitmapTransformer";
        }
    }
}
