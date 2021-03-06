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
package org.pixmob.fm2.net;

import static org.pixmob.fm2.Constants.DEBUG;
import static org.pixmob.fm2.Constants.TAG;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pixmob.fm2.model.Account;
import org.pixmob.fm2.util.HttpUtils;
import org.pixmob.fm2.util.IOUtils;

import android.content.Context;
import android.util.Log;

/**
 * Read account updates from the Free Mobile website.
 * @author Pixmob
 */
public class AccountNetworkClient {
    private static final String CHARSET = "UTF-8";
    private static final long[] DIGIT_CRCS = new long[10];
    
    static {
        DIGIT_CRCS[0] = 3376728150L;
        DIGIT_CRCS[1] = 3751550807L;
        DIGIT_CRCS[2] = 478599653L;
        DIGIT_CRCS[3] = 3783288966L;
        DIGIT_CRCS[4] = 1283656032L;
        DIGIT_CRCS[5] = 2508031604L;
        DIGIT_CRCS[6] = 804711360L;
        DIGIT_CRCS[7] = 40629710L;
        DIGIT_CRCS[8] = 271195857L;
        DIGIT_CRCS[9] = 4291279220L;
    }
    
    private final Context context;
    
    public AccountNetworkClient(final Context context) {
        this.context = context;
    }
    
    /**
     * Connect to the Free Mobile website and get account updates.
     */
    public void update(Account account) throws IOException {
        // FIXME Remove this when the nasty bug about "weird" accounts is fixed.
        if (account.login.length() < 8) {
            throw new IOException("Invalid user: " + account.login + " ["
                    + account.id + "]");
        }
        
        final Set<String> cookies = new HashSet<String>(4);
        if (!authenticate(account, cookies)) {
            throw new IOException("Authentication failed for user "
                    + account.login);
        }
        
        final File accountDataFile = fetchAccountData(account.login, cookies);
        logout(account.login, cookies);
        try {
            parseAccountData(accountDataFile, account);
            Log.i(TAG, "User " + account.login + " has status "
                    + account.status);
        } finally {
            accountDataFile.delete();
        }
    }
    
    /**
     * Authenticate an account. Authentication cookies are added in
     * <code>cookies</code>.
     * @return <code>true</code> if authentication was successful
     */
    public boolean authenticate(Account account, Set<String> cookies)
            throws IOException {
        final String encodedLogin = encodeLogin(account, cookies);
        
        final Map<String, String> params = new HashMap<String, String>(2);
        params.put("login_abo", encodedLogin);
        params.put("pwd_abo", account.password);
        
        Log.i(TAG, "Authenticating user " + account.login);
        
        final HttpURLConnection conn = HttpUtils
                .newPostRequest(
                    context,
                    "https://mobile.free.fr/moncompte/index.php?page=commande&produit=sim",
                    cookies, params, CHARSET);
        try {
            conn.connect();
            final int sc = conn.getResponseCode();
            if (DEBUG) {
                Log.d(TAG, "Got response: " + sc);
            }
            if (sc != HttpURLConnection.HTTP_MOVED_TEMP
                    && sc != HttpURLConnection.HTTP_OK) {
                return false;
            }
            
            Log.i(TAG, "User " + account.login + " authenticated");
            HttpUtils.readCookies(conn, cookies);
        } finally {
            conn.disconnect();
        }
        
        return true;
    }
    
    private String encodeLogin(Account account, Set<String> cookies)
            throws IOException {
        Log.i(TAG, "Encoding login for user " + account.login);
        
        // Make a first request to get cookies.
        final HttpURLConnection conn = HttpUtils.newRequest(context,
            "https://mobile.free.fr/moncompte/index.php", cookies);
        try {
            conn.connect();
            
            final int sc = conn.getResponseCode();
            if (sc != HttpURLConnection.HTTP_OK) {
                throw new IOException("Login encoding failed for user "
                        + account.login);
            }
            
            HttpUtils.readCookies(conn, cookies);
        } finally {
            conn.disconnect();
        }
        
        try {
            final Map<String, String> cipherMap = fetchCipherMap(cookies);
            if (DEBUG) {
                Log.d(TAG, "Cipher map for user " + account.login + ": "
                        + cipherMap);
            }
            
            final StringBuilder encodedLogin = new StringBuilder(10);
            final int loginLen = account.login.length();
            
            for (int i = 0; i < loginLen; ++i) {
                final String key = String.valueOf(account.login.charAt(i));
                final String encoded = cipherMap.get(key);
                if (encoded == null) {
                    throw new IOException("Digit encoding failed for user "
                            + account.login + ": " + key);
                }
                encodedLogin.append(encoded);
            }
            
            if (DEBUG) {
                Log.d(TAG, "Login encoded: " + account.login + "=>"
                        + encodedLogin);
            }
            
            return encodedLogin.toString();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            final IOException ioe = new IOException(
                    "Login encoding failed for user " + account.login);
            ioe.initCause(e);
            throw ioe;
        }
    }
    
    private Map<String, String> fetchCipherMap(Set<String> cookies)
            throws IOException {
        final Map<String, String> cipherMap = new HashMap<String, String>(10);
        final File temp = new File(context.getCacheDir(), "digit.png");
        final byte[] buf = new byte[2048];
        final CRC32 crc = new CRC32();
        
        for (int pos = 0; pos < 10; ++pos) {
            final String uri = "https://mobile.free.fr/moncompte/chiffre.php?pos="
                    + pos;
            HttpUtils.downloadToFile(context, uri, cookies, temp);
            
            final InputStream input = new FileInputStream(temp);
            try {
                for (int bytesRead; (bytesRead = input.read(buf)) != -1;) {
                    crc.update(buf, 0, bytesRead);
                }
            } finally {
                IOUtils.close(input);
                temp.delete();
            }
            
            final long c = crc.getValue();
            boolean found = false;
            for (int i = 0; !found && i < DIGIT_CRCS.length; ++i) {
                if (c == DIGIT_CRCS[i]) {
                    cipherMap.put(String.valueOf(i), String.valueOf(pos));
                    found = true;
                }
            }
            
            if (!found) {
                throw new IOException("Unknown digit CRC: " + c + "; pos="
                        + pos);
            }
            
            crc.reset();
        }
        
        return cipherMap;
    }
    
    private File fetchAccountData(String userLogin, Set<String> cookies)
            throws IOException {
        Log.i(TAG, "Fetching status for user " + userLogin);
        
        final HttpURLConnection conn = HttpUtils
                .newRequest(
                    context,
                    "https://mobile.free.fr/moncompte/index.php?page=commande&produit=sim",
                    cookies);
        try {
            conn.connect();
            final int sc = conn.getResponseCode();
            if (DEBUG) {
                Log.d(TAG, "Got response: " + sc);
            }
            if (sc != HttpURLConnection.HTTP_OK) {
                throw new IOException("Status update failed");
            }
            
            HttpUtils.readCookies(conn, cookies);
            
            final File outputDir = context.getCacheDir();
            final File outputFile = new File(outputDir, "account_" + userLogin
                    + ".html");
            IOUtils.writeToFile(HttpUtils.getInputStream(conn), outputFile);
            
            if (DEBUG) {
                Log.d(TAG,
                    "Output written to file: " + outputFile.getAbsolutePath());
            }
            
            return outputFile;
        } finally {
            conn.disconnect();
        }
    }
    
    private void logout(String userLogin, Set<String> cookies)
            throws IOException {
        Log.i(TAG, "Closing session for user " + userLogin);
        final HttpURLConnection conn = HttpUtils.newRequest(context,
            "https://mobile.free.fr/moncompte/index.php?act=logout", cookies);
        try {
            conn.connect();
            final int sc = conn.getResponseCode();
            if (DEBUG) {
                Log.d(TAG, "Got response: " + sc);
            }
        } finally {
            conn.disconnect();
        }
    }
    
    private void parseAccountData(File accountDataFile, Account account)
            throws IOException {
        try {
            final Document doc = Jsoup.parse(accountDataFile, null);
            final Elements h4Nodes = doc.select("h4[class]");
            final int h4NodesLen = h4Nodes.size();
            
            boolean statusFound = false;
            account.status = 0;
            
            for (int i = 0; i < h4NodesLen; ++i) {
                final Element elem = h4Nodes.get(i);
                if (elem.hasClass("actif1") || elem.hasClass("inactif1")) {
                    statusFound = true;
                }
                
                if (elem.hasClass("actif1")) {
                    account.status += 1;
                }
            }
            if (!statusFound) {
                throw new IOException("No account data found for user "
                        + account.login);
            }
            
            final Elements produitNodes = doc.select("td[class]");
            for (final Element elem : produitNodes) {
                if (elem.hasClass("produit")) {
                    final String text = elem.text();
                    final int i = text.indexOf("Forfait : ");
                    if (i != -1) {
                        account.name = text
                                .substring(i + "Forfait : ".length()).trim();
                    }
                    
                    final int j = text.indexOf("Numéro : ");
                    if (j != -1) {
                        final int k = text.indexOf(" (Numéro", j);
                        if (k != -1) {
                            account.phoneNumber = text.substring(
                                j + "Numéro : ".length(), k).trim();
                        } else {
                            account.phoneNumber = text.substring(
                                j + "Numéro : ".length()).trim();
                        }
                    }
                }
            }
            
            account.timestamp = System.currentTimeMillis();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            final IOException ioe = new IOException(
                    "User status parsing failed");
            ioe.initCause(e);
            throw ioe;
        }
    }
}
