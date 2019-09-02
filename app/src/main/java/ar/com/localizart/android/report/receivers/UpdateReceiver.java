package ar.com.localizart.android.report.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ar.com.localizart.android.report.service.InformationService;

/**
 * Receiver which starts our InformationService when the application is updated (via Market only).
 * 
 * @author diego
 * 
 */
public class UpdateReceiver extends BroadcastReceiver {

	private static final String TAG = UpdateReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "Starting service after update...");

// On API versions below 12, we must use "android.intent.action.PACKAGE_REPLACED" to capture the application update event. But this is called for any application being installed, thus we have to specify the package, to know when "this" app was updated. Versions 12 and above have another intent to solve this issue.
    if (intent.getDataString().contains("ar.com.localizart.android.report")){
        context.startService(new Intent(context, InformationService.class));
    }
	}

}
