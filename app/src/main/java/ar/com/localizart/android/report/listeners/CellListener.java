package ar.com.localizart.android.report.listeners;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * Listener for cell location changes.
 * 
 * @author diego
 * 
 */
public class CellListener extends PhoneStateListener {
	private static final String TAG = CellListener.class.getSimpleName();

	
	/* //JM */
	private int signal = 0;
	TelephonyManager telephonyManager;
	public CellListener(TelephonyManager telephonyManager) {

		this.telephonyManager = telephonyManager;
	}
	
	/*//JM */
	public int getSignal(){
		
		return signal;
	}
	
	/**
	 * GSM cell location.
	 */
	private volatile GsmCellLocation gsmCellLocation = null;

	@Override
	public void onCellLocationChanged(CellLocation location) {
		super.onCellLocationChanged(location);

		if (location != null && location instanceof GsmCellLocation) {
			Log.v(TAG, "Cell location changed:" + location);

			synchronized (this) {
				gsmCellLocation = (GsmCellLocation) location;

				notifyAll();
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.telephony.PhoneStateListener#onSignalStrengthsChanged(android.telephony.SignalStrength)
	 * //JM
	 */
	@Override
	public void onSignalStrengthChanged(int asu) {
		signal = asu;
	};
	/* api 5 */
	/*
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		
		 if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
			 signal = signalStrength.getCdmaDbm();
	            
	     } else if  (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
	    	 signal =  signalStrength.getGsmSignalStrength();
	            
	     }
		
		
		System.out.println( "oooooooo Cell signal Strength changed: ooooooo>>> " +signal);
		
		
		super.onSignalStrengthsChanged(signalStrength);
	}
	*/
	
	/**
	 * GSM cell location getter.
	 * 
	 * @return
	 */
	public synchronized GsmCellLocation getGsmCellLocation() {
		return gsmCellLocation;
	}
}
