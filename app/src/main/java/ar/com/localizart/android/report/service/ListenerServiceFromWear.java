package ar.com.localizart.android.report.service;

/**
 * Created by anshad on 29/5/16.
 */


import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import ar.com.localizart.android.report.enums.Constants;

/**
 * Created by anshad on 1/6/16.
 */
public class ListenerServiceFromWear extends WearableListenerService {

    private static final String PANIC_LISTENER_WEAR_PATH = "/panic-listener-wear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        /*
         * Receive the message from wear
         */

        if (messageEvent.getPath().equals(PANIC_LISTENER_WEAR_PATH)) {

//            Intent startIntent = new Intent(this, MainActivity.class);
//            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startIntent);
            Intent intent=new Intent();
            intent.setAction(Constants.ACTION_WEARABLE_PANIC_ALERT);
            sendBroadcast(intent);
            System.out.println("messaged received");
        }

    }

}