package ar.com.localizart.android.report.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ar.com.localizart.android.report.config.Configuration;

import static android.content.ContentValues.TAG;

public class ADTFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        new Configuration(this).setPushToken(refreshedToken);
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }
}
