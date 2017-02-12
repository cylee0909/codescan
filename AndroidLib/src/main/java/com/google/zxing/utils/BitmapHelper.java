package com.google.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapHelper {
	// -------------------------------------------------------------------
	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {  
	  
	        Bitmap BitmapOrg = bitmap;  
	        int width = BitmapOrg.getWidth();  
	        int height = BitmapOrg.getHeight();  
	        int newWidth = w;  
	        int newHeight = h;  
	  
	         
	        float scaleWidth = ((float) newWidth) / width;  
	        float scaleHeight = ((float) newHeight) / height;  
	  
	        Matrix matrix = new Matrix();  
	        matrix.postScale(scaleWidth, scaleHeight);  
	  
	        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,  
	                height, matrix, true);  
	  
	        return resizedBitmap;  
	  
	    }  
}
