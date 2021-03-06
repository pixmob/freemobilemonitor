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
package org.pixmob.fm2.model;

import static org.pixmob.fm2.Constants.DEBUG;
import static org.pixmob.fm2.Constants.TAG;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A repository for managing {@link Account} instances in a local database.
 * @author Pixmob
 */
public class AccountRepository {
    private static final String ACCOUNTS_TABLE = "accounts";
    private static final Object LOCK = new Object();
    private final SQLiteOpenHelper dbHelper;
    
    public AccountRepository(final Context context) {
        dbHelper = new DbHelper(context);
    }
    
    private static void close(SQLiteDatabase db) {
        if (db != null) {
            db.close();
        }
    }
    
    /**
     * Get account list.
     */
    public List<Account> list() {
        final List<Account> accounts = new ArrayList<Account>(4);
        
        synchronized (LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getReadableDatabase();
                Cursor c = null;
                try {
                    c = db.query(ACCOUNTS_TABLE, new String[] { "id", "name",
                            "phone_number", "login", "password", "status",
                            "timestamp" }, null, null, null, null, "name");
                    
                    while (c.moveToNext()) {
                        final Account account = new Account();
                        account.id = c.getInt(0);
                        account.name = c.getString(1);
                        account.phoneNumber = c.getString(2);
                        account.login = c.getString(3);
                        account.password = c.getString(4);
                        account.status = c.getInt(5);
                        account.timestamp = c.getLong(6);
                        accounts.add(account);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            } finally {
                close(db);
            }
        }
        
        return accounts;
    }
    
    /**
     * Create a new account.
     */
    public void create(String login, String password) {
        assert login != null;
        assert password != null;
        
        if (DEBUG) {
            Log.d(TAG, "Creating new account: " + login);
        }
        
        final ContentValues cv = new ContentValues(2);
        cv.put("login", login);
        cv.put("password", password);
        
        synchronized (LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    db.insertOrThrow(ACCOUNTS_TABLE, "id", cv);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            } finally {
                close(db);
            }
        }
    }
    
    /**
     * Delete an account.
     */
    public void delete(Account account) {
        if (DEBUG) {
            Log.d(TAG, "Deleting account: " + account.login);
        }
        
        synchronized (LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    db.delete(ACCOUNTS_TABLE, "id=?",
                        new String[] { String.valueOf(account.id) });
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            } finally {
                close(db);
            }
        }
    }
    
    /**
     * Update an account.
     */
    public void update(Account account) {
        if (DEBUG) {
            Log.d(TAG, "Updating account: " + account.login);
        }
        
        final ContentValues cv = new ContentValues(4);
        cv.put("name", account.name);
        cv.put("phone_number", account.phoneNumber);
        cv.put("status", account.status);
        cv.put("timestamp", account.timestamp);
        
        synchronized (LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    db.update(ACCOUNTS_TABLE, cv, "id=?",
                        new String[] { String.valueOf(account.id) });
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            } finally {
                close(db);
            }
        }
    }
    
    /**
     * Internal class for creating the application database.
     * @author Pixmob
     */
    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(final Context context) {
            super(context, "accounts.db", null, 2);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "
                    + ACCOUNTS_TABLE
                    + " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "login VARCHAR NOT NULL, password VARCHAR NOT NULL, "
                    + "status INTEGER NOT NULL DEFAULT 0, "
                    + "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "name VARCHAR, phone_number VARCHAR);");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Clean up database. Earlier versions of this application did not
            // check user input when creating an account. It was possible to
            // create an account with an empty login (or the weird value 255).
            db.execSQL("DELETE FROM " + ACCOUNTS_TABLE
                    + " WHERE login=? OR login=?", new Object[] { "", "255" });
        }
    }
}
