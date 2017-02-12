package com.cylee.androidlib.net;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import java.io.IOException;

/**
 * A {@link Drawable} which can be used to hold GIF images, especially animations.
 * Basic GIF metadata can be also obtained.
 * @author koral--
 */
public class GifDrawable extends Drawable implements Animatable
{
    private final Paint paint = new Paint( Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG );
    private volatile boolean mIsRunning = true;
    private Movie movie;
    private int duration;
    private int size;
    private long begin = SystemClock.uptimeMillis();
    /**
     * Creates drawable from byte array.<br>
     * It can be larger than size of the GIF data. Bytes beyond GIF terminator are not accessed.
     * @param bytes raw GIF bytes
     * @throws IOException if bytes does not contain valid GIF data
     * @throws NullPointerException if bytes are null
     */
    public GifDrawable(byte[] bytes ) throws IOException
    {
        this.movie = Movie.decodeByteArray(bytes,0,bytes.length);
        duration = this.movie.duration();
        this.size = bytes.length/1024;
    }

    public int getSize() {
        return size;
    }

    /**
     * Reads and renders new frame if needed then draws last rendered frame.
     * @param canvas canvas to draw into
     */
    @Override
    public void draw ( Canvas canvas )
    {
        if ( mIsRunning )
        {
            int ms;
            if(duration != 0){
                ms = (int)(SystemClock.uptimeMillis()-begin)%duration;
            }else{
                ms = 0;
            }
            movie.setTime(ms);
            movie.draw(canvas, 0, 0);
            invalidateSelf();
        }
    }
    @Override
    public int getIntrinsicHeight ()
    {
        return this.movie.height();
    }

    @Override
    public int getIntrinsicWidth ()
    {
        return this.movie.width();
    }

    @Override
    public void setAlpha ( int alpha )
    {
        paint.setAlpha( alpha );
    }

    @Override
    public void setColorFilter ( ColorFilter cf )
    {
        paint.setColorFilter( cf );
    }

    /**
     * See {@link Drawable#getOpacity()}
     * @return always {@link PixelFormat#TRANSPARENT}
     */
    @Override
    public int getOpacity ()
    {
        return PixelFormat.TRANSPARENT;
    }

    /**
     * Starts the animation. Does nothing if GIF is not animated.
     * Can be called from any thread.
     */
    @Override
    public void start ()
    {
        mIsRunning = true;
    }
    /**
     * Stops the animation. Does nothing if GIF is not animated.
     * Can be called from any thread.
     */
    @Override
    public void stop ()
    {
        mIsRunning = false;
    }

    @Override
    public boolean isRunning ()
    {
        return mIsRunning;
    }

}