package com.zendesk.adtapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.zendesk.adtapp.storage.UserProfileStorage;
import com.zendesk.adtapp.model.UserProfile;
import com.zendesk.sdk.support.SupportActivity;
import com.zendesk.util.StringUtils;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;
import com.zopim.android.sdk.prechat.ZopimChatActivity;

/**
 * Created by rahulramachandra on 20/06/17.
 */

public class HomeActivity extends SupportActivity {
    private UserProfileStorage mUserProfileStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUserProfileStorage = new UserProfileStorage(this);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setHomeAsUpIndicator(com.zendesk.adtapp.R.drawable.chaticon);

        if (!isUserDataAvailable()){
            startActivity(new Intent(this, CreateProfileActivity.class));
        }

    }
    public boolean isUserDataAvailable (){
        final UserProfile profile = mUserProfileStorage.getProfile();
        if(StringUtils.hasLength(profile.getEmail())){
            return true;
        }else{
            return false;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                Toast.makeText(getApplicationContext(),"Back button clicked", Toast.LENGTH_SHORT).show();
                if (isUserDataAvailable()){
                    final UserProfile profile = mUserProfileStorage.getProfile();
                    VisitorInfo visitorData = new VisitorInfo.Builder()
                            .name(profile.getName())
                            .email(profile.getEmail())
                            .phoneNumber("")
                            .build();
                    ZopimChat.setVisitorInfo(visitorData);
                    startActivity(new Intent(this, ZopimChatActivity.class));
                }else {
                    startActivity(new Intent(this, CreateProfileActivity.class));

                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
