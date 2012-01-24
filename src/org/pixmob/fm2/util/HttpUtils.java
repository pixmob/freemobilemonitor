/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
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
package org.pixmob.fm2.util;

import static org.pixmob.fm2.Constants.APPLICATION_NAME_USER_AGENT;
import static org.pixmob.fm2.Constants.TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

/**
 * Http utilities.
 * @author Pixmob
 */
public final class HttpUtils {
    private static String applicationVersion;
    
    static {
        trustAllHosts();
    }
    
    private HttpUtils() {
    }
    
    /**
     * Get Http cookies from a response.
     */
    public static void readCookies(HttpURLConnection conn, Set<String> cookies) {
        final List<String> newCookies = conn.getHeaderFields()
                .get("Set-Cookie");
        if (newCookies != null) {
            for (final String newCookie : newCookies) {
                cookies.add(newCookie.split(";", 2)[0]);
            }
        }
    }
    
    /**
     * Create a new Http connection for an URI.
     */
    public static HttpURLConnection newRequest(Context context, String uri,
            Set<String> cookies) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(uri)
                .openConnection();
        conn.setUseCaches(true);
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("User-Agent", generateUserAgent(context));
        
        if (conn instanceof HttpsURLConnection) {
            // Some Android devices (H**) has trouble with SSL:
            // as a workaround, we trust every host names.
            final HttpsURLConnection sConn = (HttpsURLConnection) conn;
            sConn.setHostnameVerifier(AcceptAllHostnamesVerifier.INSTANCE);
        }
        
        if (cookies != null) {
            final StringBuilder buf = new StringBuilder(128);
            for (final String cookie : cookies) {
                if (buf.length() != 0) {
                    buf.append("; ");
                }
                buf.append(cookie);
            }
            conn.addRequestProperty("Cookie", buf.toString());
        }
        
        return conn;
    }
    
    /**
     * Prepare a <code>POST</code> Http request.
     */
    public static HttpURLConnection newPostRequest(Context context, String uri,
            Map<String, String> params, String charset) throws IOException {
        final StringBuilder query = new StringBuilder();
        if (params != null) {
            for (final Map.Entry<String, String> e : params.entrySet()) {
                if (query.length() != 0) {
                    query.append("&");
                }
                query.append(e.getKey()).append("=")
                        .append(URLEncoder.encode(e.getValue()));
            }
        }
        
        final HttpURLConnection conn = newRequest(context, uri, null);
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded;charset=" + charset);
        
        final OutputStream queryOutput = conn.getOutputStream();
        try {
            queryOutput.write(query.toString().getBytes(charset));
        } finally {
            IOUtils.close(queryOutput);
        }
        
        return conn;
    }
    
    /**
     * Open the {@link InputStream} of an Http response. This method supports
     * GZIP responses.
     */
    public static InputStream getInputStream(HttpURLConnection conn)
            throws IOException {
        final List<String> contentEncodingValues = conn.getHeaderFields().get(
            "Content-Encoding");
        for (final String contentEncoding : contentEncodingValues) {
            if ("gzip".contains(contentEncoding)) {
                return new GZIPInputStream(conn.getInputStream());
            }
        }
        return conn.getInputStream();
    }
    
    /**
     * Get Http User Agent for this application.
     */
    private static final String generateUserAgent(Context context) {
        if (applicationVersion == null) {
            try {
                applicationVersion = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                applicationVersion = "0.0.0";
            }
        }
        return APPLICATION_NAME_USER_AGENT + "/" + applicationVersion + " ("
                + Build.MANUFACTURER + " " + Build.MODEL + " with Android "
                + Build.VERSION.RELEASE + "/" + Build.VERSION.SDK_INT + ")";
    }
    
    /**
     * Trust every server: do not check for any certificate. From:
     * http://stackoverflow.com/a/1000205/422906.
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains.
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
            
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }
            
            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }
        } };
        
        // Install the all-trusting trust manager.
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            Log.w(TAG, "SSL engine setup error", e);
        }
    }
    
    /**
     * {@link HostnameVerifier} accepting every host names.
     * @author Pixmob
     */
    private final static class AcceptAllHostnamesVerifier implements
            HostnameVerifier {
        public static final HostnameVerifier INSTANCE = new AcceptAllHostnamesVerifier();
        
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
