package com.google.zxing.utils;

import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtils {
	public static Bitmap createImage(String text, int width, int height,
			Bitmap logo) {
		try {
			if (text == null || "".equals(text) || text.length() < 1) {
				return null;
			}
			
			Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(text,
					BarcodeFormat.QR_CODE, width, height, hints);
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff404040;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}

				}
			}

			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);

			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

			return setQRCodeLogo(bitmap, logo);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}

	}

	private static Bitmap setQRCodeLogo(Bitmap qr_img, Bitmap logo) {

		int logoWidth = qr_img.getWidth() / 5; 

		int logoHeight = qr_img.getHeight() / 5; 
		logo=BitmapHelper.resizeImage(logo, logoWidth, logoHeight);
		int logoX = (qr_img.getWidth() - logoWidth) / 2; 

		int logoY = (qr_img.getHeight() - logoHeight) / 2; 
		int right = logoX + logoWidth;  
	    int bottom = logoY + logoHeight; 
	    
	    Rect mRect = new Rect(logoX, logoY, right, bottom);
	    
	    Canvas canvas = new Canvas(qr_img);
	    
	    Rect mRectLogo = new Rect(0, 0, logoWidth, logoHeight);
	    canvas.drawBitmap(logo, mRectLogo, mRect, null);
		
		return qr_img;
	}
}
