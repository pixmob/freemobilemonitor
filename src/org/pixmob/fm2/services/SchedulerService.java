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
package org.pixmob.fm2.services;

import static org.pixmob.fm2.Constants.SHARED_PREFS;
import static org.pixmob.fm2.Constants.SP_KEY_PERFORM_UPDATES;
import static org.pixmob.fm2.Constants.SP_KEY_UPDATE_INTERVAL;
import static org.pixmob.fm2.Constants.TAG;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Schedule background account synchronizations.
 * @author Pixmob
 */
public class SchedulerService extends IntentService {
    public SchedulerService() {
        super("FM2/Scheduler");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        final PendingIntent syncIntent = PendingIntent.getService(this, 0,
            new Intent(this, SyncService.class).putExtra(
                SyncService.EXTRA_TRACK_UPDATES, true),
            PendingIntent.FLAG_CANCEL_CURRENT);
        
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFS,
            Context.MODE_PRIVATE);
        final long interval = Long.parseLong(prefs.getString(
            SP_KEY_UPDATE_INTERVAL,
            String.valueOf(AlarmManager.INTERVAL_FIFTEEN_MINUTES)));
        final boolean performUpdates = prefs.getBoolean(SP_KEY_PERFORM_UPDATES,
            true);
        
        final AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (interval == 0 || !performUpdates) {
            Log.i(TAG, "Canceling background account synchronization");
            am.cancel(syncIntent);
        } else {
            Log.i(TAG, "Scheduling background account synchronization every "
                    + interval + " ms");
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000 * 60 * 3, interval,
                syncIntent);
        }
    }
}
