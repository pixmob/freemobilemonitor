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
package org.pixmob.fm2.receivers;

import static org.pixmob.fm2.Constants.TAG;

import org.pixmob.fm2.services.SchedulerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Track network connectivity.
 * @author Pixmob
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isAvailable() && info.isConnected()) {
            Log.i(TAG, "Network is available");
            
            // The network becomes available: schedule background account
            // synchronizations.
            context.startService(new Intent(context, SchedulerService.class));
        } else {
            Log.i(TAG, "Network is NOT available");
        }
    }
}
