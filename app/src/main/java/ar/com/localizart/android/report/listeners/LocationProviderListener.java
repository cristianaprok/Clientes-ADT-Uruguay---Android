package ar.com.localizart.android.report.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import ar.com.localizart.android.report.info.LocationDataHandler;

/**
 * Base class for our location listeners.
 * 
 * @author diego
 * 
 */
public abstract class LocationProviderListener implements LocationListener {

	/**
	 * Caller location data handler (needed because we might disable or enable
	 * it based on the listener reported status).
	 */
	private LocationDataHandler locationDataHandler;

	/**
	 * Constructor.
	 * 
	 * @param locationDataHandler
	 */
	public LocationProviderListener(LocationDataHandler locationDataHandler) {
		super();
		this.locationDataHandler = locationDataHandler;
	}

	/**
	 * Set the new location when it changes.
	 */
	@Override
	public void onLocationChanged(Location location) {
		locationDataHandler.setLocation(location);
	}

	/**
	 * Enable or disable location data handling based on the provider status.
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE
				|| status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			// Disable it, it's not available:
			this.onProviderDisabled(provider);
		} else if (status == LocationProvider.AVAILABLE) {
			// Enable it again when it becomes available:
			this.onProviderEnabled(provider);
		}
	}

	/**
	 * Location data handler getter.
	 * 
	 * @return
	 */
	protected LocationDataHandler getLocationDataHandler() {
		return locationDataHandler;
	}
}
