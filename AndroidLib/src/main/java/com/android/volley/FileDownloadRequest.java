package com.android.volley;

import android.text.TextUtils;

import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RetryPolicyFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

public class FileDownloadRequest extends Request<Void> {

	public static class FileDownloadListener implements Response.Listener<File> {
		@Override
		public void onResponse(File response) {
		}

		public void onCancel() {
		}

		public void onProgress(long fileSize, long downloadedSize) {
		}

		public void onError(VolleyError e) {
		}
	}

	private File mStoreFile;
	private File mTemporaryFile;
	private FileDownloadListener mListener;

	public FileDownloadRequest(String storeFilePath, String url) {
		super(url, null);
		mStoreFile = new File(storeFilePath);
		mTemporaryFile = new File(storeFilePath + ".tmp");
		setShouldCache(false);
		// Turn the retries frequency greater.
		setRetryPolicy(RetryPolicyFactory.getRetryPolicy(RetryPolicyFactory.RETRY_POLICY.DOWNLOAD));
	}

	public void setDownloadListener(FileDownloadListener listener) {
		mListener = listener;
	}


	/** Init or reset the Range header, ensure the begin position always be the temporary file size. */
	@Override
	public void prepare() {
		// Note: if the request header "Range" greater than the actual length that server-size have,
		// the response header "Content-Range" will return "bytes */[actual length]", that's wrong.
		addHeader("Range", "bytes=" + mTemporaryFile.length() + "-");

//		Suppress the HttpStack accept gzip encoding, avoid the progress calculate wrong problem.
//		addHeader("Accept-Encoding", "identity");
	}

	/** Ignore the response content, just rename the TemporaryFile to StoreFile. */
	@Override
	protected Response<Void> parseNetworkResponse(NetworkResponse response) {
		if (!isCanceled()) {
			if (mTemporaryFile.canRead() && mTemporaryFile.length() > 0) {
				if (mTemporaryFile.renameTo(mStoreFile)) {
					return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
				} else {
					return Response.error(new VolleyError("Can't rename the download temporary file!"));
				}
			} else {
				return Response.error(new VolleyError("Download temporary file was invalid!"));
			}
		}
		return Response.error(new VolleyError("Request was Canceled!"));
	}

	@Override
	protected void deliverResponse(Void response) {
		if (mListener != null) {
			mListener.onResponse(mStoreFile);
		}
	}

	@Override
	public void deliverCancel() {
		super.deliverCancel();
		if (mListener != null) {
			mListener.onCancel();
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		// fix by cylee, if canceled delivery it right now
		RequestQueue queue = getRequestQueue();
		if (queue != null) {
			ResponseDelivery delivery = queue.getDelivery();
			if (delivery != null) {
				delivery.postCancel(this);
			}
		}
	}

	@Override
	public void deliverDownloadProgress(long fileSize, long downloadedSize) {
		super.deliverDownloadProgress(fileSize, downloadedSize);
		if (mListener != null) {
			mListener.onProgress(fileSize, downloadedSize);
		}
	}

	@Override
	public void deliverError(VolleyError error) {
		super.deliverError(error);
		if (mListener != null) {
			mListener.onError(error);
		}
	}

	/**
	 * In this method, we got the Content-Length, with the TemporaryFile length,
	 * we can calculate the actually size of the whole file, if TemporaryFile not exists,
	 * we'll take the store file length then compare to actually size, and if equals,
	 * we consider this download was already done.
	 * We used {@link RandomAccessFile} to continue download, when download success,
	 * the TemporaryFile will be rename to StoreFile.
	 */
	@Override
	public byte[] handleResponse(HttpResponse response, ResponseDelivery delivery) throws IOException, ServerError {
		// Content-Length might be negative when use HttpURLConnection because it default header Accept-Encoding is gzip,
		// we can force set the Accept-Encoding as identity in prepare() method to slove this problem but also disable gzip response.
		HttpEntity entity = response.getEntity();
		long fileSize = entity.getContentLength();
		if (fileSize <= 0) {
			VolleyLog.d("Response doesn't present Content-Length!");
		}

		long downloadedSize = mTemporaryFile.length();
		boolean isSupportRange = HttpUtils.isSupportRange(response);
		if (isSupportRange) {
			fileSize += downloadedSize;

			// Verify the Content-Range Header, to ensure temporary file is part of the whole file.
			// Sometime, temporary file length add response content-length might greater than actual file length,
			// in this situation, we consider the temporary file is invalid, then throw an exception.
			String realRangeValue = HttpUtils.getHeader(response, "Content-Range");
			// response Content-Range may be null when "Range=bytes=0-"
			if (!TextUtils.isEmpty(realRangeValue)) {
				String assumeRangeValue = "bytes " + downloadedSize + "-" + (fileSize - 1);
				if (TextUtils.indexOf(realRangeValue, assumeRangeValue) == -1) {
					throw new IllegalStateException(
							"The Content-Range Header is invalid Assume[" + assumeRangeValue + "] vs Real[" + realRangeValue + "], " +
									"please remove the temporary file [" + mTemporaryFile + "].");
				}
			}
		}

		// Compare the store file size(after download successes have) to server-side Content-Length.
		// temporary file will rename to store file after download success, so we compare the
		// Content-Length to ensure this request already download or not.
		if (fileSize > 0 && mStoreFile.length() == fileSize) {
			// Rename the store file to temporary file, mock the download success. ^_^
			mStoreFile.renameTo(mTemporaryFile);

			// Deliver download progress.
			delivery.postDownloadProgress(this, fileSize, fileSize);

			return null;
		}

		RandomAccessFile tmpFileRaf = new RandomAccessFile(mTemporaryFile, "rw");

		// If server-side support range download, we seek to last point of the temporary file.
		if (isSupportRange) {
			tmpFileRaf.seek(downloadedSize);
		} else {
			// If not, truncate the temporary file then start download from beginning.
			tmpFileRaf.setLength(0);
			downloadedSize = 0;
		}

		try {
			InputStream in = entity.getContent();
			byte[] buffer = new byte[6 * 1024]; // 6K buffer
			int offset;

			while ((offset = in.read(buffer)) != -1) {
				if (isCanceled()) {
					break;
				}
				tmpFileRaf.write(buffer, 0, offset);
				downloadedSize += offset;
				delivery.postDownloadProgress(this, fileSize, downloadedSize);
			}
		} finally {
			try {
				// Close the InputStream and release the resources by "consuming the content".
				if (entity != null) entity.consumeContent();
			} catch (Exception e) {
				// This can happen if there was an exception above that left the entity in
				// an invalid state.
				VolleyLog.v("Error occured when calling consumingContent");
			}
			tmpFileRaf.close();
		}

		return null;
	}

	@Override
	public Priority getPriority() {
		return Priority.LOW;
	}
}