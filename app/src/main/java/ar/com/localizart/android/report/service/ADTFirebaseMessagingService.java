package ar.com.localizart.android.report.service;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.util.ADTApplication;

import static android.content.ContentValues.TAG;

public class ADTFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "FireBaseMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG, "onMessageReceived: " + remoteMessage);

        if (ADTApplication.getInstance().isServiceRunning()) {
            //Some action
            Log.e(TAG, "onMessageReceived: Service is Running");
            String title = getString(R.string.status_bar_notifications_running_title);
            String body = getString(R.string.status_bar_notifications_running_text);
            Answers.getInstance().logCustom(new CustomEvent("ADTFirebaseMessagingService")
                    .putCustomAttribute("message_id", remoteMessage.getMessageId())
                    .putCustomAttribute("is_service_alive", 1));
        } else {
            Log.e(TAG, "onMessageReceived: Service not running -> Restarting");
            Answers.getInstance().logCustom(new CustomEvent("ADTFirebaseMessagingService")
                    .putCustomAttribute("message_id", remoteMessage.getMessageId())
                    .putCustomAttribute("is_service_alive", 0));

            Answers.getInstance().logCustom(new CustomEvent("Service Restart")
                    .putCustomAttribute("message_id", remoteMessage.getMessageId())
                    .putCustomAttribute("is_service_alive", 0));

            ADTApplication.getInstance().restartService();
        }
    }
}
