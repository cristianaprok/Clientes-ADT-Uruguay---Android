package com.zendesk.adtapp.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.zendesk.adtapp.R;
import com.zendesk.adtapp.model.UserProfile;
import com.zendesk.adtapp.storage.UserProfileStorage;

/**
 * Created by rahulramachandra on 09/10/17.
 */

public class ForgotPasswordActivity extends AppCompatActivity {
    private WebView webView;
    private UserProfileStorage mUserProfileStorage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_activity);
        mUserProfileStorage = new UserProfileStorage(this);
        webView = (WebView) findViewById(R.id.forgot_web);
        Boolean isM = getIntent().getBooleanExtra("IS_PASSWORD",false);
        if (isM){
            String imageUrl =  "https://www.adt.com.ar/numero-de-cliente";
            webView.loadUrl(imageUrl);
        }else {
            UserProfile userProfile = mUserProfileStorage.getProfile();

            String imageUrl =  "https://www.localizar-t.com/dashboard/clients/adt/ver_facturas.php?cliente="+userProfile.getAccountNumber().toString()+"&email="+userProfile.getEmail();
            webView.loadUrl(imageUrl);
        }


    }
}
