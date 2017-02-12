package com.android.volley;

import android.text.TextUtils;

import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.PoolingByteArrayOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

public class HttpUtils {

	/** Reads the contents of HttpEntity into a byte[]. */
	public static byte[] responseToBytes(HttpResponse response) throws IOException, ServerError {
		HttpEntity entity = response.getEntity();
		PoolingByteArrayOutputStream bytes =
				new PoolingByteArrayOutputStream(ByteArrayPool.get(), (int) entity.getContentLength());
		byte[] buffer = null;
		try {
			InputStream in = entity.getContent();
			if (in == null) {
				throw new ServerError("response content is null");
			}

			buffer = ByteArrayPool.get().getBuf(1024);
			int count;
			while ((count = in.read(buffer)) != -1) {
				bytes.write(buffer, 0, count);
			}
			return bytes.toByteArray();
		} finally {
			try {
				// Close the InputStream and release the resources by "consuming the content".
				entity.consumeContent();
			} catch (IOException e) {
				// This can happen if there was an exception above that left the entity in
				// an invalid state.
				VolleyLog.v("Error occured when calling consumingContent");
			}
			ByteArrayPool.get().returnBuf(buffer);
			bytes.close();
		}
	}

	public static String getHeader(HttpResponse response, String key) {
		Header header = response.getFirstHeader(key);
		return header == null ? null : header.getValue();
	}

	public static boolean isSupportRange(HttpResponse response) {
		if (TextUtils.equals(getHeader(response, "Accept-Ranges"), "bytes")) {
			return true;
		}
		String value = getHeader(response, "Content-Range");
		return value != null && value.startsWith("bytes");
	}
}
