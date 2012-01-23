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

import java.io.Serializable;

/**
 * This class stores data about an user account. As soon as an account is
 * submitted by the user, the application will be able to check for updates.
 * Some attributes may be <code>null</code> (except for {@link #login} and
 * {@link #password}).
 * @author Pixmob
 * @see AccountRepository
 */
public final class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Account database identifier.
     */
    public int id;
    /**
     * Account name, related to the contract name.
     */
    public String name;
    /**
     * Account phone number.
     */
    public String phoneNumber;
    /**
     * Account login.
     */
    public String login;
    /**
     * Account password.
     */
    public String password;
    /**
     * Account status, for <code>1</code> to <code>3</code>. The value
     * <code>0</code> means "unknown status".
     */
    public int status;
    /**
     * Account timestamp: when was this account last updated?
     */
    public long timestamp;
    
    @Override
    public String toString() {
        return "Account[id=" + id + ", login=" + login + ", password="
                + password + "]";
    }
}
