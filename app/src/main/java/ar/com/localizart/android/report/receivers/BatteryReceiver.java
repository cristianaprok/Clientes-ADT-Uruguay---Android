package ar.com.localizart.android.report.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ar.com.localizart.android.report.enums.BatteryStatus;
import ar.com.localizart.android.report.vo.BatteryVO;

/**
 * Receiver to obtain battery status information.
 * 
 * @author diego
 * 
 */
public class BatteryReceiver extends BroadcastReceiver {

	/**
	 * Value object to hold battery status information.
	 */
	private volatile BatteryVO batteryVO;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			// We need to synchronize this, since receivers that are started
			// manually, run in another thread.
			synchronized (this) {
				if (batteryVO == null) {
					batteryVO = new BatteryVO();
				}
/*
				batteryVO.setLevel(intent.getIntExtra(
						BatteryManager.EXTRA_LEVEL, -1));
				batteryVO.setScale(intent.getIntExtra(
						BatteryManager.EXTRA_SCALE, -1));

				int status = intent
						.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				batteryVO.setStatus(BatteryStatus.fromStatus(status));
*/
				int level = intent.getIntExtra("level", -1);
				batteryVO.setLevel(level);
				
				int scale = intent.getIntExtra("scale", -1);
				batteryVO.setScale(scale);

				int status = intent.getIntExtra("status", -1);
				batteryVO.setStatus(BatteryStatus.fromStatus(status));
				
				System.out.println(" BATTERY LEVEL:"+level+"   SCALE: "+scale+"   STATUS: "+status);
				// Wake up all threads waiting for this.
				notifyAll();
			}
		}
	}

	/**
	 * Battery value object getter.
	 * 
	 * @return
	 */
	public synchronized BatteryVO getBatteryVO() {
		return batteryVO;
	}

}
