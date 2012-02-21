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

import static org.pixmob.fm2.Constants.TAG;

import org.pixmob.fm2.model.Account;
import org.pixmob.fm2.model.AccountRepository;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Service for cleaning up application database.
 * @author Pixmob
 */
public class DatabaseCleanupService extends IntentService {
    public DatabaseCleanupService() {
        super("FM2/DatabaseCleanup");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Cleaning up application database");
        
        // Delete "weird" accounts.
        final AccountRepository repo = new AccountRepository(this);
        for (final Account account : repo.list()) {
            boolean invalidAccount = false;
            if (account.login == null) {
                invalidAccount = true;
            } else {
                final String login = account.login.trim();
                if (login.length() == 0 || "255".equals(login)) {
                    invalidAccount = true;
                }
            }
            
            if (invalidAccount) {
                repo.delete(account);
            }
        }
    }
}
