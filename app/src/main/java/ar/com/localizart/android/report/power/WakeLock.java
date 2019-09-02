package ar.com.localizart.android.report.power;

import android.content.Context;
import android.os.PowerManager;

/**
 * Class to manage locks (to keep the CPU always ON).
 * 
 * @author diego
 * 
 */
public class WakeLock {
	private static final String TAG = WakeLock.class.getName();

	private static volatile PowerManager.WakeLock wakeLock = null;

	/**
	 * Acquire lock.
	 * 
	 * @param context
	 */
	public synchronized static void acquireWakeLock(Context context) {
		getLock(context).acquire();
	}

	/**
	 * Release lock.
	 * 
	 * @param context
	 */
	public synchronized static void releaseWakeLock(Context context) {
		getLock(context).release();
	}

	/**
	 * Obtains the lock.
	 * 
	 * @param context
	 * @return
	 */
	private synchronized static PowerManager.WakeLock getLock(Context context) {
		if (wakeLock == null && context != null) {
			PowerManager powerManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);

			// Keep only the CPU ON (screen and keyboard may be OFF):
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					TAG);

			wakeLock.setReferenceCounted(true);
		}

		return wakeLock;
	}
}
