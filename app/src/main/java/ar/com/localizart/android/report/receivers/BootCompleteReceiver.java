package ar.com.localizart.android.report.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.service.InformationService;
import ar.com.localizart.android.report.ui.InformationActivity;

/**
 * Receiver which starts our InformationService when the Android device boots.
 * 
 * @author diego
 * 
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	private static final String TAG = BootCompleteReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "Starting service after booting...");

		context.startService(new Intent(context, InformationService.class));
	}

}
