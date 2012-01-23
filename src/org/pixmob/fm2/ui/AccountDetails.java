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

import org.pixmob.fm2.R;
import org.pixmob.fm2.model.Account;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Activity host for a {@link AccountDetailsFragment} instance.
 * @author Pixmob
 */
public class AccountDetails extends FragmentActivity {
    public static final String EXTRA_ACCOUNT = "account";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.nav);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        
        final Account account = (Account) getIntent().getSerializableExtra(
            EXTRA_ACCOUNT);
        if (account == null) {
            throw new IllegalStateException("Missing account");
        }
        
        if (savedInstanceState == null) {
            final FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            final AccountDetailsFragment f = AccountDetailsFragment
                    .newInstance(account);
            ft.add(android.R.id.content, f);
            ft.commit();
        }
    }
}
