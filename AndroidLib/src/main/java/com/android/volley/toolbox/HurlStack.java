/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.content.res.AssetManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.cylee.androidlib.base.BaseApplication;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * An {@link com.android.volley.toolbox.HttpStack} based on {@link java.net.HttpURLConnection}.
 */
public class HurlStack implements HttpStack {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private Proxy mProxy;

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        public String rewriteUrl(String originalUrl);
    }

    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public HurlStack(){
        this(null);
    }
    public HurlStack(Proxy proxy) {
        this(null,proxy);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     * @param proxy proxy used for connection
     */
    public HurlStack(UrlRewriter urlRewriter,Proxy proxy) {
        this(urlRewriter, null, proxy);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     * @param proxy proxy used for connection
     */
    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory,Proxy proxy) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
        mProxy = proxy;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }
        URL parsedUrl = new URL(url);
        HttpResponse response;
        //add by zhangsn 增加对本地文件以及asset文件的支持
        if(url.startsWith("file:")){
            StatusLine responseStatus = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1),200,"OK");
            response = new BasicHttpResponse(responseStatus);
            URLConnection connection = parsedUrl.openConnection();
            InputStream is;
            long length;
            if(url.startsWith("file:///android_asset/")){
                AssetManager am =  BaseApplication.getApplication().getAssets();
                String name = url.replaceFirst("file:///android_asset/", "");
                is = am.open(name);
                length = am.openFd(name).getLength();
            }else{
                is = connection.getInputStream();
                try {
                    length = new File(parsedUrl.toURI()).length();
                } catch (URISyntaxException e) {
                    length = 0;
                }
            }
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(is);
            entity.setContentLength(length);
            response.setEntity(entity);
        } else{
            HttpURLConnection connection = openConnection(parsedUrl, request);
            for (String headerName : map.keySet()) {
                connection.addRequestProperty(headerName, map.get(headerName));
            }
            setConnectionParametersForRequest(connection, request);
            // Initialize HttpResponse with data from the HttpURLConnection.
            ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
            int responseCode = connection.getResponseCode();
            if (responseCode == -1) {
                // -1 is returned by getResponseCode() if the response code could not be retrieved.
                // Signal to the caller that something was wrong with the connection.
                throw new IOException("Could not retrieve response code from HttpUrlConnection.");
            }
            StatusLine responseStatus = new BasicStatusLine(protocolVersion,
                    connection.getResponseCode(), connection.getResponseMessage());
            response = new BasicHttpResponse(responseStatus);
            response.setEntity(entityFromConnection(connection));
            for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                    response.addHeader(h);
                }
            }
        }
        return response;
    }

    /**
     * Initializes an {@link org.apache.http.HttpEntity} from the given {@link java.net.HttpURLConnection}.
     * @param connection
     * @return an HttpEntity populated with data from <code>connection</code>.
     */
    private static HttpEntity entityFromConnection(HttpURLConnection connection) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    /**
     * Create an {@link java.net.HttpURLConnection} for the specified {@code url}.
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) (mProxy == null?url.openConnection():url.openConnection(mProxy));
    }

    /**
     * Opens an {@link java.net.HttpURLConnection} with parameters.
     * @param url
     * @return an openBook connection
     * @throws java.io.IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection)connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

    @SuppressWarnings("deprecation")
    /* package */ static void setConnectionParametersForRequest(HttpURLConnection connection,
            Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST:
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    // Prepare output. There is no need to set Content-Length explicitly,
                    // since this is handled by HttpURLConnection using the size of the prepared
                    // output stream.
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty(HEADER_CONTENT_TYPE,
                            request.getPostBodyContentType());
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(postBody);
                    out.close();
                }
                break;
            case Method.GET:
                // Not necessary to set the request method because connection defaults to GET but
                // being explicit here.
                connection.setRequestMethod("GET");
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HEADER_CONTENT_TYPE, request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }
    /**
     * Dynamically change proxy
     * @param proxy
     */
    @Override
    public void setProxy(Proxy proxy){
        this.mProxy = proxy;
    }
}
