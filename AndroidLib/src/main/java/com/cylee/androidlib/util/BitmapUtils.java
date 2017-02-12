/**
 *
 * 创建人：SongZhiyong
 * 创建时间：2012-12-30
 */
package com.cylee.androidlib.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 功能：Bitmap相关操作及特效
 *
 * @author SongZhiyong
 */
public class BitmapUtils {
  private static Bitmap destBmp = null;

  /**
   * 创建圆角图片
   *
   * @param bmp 源bitmap
   * @param radius 圆角半径
   * @return destBmp 已生成的圆角图片
   * @throws Exception 圆角直径大于最小边长
   */
  public static Bitmap createRoundCornerBmp(Bitmap bmp, int radius) throws Exception {
    if (2 * radius > Math.min(bmp.getWidth(), bmp.getHeight())) {
      throw new Exception("参数错误");
    }
    destBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
    Canvas canvas = new Canvas(destBmp);
    Paint paint = new Paint();
    Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
    RectF rectF = new RectF(rect);
    // 去锯齿
    paint.setAntiAlias(true);
    canvas.drawRoundRect(rectF, radius, radius, paint);
    // 设置相交模式
    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(bmp, null, rect, paint);
    return destBmp;
  }

  /**
   * getRoundedCornerBitmap: QQ代码中获取圆角bitmap
   *
   * @param @param paramBitmap
   * @param @param paramFloat
   * @param @param paramInt1
   * @param @param paramInt2
   * @return Bitmap
   * @throws
   * @since 上午11:47:07
   */
  public static Bitmap getRoundedCornerBitmap(Bitmap srcBitmap, float radius, int width,
      int height) {
    Bitmap result;
    try {
      Bitmap localBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      if (srcBitmap == null) {
        result = null;
      } else {
        Canvas canvas = new Canvas(localBitmap2);
        Paint localPaint = new Paint();
        Rect localRect1 = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        Rect localRect2 = new Rect(0, 0, width, height);
        RectF localRectF = new RectF(localRect2);
        localPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        localPaint.setColor(-12434878);
        canvas.drawRoundRect(localRectF, radius, radius, localPaint);
        localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Matrix localMatrix = new Matrix();
        localMatrix.setRectToRect(new RectF(localRect1), new RectF(localRect2),
            Matrix.ScaleToFit.FILL);
        canvas.drawBitmap(
            Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(),
                localMatrix, true), localRect2, localRect2, localPaint);
        result = localBitmap2;
      }
    } catch (Throwable localThrowable) {
      localThrowable.printStackTrace();
      result = null;
    }
    return result;
  }

  /**
   * 保存图片到指定的路径
   *
   * @param bmp 要保存的bitmap
   * @param path 保存到的路径
   * @param quality 保存图片质量
   * @param format 压缩格式（CompressFormat.JPEG,CompressFormat.PNG）
   * @throws IOException IO异常
   */
  public static void saveBitmap(CompressFormat format, Bitmap bmp, String path, int quality)
      throws IOException {
    File file = new File(path);
    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
    if (!file.exists()) {
      file.createNewFile();
    }
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(file);
      bmp.compress(format, quality, stream);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * 利用Matrix旋转图片
   *
   * @param bmp 需要旋转的图片
   * @param degree 旋转角度
   * @return destBmp 旋转后的图片
   */
  public static Bitmap rotate(Bitmap bmp, int degree) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degree);
    destBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    bmp.recycle();
    return destBmp;
  }

  /**
   * 根据传入的Bitmap对象构建带有倒影的Bitmap
   *
   * @param bmp 图片位图
   * @return Bitmap 返回带有倒影的位图Bitmap
   */
  public static Bitmap createWithReflectedBmp(Bitmap bmp) {
    if (bmp == null) {
      return bmp;
    }
    // 图片与倒影之间的距离间隔
    int reflectionGap = 4;
    int width = bmp.getWidth();
    int height = bmp.getHeight();
    // 变换所需的Matrix,完成 图片旋转，缩放等控制
    Matrix matrix = new Matrix();
    matrix.preScale(1, -1);
    // 获取倒影Bitmap
    Bitmap reflectionBitmap =
        Bitmap.createBitmap(bmp, 0, height / 2, width, height / 2, matrix, false);
    // 获取带倒影的Bitmap.即整体的效果图位图对象
    Bitmap withReflectionBitmap = Bitmap.createBitmap(width, height + height / 2, Config.ARGB_8888);
    /** Bitmap的显示还需要画布Canvas来完成 */
    // 由该位图对象创建初始画布(规定了画布的宽高)
    Canvas canvas = new Canvas(withReflectionBitmap);
    canvas.drawBitmap(bmp, 0, 0, null);
    // 绘制出原图与倒影之间的间隔，用矩形来描绘
    Paint paint1 = new Paint();
    canvas.drawRect(0, height, width, height + reflectionGap, paint1);
    // 绘制出倒影的Bitmap
    canvas.drawBitmap(reflectionBitmap, 0, height + reflectionGap, paint1);
    // 绘制线性渐变对象
    Paint paint2 = new Paint();
    LinearGradient shader =
        new LinearGradient(0, bmp.getHeight(), 0, withReflectionBitmap.getHeight() + reflectionGap,
            0x70ffffff, 0x00ffffff, TileMode.CLAMP);
    // 把渐变效果应用在画笔上
    paint2.setShader(shader);
    // 设置倒影的阴影度，使其与原来的图像颜色区别开来，此处显示灰度，会被染上下面的底部的原图片的倒影颜色，实现倒影的修饰
    paint2.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    // 用设置好的paint2绘制此倒影
    canvas.drawRect(0, height, width, withReflectionBitmap.getHeight() + reflectionGap, paint2);
    return withReflectionBitmap;
  }

  /**
   * 怀旧效果(相对之前做了优化快一倍)
   *
   * @param bmp 源bitmap
   * @return destBmp 怀旧效果的图片
   */
  public static Bitmap createOlderBmp(Bitmap bmp) {
    int width = bmp.getWidth();
    int height = bmp.getHeight();
    destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    int pixColor = 0;
    int pixR = 0;
    int pixG = 0;
    int pixB = 0;
    int newR = 0;
    int newG = 0;
    int newB = 0;
    int[] pixels = new int[width * height];
    bmp.getPixels(pixels, 0, width, 0, 0, width, height);
    for (int i = 0; i < height; i++) {
      for (int k = 0; k < width; k++) {
        pixColor = pixels[width * i + k];
        pixR = Color.red(pixColor);
        pixG = Color.green(pixColor);
        pixB = Color.blue(pixColor);
        newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
        newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
        newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
        int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG,
            newB > 255 ? 255 : newB);
        pixels[width * i + k] = newColor;
      }
    }
    destBmp.setPixels(pixels, 0, width, 0, 0, width, height);
    return destBmp;
  }

  /**
   * 图片锐化（拉普拉斯变换）
   *
   * @param bmp 源bitmap
   * @return 锐化后图片
   */
  public static Bitmap createSharpenBmp(Bitmap bmp) {
    // 拉普拉斯矩阵
    int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };
    int width = bmp.getWidth();
    int height = bmp.getHeight();
    destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    int pixR = 0;
    int pixG = 0;
    int pixB = 0;
    int pixColor = 0;
    int newR = 0;
    int newG = 0;
    int newB = 0;
    int idx = 0;
    float alpha = 0.3F;
    int[] pixels = new int[width * height];
    bmp.getPixels(pixels, 0, width, 0, 0, width, height);
    for (int i = 1, length = height - 1; i < length; i++) {
      for (int k = 1, len = width - 1; k < len; k++) {
        idx = 0;
        for (int m = -1; m <= 1; m++) {
          for (int n = -1; n <= 1; n++) {
            pixColor = pixels[(i + n) * width + k + m];
            pixR = Color.red(pixColor);
            pixG = Color.green(pixColor);
            pixB = Color.blue(pixColor);
            newR = newR + (int) (pixR * laplacian[idx] * alpha);
            newG = newG + (int) (pixG * laplacian[idx] * alpha);
            newB = newB + (int) (pixB * laplacian[idx] * alpha);
            idx++;
          }
        }
        newR = Math.min(255, Math.max(0, newR));
        newG = Math.min(255, Math.max(0, newG));
        newB = Math.min(255, Math.max(0, newB));
        pixels[i * width + k] = Color.argb(255, newR, newG, newB);
        newR = 0;
        newG = 0;
        newB = 0;
      }
    }
    destBmp.setPixels(pixels, 0, width, 0, 0, width, height);
    return destBmp;
  }

  /**
   * 柔化效果(高斯模糊)(优化后比上面快三倍)
   *
   * @param bmp 源bitmap
   * @return destBmp 模糊后的图片
   */
  public static Bitmap createBlurBmp(Bitmap bmp) {
    // 高斯矩阵
    int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
    int width = bmp.getWidth();
    int height = bmp.getHeight();
    destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    int pixR = 0;
    int pixG = 0;
    int pixB = 0;
    int pixColor = 0;
    int newR = 0;
    int newG = 0;
    int newB = 0;
    int delta = 16; // 值越小图片会越亮，越大则越暗
    int idx = 0;
    int[] pixels = new int[width * height];
    bmp.getPixels(pixels, 0, width, 0, 0, width, height);
    for (int i = 1, length = height - 1; i < length; i++) {
      for (int k = 1, len = width - 1; k < len; k++) {
        idx = 0;
        for (int m = -1; m <= 1; m++) {
          for (int n = -1; n <= 1; n++) {
            pixColor = pixels[(i + m) * width + k + n];
            pixR = Color.red(pixColor);
            pixG = Color.green(pixColor);
            pixB = Color.blue(pixColor);
            newR = newR + (int) (pixR * gauss[idx]);
            newG = newG + (int) (pixG * gauss[idx]);
            newB = newB + (int) (pixB * gauss[idx]);
            idx++;
          }
        }
        newR /= delta;
        newG /= delta;
        newB /= delta;
        newR = Math.min(255, Math.max(0, newR));
        newG = Math.min(255, Math.max(0, newG));
        newB = Math.min(255, Math.max(0, newB));
        pixels[i * width + k] = Color.argb(255, newR, newG, newB);
        newR = 0;
        newG = 0;
        newB = 0;
      }
    }
    destBmp.setPixels(pixels, 0, width, 0, 0, width, height);
    return destBmp;
  }

  /**
   * 底片效果
   *
   * @param bmp 源bitmap
   * @return 底片效果图片
   */
  public static Bitmap createFilmBmp(Bitmap bmp) {
    // RGBA的最大值
    final int MAX_VALUE = 255;
    int width = bmp.getWidth();
    int height = bmp.getHeight();
    destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    int pixR = 0;
    int pixG = 0;
    int pixB = 0;
    int pixColor = 0;
    int newR = 0;
    int newG = 0;
    int newB = 0;
    int[] pixels = new int[width * height];
    bmp.getPixels(pixels, 0, width, 0, 0, width, height);
    int pos = 0;
    for (int i = 1, length = height - 1; i < length; i++) {
      for (int k = 1, len = width - 1; k < len; k++) {
        pos = i * width + k;
        pixColor = pixels[pos];
        pixR = Color.red(pixColor);
        pixG = Color.green(pixColor);
        pixB = Color.blue(pixColor);
        newR = MAX_VALUE - pixR;
        newG = MAX_VALUE - pixG;
        newB = MAX_VALUE - pixB;
        newR = Math.min(MAX_VALUE, Math.max(0, newR));
        newG = Math.min(MAX_VALUE, Math.max(0, newG));
        newB = Math.min(MAX_VALUE, Math.max(0, newB));
        pixels[pos] = Color.argb(MAX_VALUE, newR, newG, newB);
      }
    }
    destBmp.setPixels(pixels, 0, width, 0, 0, width, height);
    return destBmp;
  }

  /**
   * Bitmap转换到Byte[]
   *
   * @param bitmap 源bitmap
   * @return byte 字节数组
   */
  public static byte[] bitmap2Bytes(Bitmap bitmap) {
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bas);
    return bas.toByteArray();
  }

  /**
   * 截取Bitmap为CenterCrop样式
   *
   * @param @param source
   * @param @param newHeight
   * @param @param newWidth
   * @param @return 设定文件
   * @return Bitmap DOM对象
   * @throws
   * @since CodingExample　Ver 1.1
   */
  public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
    int sourceWidth = source.getWidth();
    int sourceHeight = source.getHeight();

    // Compute the scaling factors to fit the new height and width,
    // respectively.
    // To cover the final image, the final scaling will be the bigger
    // of these two.
    float xScale = (float) newWidth / sourceWidth;
    float yScale = (float) newHeight / sourceHeight;
    float scale = Math.max(xScale, yScale);

    // Now get the size of the source bitmap when scaled
    float scaledWidth = scale * sourceWidth;
    float scaledHeight = scale * sourceHeight;

    // Let's find out the upper left coordinates if the scaled bitmap
    // should be centered in the new size give by the parameters
    float left = (newWidth - scaledWidth) / 2;
    float top = (newHeight - scaledHeight) / 2;

    // The target rectangle for the new, scaled version of the source bitmap
    // will now
    // be
    RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

    // Finally, we create a new bitmap of the specified size and draw our
    // new,
    // scaled bitmap onto it.
    Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
    Canvas canvas = new Canvas(dest);
    canvas.drawBitmap(source, null, targetRect, null);

    return dest;
  }

  private static int getScaleOption(int w, int dw) {
    int result = 1;
    while (w / dw >= 2) {
      w = (int) (w / 2f);
      result *= 2;
    }
    return result;
  }

  public static Bitmap decodeFromPath(String path, BitmapFactory.Options options) {
    try {
      return BitmapFactory.decodeFile(path, options);
    } catch (OutOfMemoryError e) {
    } catch (Exception e) {
    }
    return null;
  }

  public static Bitmap decodeFromRes(Context context, int id, BitmapFactory.Options options) {
    try {
      return BitmapFactory.decodeResource(context.getResources(), id, options);
    } catch (OutOfMemoryError e) {
    } catch (Exception e) {
    }
    return null;
  }

  public static Bitmap decodeFromPath(String path) {
    try {
      return BitmapFactory.decodeFile(path);
    } catch (OutOfMemoryError e) {
    } catch (Exception e) {
    }
    return null;
  }

  public static Bitmap decodeFromRes(Context context, int id) {
    try {
      return BitmapFactory.decodeResource(context.getResources(), id);
    } catch (OutOfMemoryError e) {
    } catch (Exception e) {
    }
    return null;
  }

  public static Bitmap cropBitmap(String path, int desWidth, int desHeight, float scaleRatio,
      int maxRetryCount) {
    return cropBitmap(new PathBitmapDecode(path), desWidth, desHeight, scaleRatio, maxRetryCount);
  }

  public static Bitmap cropBitmap(Context context, int resId, int desWidth, int desHeight,
      float scaleRatio, int maxRetryCount) {
    return cropBitmap(new ResBitmapDecode(context, resId), desWidth, desHeight, scaleRatio,
        maxRetryCount);
  }

  public static Bitmap cropBitmap(Context context, Uri uri, int desWidth, int desHeight,
      float scaleRatio, int maxRetryCount) {
    return cropBitmap(new UriBitmapDecode(context, uri), desWidth, desHeight, scaleRatio,
        maxRetryCount);
  }

  public static Bitmap cropBitmap(IBitmapDecode decode, int desWidth, int desHeight,
      float scaleRatio, int maxRetryCount) {
    Bitmap result = null;
    int retryCount = 0;
    while (result == null && retryCount < maxRetryCount) {
      result = cropBitmap(decode, desWidth, desHeight, true);
      if (result == null) {
        desWidth = (int) (desWidth * scaleRatio);
        desHeight = (int) (desHeight * scaleRatio);
        retryCount++;
      }
    }
    return result;
  }

  public static Bitmap cropBitmap(String path, int desWidth, int desHeight) {
    return cropBitmap(new PathBitmapDecode(path), desWidth, desHeight, true);
  }

  public static Bitmap cropBitmap(Context context, int id, int desWidth, int desHeight) {
    return cropBitmap(new ResBitmapDecode(context, id), desWidth, desHeight, true);
  }

  public static Bitmap cropBitmap(Context context, Uri uri, int desWidth, int desHeight) {
    return cropBitmap(new UriBitmapDecode(context, uri), desWidth, desHeight, true);
  }

  public static Bitmap cropBitmap(IBitmapDecode decode, int desWidth, int desHeight,
      boolean autoRotation) {
    int rotation = autoRotation ? decode.getRotation() : 0;
    if (rotation == 90 || rotation == 270) {
      desWidth = desWidth + desHeight;
      desHeight = desWidth - desHeight;
      desWidth = desWidth - desHeight;
    }
    Bitmap result = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    options.inScaled = false;
    decode.decode(options);
    int bw = options.outWidth;
    int bh = options.outHeight;
    float bmpRatio = bw / (float) bh;
    float destRatio = desWidth / (float) desHeight;
    options.inJustDecodeBounds = false;
    if (bmpRatio >= destRatio) {
      if (bh >= desHeight) {
        int scale = getScaleOption(bh, desHeight);
        options.inSampleSize = scale;
        options.inScaled = false;
        Bitmap scaleBitmap = null;
        Bitmap originBitmap = null;
        try {
          originBitmap = decode.decode(options);
          if (originBitmap == null) {
            return null;
          }
          int ow = originBitmap.getWidth();
          int oh = originBitmap.getHeight();
          if (ow == desWidth && oh == desHeight) {
            result = originBitmap;
          } else {
            float s = oh / (float) desHeight;
            scaleBitmap = Bitmap.createScaledBitmap(originBitmap, (int) (ow / s), desHeight, false);
            int startX = Math.max((int) ((scaleBitmap.getWidth() - desWidth) / 2f), 0);
            if (scaleBitmap.getWidth() != desWidth || scaleBitmap.getHeight() != desHeight) {
              result = Bitmap.createBitmap(scaleBitmap, startX, 0,
                  Math.min(desWidth, scaleBitmap.getWidth()),
                  Math.min(desHeight, scaleBitmap.getHeight()));
            } else {
              result = scaleBitmap;
            }
          }
        } catch (OutOfMemoryError e) {
          recycleBitmap(result);
          result = null;
        } finally {
          if (originBitmap != result) {
            recycleBitmap(originBitmap);
          }
          if (scaleBitmap != result) {
            recycleBitmap(scaleBitmap);
          }
        }
      } else {
        options.inSampleSize = 1;
        options.inScaled = false;
        Bitmap originBitmap = null;
        Bitmap tmp = null;
        try {
          originBitmap = decode.decode(options);
          if (originBitmap == null) {
            return null;
          }
          int dh = originBitmap.getHeight();
          int dw = (int) (dh * destRatio);
          int startX = (int) ((originBitmap.getWidth() - dw) / 2f);
          tmp = Bitmap.createBitmap(originBitmap, startX, 0, dw, dh);
          result = Bitmap.createScaledBitmap(tmp, desWidth, desHeight, false);
        } catch (OutOfMemoryError e) {
          recycleBitmap(result);
          result = null;
        } finally {
          if (originBitmap != result) {
            recycleBitmap(originBitmap);
          }
          if (tmp != result) {
            recycleBitmap(tmp);
          }
        }
      }
    } else {
      if (bw >= desWidth) {
        int scale = getScaleOption(bw, desWidth);
        options.inSampleSize = scale;
        options.inScaled = false;
        Bitmap scaleBitmap = null;
        Bitmap originBitmap = null;
        try {
          originBitmap = decode.decode(options);
          if (originBitmap == null) {
            return null;
          }
          int ow = originBitmap.getWidth();
          int oh = originBitmap.getHeight();
          if (ow == desWidth && oh == desHeight) {
            result = originBitmap;
          } else {
            float s = ow / (float) desWidth;
            scaleBitmap = Bitmap.createScaledBitmap(originBitmap, desWidth, (int) (oh / s), false);
            int startY = Math.max((int) ((scaleBitmap.getHeight() - desHeight) / 2f), 0);
            if (scaleBitmap.getWidth() != desWidth || scaleBitmap.getHeight() != desHeight) {
              result = Bitmap.createBitmap(scaleBitmap, 0, startY,
                  Math.min(desWidth, scaleBitmap.getWidth()),
                  Math.min(desHeight, scaleBitmap.getHeight()));
            } else {
              result = scaleBitmap;
            }
          }
        } catch (OutOfMemoryError e) {
          recycleBitmap(result);
          result = null;
        } finally {
          if (originBitmap != result) {
            recycleBitmap(originBitmap);
          }
          if (scaleBitmap != result) {
            recycleBitmap(scaleBitmap);
          }
        }
      } else {
        options.inSampleSize = 1;
        options.inScaled = false;
        Bitmap originBitmap = null;
        Bitmap tmp = null;
        try {
          originBitmap = decode.decode(options);
          if (originBitmap == null) {
            return null;
          }
          int dw = originBitmap.getWidth();
          int dh = (int) (dw / destRatio);
          int startY = (int) ((originBitmap.getHeight() - dh) / 2f);
          tmp = Bitmap.createBitmap(originBitmap, 0, startY, dw, dh);
          result = Bitmap.createScaledBitmap(tmp, desWidth, desHeight, false);
        } catch (OutOfMemoryError e) {
          recycleBitmap(result);
          result = null;
        } finally {
          if (originBitmap != result) {
            recycleBitmap(originBitmap);
          }
          if (tmp != result) {
            recycleBitmap(tmp);
          }
        }
      }
    }
    if (rotation != 0) {
      Matrix matrix = new Matrix();
      matrix.postRotate(rotation);
      Bitmap tmp = null;
      try {
        tmp = result;
        result =
            Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
      } catch (OutOfMemoryError e) {
        recycleBitmap(result);
        result = null;
      } catch (Exception e) {
        recycleBitmap(result);
        result = null;
      } finally {
        if (tmp != result) {
          recycleBitmap(tmp);
        }
      }
    }
    return result;
  }

  public static void recycleBitmap(Bitmap bitmap) {
    if (bitmap != null && !bitmap.isRecycled()) {
      bitmap.recycle();
    }
  }

  public static void recycleBitmapDrawable(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bd = (BitmapDrawable) drawable;
      Bitmap bmp = bd.getBitmap();
      recycleBitmap(bmp);
      drawable.setCallback(null);
    }
  }

  public static int getCameraPhotoOrientation(String imagePath) {
    int rotate = 0;
    try {
      File imageFile = new File(imagePath);
      ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
      int orientation =
          exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_270:
          rotate = 270;
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          rotate = 180;
          break;
        case ExifInterface.ORIENTATION_ROTATE_90:
          rotate = 90;
          break;
      }
    } catch (Exception e) {
    }
    return rotate;
  }

  public static Bitmap drawableToBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }
    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
        Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }

  public static int determineOrientation(ContentResolver resolver, Uri imageUri) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    try {
      InputStream in = resolver.openInputStream(imageUri);
      BitmapFactory.decodeStream(in, null, options);

      /* Choose orientation based on image ratio. */
      float ratio = ((float) options.outWidth) / ((float) options.outHeight);
      return ratio > 1 ? 90 : 0;

    } catch (IOException e) {
      Log.e("Unable to determine necessary screen orientation");
      return 0;
    }
  }

  public static class ResBitmapDecode implements IBitmapDecode {
    private int mId;
    private Context mContext;

    public ResBitmapDecode(Context context, int id) {
      mContext = context;
      mId = id;
    }

    @Override public Bitmap decode(BitmapFactory.Options options) {
      return decodeFromRes(mContext, mId, options);
    }

    @Override public Bitmap decode() {
      return decodeFromRes(mContext, mId);
    }

    @Override public int getRotation() {
      return 0;
    }
  }

  public static class PathBitmapDecode implements IBitmapDecode {
    private String mPath;

    public PathBitmapDecode(String path) {
      mPath = path;
    }

    @Override public Bitmap decode(BitmapFactory.Options options) {
      return decodeFromPath(mPath, options);
    }

    @Override public Bitmap decode() {
      return decodeFromPath(mPath);
    }

    @Override public int getRotation() {
      return getCameraPhotoOrientation(mPath);
    }
  }

  public static class UriBitmapDecode implements IBitmapDecode {
    private Uri mUri;
    private Context mContext;
    public UriBitmapDecode(Context context, Uri uri) {
      mContext = context;
      mUri = uri;
    }

    @Override public Bitmap decode(BitmapFactory.Options options) {
      try {
        InputStream inputBounds = mContext.getContentResolver().openInputStream(mUri);
        return BitmapFactory.decodeStream(inputBounds, null, options);
      } catch (Exception e){
      }
      return null;
    }

    @Override public Bitmap decode() {
      try {
        InputStream inputBounds = mContext.getContentResolver().openInputStream(mUri);
        return BitmapFactory.decodeStream(inputBounds);
      } catch (Exception e){
      }
      return null;
    }

    @Override public int getRotation() {
      return determineOrientation(mContext.getContentResolver(), mUri);
    }
  }

  public static interface IBitmapDecode {
    Bitmap decode(BitmapFactory.Options options);

    Bitmap decode();

    int getRotation();
  }
}