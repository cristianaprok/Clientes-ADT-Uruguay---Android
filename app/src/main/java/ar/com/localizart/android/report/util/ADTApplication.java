package ar.com.localizart.android.report.util;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.app.NotificationManager;
import android.os.Build;

import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.service.InformationService;
import ar.com.localizart.android.report.ui.InformationActivity;
import io.fabric.sdk.android.Fabric;


public class ADTApplication extends Application {
    private static final String TAG = ADTApplication.class.getSimpleName();
    public static ADTApplication Instance;
    public static ADTApplication getInstance() {
        return Instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Log.d(TAG, "ADT Application Created");
        Instance = this;
        FirebaseApp.initializeApp(this);
    }

    public boolean isServiceRunning() {
        return isMyServiceRunning(InformationService.class);
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void restartService() {
        if (!isServiceRunning()) {
            startService(new Intent(Instance, InformationService.class));
        }
    }



}
