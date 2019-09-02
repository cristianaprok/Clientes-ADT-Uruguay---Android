package ar.com.localizart.android.report.listeners;

import ar.com.localizart.android.report.info.LocationDataHandler;

/**
 * Listener for GPS location information.
 * 
 * @author diego
 * 
 */
public class GPSListener extends LocationProviderListener {

	/**
	 * Constructor.
	 * 
	 * @param locationDataHandler
	 */
	public GPSListener(LocationDataHandler locationDataHandler) {
		super(locationDataHandler);
	}

	/**
	 * Disable the GPS data handling if the provider is disabled.
	 */
	@Override
	public void onProviderDisabled(String provider) {
		getLocationDataHandler().setGpsEnabled(false);
	}

	/**
	 * Enable GPS data handling again, if the provider comes back.
	 */
	@Override
	public void onProviderEnabled(String provider) {
		getLocationDataHandler().setGpsEnabled(true);
	}

}
