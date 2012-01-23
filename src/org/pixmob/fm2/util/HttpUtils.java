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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

/**
 * Http utilities.
 * @author Pixmob
 */
public final class HttpUtils {
    private static String applicationVersion;
    
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
    
}
