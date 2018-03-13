package com.zendesk.adtapp;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zopim.android.sdk.api.ZopimChat;
import io.fabric.sdk.android.Fabric;

public class Global extends Application {

    private final static String LOG_TAG = Global.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());


        if("replace_me_chat_account_id".equals(getResources().getString(R.string.zopim_account_id))){
            Log.w(LOG_TAG, "==============================================================================================================");
            Log.w(LOG_TAG, "Zopim chat is not connected to an account, if you wish to try chat please add your Zopim accountId to 'zd.xml'");
            Log.w(LOG_TAG, "==============================================================================================================");
        }


        ZendeskConfig.INSTANCE.init(this, getResources().getString(R.string.zd_url), getResources().getString(R.string.zd_appid), getResources().getString(R.string.zd_oauth));

        ZopimChat.init(getResources().getString(R.string.zopim_account_id));
    }
}
