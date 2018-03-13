package com.zendesk.adtapp.model;

import android.graphics.Bitmap;

/**
 * Data model for a user profile
 */
public class UserProfile {

    private String mName;

    private String mEmail;

    private String mAccountNumber;

    private Bitmap mAvatar;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setAccountNumber(String number) {
        this.mAccountNumber = number;
    }

    public String getAccountNumber() {
        return mAccountNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public void setAvatar(Bitmap avatar) {
        this.mAvatar = avatar;
    }

    public Bitmap getAvatar() {
        return mAvatar;
    }
}
