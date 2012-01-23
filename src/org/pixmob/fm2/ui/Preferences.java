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
package org.pixmob.fm2.ui;

import static org.pixmob.fm2.Constants.SHARED_PREFS;
import static org.pixmob.fm2.Constants.TAG;

import org.pixmob.fm2.R;
import org.pixmob.fm2.services.SchedulerService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Activity for setting application preferences.
 * @author Pixmob
 */
public class Preferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesMode(MODE_PRIVATE);
        pm.setSharedPreferencesName(SHARED_PREFS);
        
        addPreferencesFromResource(R.xml.prefs);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if ("performUpdates".equals(key) || "updateInterval".equals(key)) {
            Log.i(TAG, "Update background account "
                    + "synchronization schedules");
            
            startService(new Intent(Preferences.this, SchedulerService.class));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
