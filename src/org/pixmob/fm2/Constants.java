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
package org.pixmob.fm2;

/**
 * Application constants.
 * @author Pixmob
 */
public final class Constants {
    /**
     * Application for the Http User Agent.
     */
    public static final String APPLICATION_NAME_USER_AGENT = "FreeMobileMonitor";
    
    /**
     * Log tag.
     */
    public static final String TAG = "FM2";
    
    /**
     * Is this application running in debug mode?
     */
    public static final boolean DEBUG = true;
    
    /**
     * Shared preferences file name.
     */
    public static final String SHARED_PREFS = "fm2prefs";
    
    /**
     * Preference key: background synchronization period (ms).
     */
    public static final String SP_KEY_UPDATE_INTERVAL = "updateInterval";
    
    /**
     * Preference key: enable automatic updates.
     */
    public static final String SP_KEY_PERFORM_UPDATES = "performUpdates";
    
    /**
     * Preference key: disable SSL certificate check.
     */
    public static final String SP_KEY_DISABLE_CERTIFICATE_CHECK = "disableCertificateCheck";
    
    private Constants() {
    }
}
