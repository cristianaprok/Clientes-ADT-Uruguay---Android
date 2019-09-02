package ar.com.localizart.android.report.info;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.enums.UtilConstants;
import ar.com.localizart.android.report.listeners.GPSListener;
import ar.com.localizart.android.report.listeners.NetworkListener;
import ar.com.localizart.android.report.vo.GPSVO;
import ar.com.localizart.android.report.vo.URLSerializable;

/**
 * Handler for location data.
 *
 * @author diego
 *
 */
public class LocationDataHandler implements DataHandler {

	private static final String TAG = LocationDataHandler.class.getSimpleName();

	/**
	 * Context data.
	 */
	private Context context;

	/**
	 * Configuration.
	 */
	private Configuration configuration;

	/**
	 * Location manager.
	 */
	private LocationManager locationManager;

	/**
	 * GPS location listener.
	 */
	private GPSListener gpsListener;

	/**
	 * Network location listener.
	 */
	private NetworkListener networkListener;

	/**
	 * <code>true</code> if GPS location is enabled.
	 */
	private boolean gpsEnabled;

	/**
	 * <code>true</code> if network location is enabled.
	 */
	private boolean networkEnabled;

	/**
	 * The current location.
	 */
	private volatile Location location;

	/**
	 * Constructor.
	 *
	 * @param context
	 */
	public LocationDataHandler(Context context) {
		this.context = context;
	}

	/**
	 * Data handler initialization.
	 */
	@Override
	public void init() {
		System.out.println("////////////////// OCATION DATA HANDLER INIT/////////");
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		System.out.println("////////////////// OCATION DATA HANDLER SET GPS ENBLED/////////");
		// Set the provider enable flags:
		setGpsEnabled(locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER));

		System.out.println("////////////////// OCATION DATA HANDLER SET NETWORK ENBLED/////////");
		setNetworkEnabled(locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

		// Reports must be accurate and not report old locations, so by default
		// we don't use the "last known location" feature.
		boolean useLastKnownLocation = configuration.getUseLastKnownLocation(false);

		System.out.println("////////////////// OCATION DATA HANDLER ISGPSENABLED/////////" + isGpsEnabled());
		// Setup the provider listeners:
		if (isGpsEnabled()) {
			Log.v(TAG, "Using location provider: "
					+ LocationManager.GPS_PROVIDER);
			gpsListener = new GPSListener(this);
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, gpsListener);

			if (useLastKnownLocation) {
				System.out.println("////////////////// OCATION DATA HANDLER LAST KNOWN LOCATION/////////");
				// Since GPS location takes time (minutes), use the last known
				// location for the initial GPS data.
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			} else {
				System.out.println("////////////////// OCATION DATA HANDLER CLEAR GPS CACHE/////////");
				// If we are not using this feature, let's clear the GPS cache:
				locationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
						"delete_aiding_data", null);
			}
		}

		if (isNetworkEnabled()) {
			Log.v(TAG, "Using location provider: "
					+ LocationManager.NETWORK_PROVIDER);
			networkListener = new NetworkListener(this);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);

			if (useLastKnownLocation) {
				// Use last known location (see above for an explanation).
				location = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} else {
				// Don't send a delete extra command here, since the Network
				// provider doesn't implement it (that's only available for the
				// GPS provider).
			}
		}
	}

	/**
	 * Obtain the data and return it.
	 * 
	 * @return
	 */
	@Override
	public URLSerializable handle() {
		if (locationManager != null) {
			return getGPSVO();
		}

		return null;
	}

	/**
	 * Execute pre-finalization code.
	 */
	@Override
	public void destroy() {
		if (locationManager != null) {
			if (isGpsEnabled() && gpsListener != null) {
				locationManager.removeUpdates(gpsListener);
			}

			if (isNetworkEnabled() && networkListener != null) {
				locationManager.removeUpdates(networkListener);
			}
		}
	}

	/**
	 * Build a GPS value object based on the current location information.
	 * 
	 * @return
	 */
	private synchronized GPSVO getGPSVO() {
		/*//JM */
		System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		if ((isGpsEnabled() || isNetworkEnabled()) && location != null) {
			GPSVO gpsVO = new GPSVO(); //JM creamos el GPSVO, aunque no tenga datos hay que emv�ar si tenemso el GPS habilitado
										//PREGUNTAR si se quiere comprobar solo si hay GPS, o en cambio si hay posici�n auqnue sea de radio
			
			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO new xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

		//if ((isGpsEnabled() || isNetworkEnabled()) && location != null) { /*//JM */
			
			if (location.hasAltitude()) {
				gpsVO.setAltitude(location.getAltitude());
			} else {
				gpsVO.setAltitude(-1);
			}

			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO 2xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			/*
			 * Bearing is the direction to the destination or target. Heading is
			 * the direction you are traveling or facing. They are not exactly
			 * the same, but we don't have heading in Android.
			 */
			gpsVO.setHeading(location.getBearing());

			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO 3xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			gpsVO.setLatitude(location.getLatitude());
			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO 4xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			gpsVO.setLongitude(location.getLongitude());

			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO 5xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			if (location.hasSpeed()) {
				gpsVO.setSpeed(location.getSpeed());
			} else {
				gpsVO.setSpeed(-1);
			}
			
			gpsVO.setAccuracy(location.getAccuracy());
			
			if (isGpsEnabled()){ 
				gpsVO.setGPSEnabled(true);
			}
			
			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO return xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

			if (location.getTime() != 0) {
				//long elapsed = ((location.getTime()) / 1000);

				String dia = getDate(location.getTime(), "dd/MM/yyyy hh:mm:ss");
				gpsVO.setTime(dia);
			} else {
				gpsVO.setTime("-1");
			}
			
			System.out.println("xxxxxxxxxxxxxxxxxxxxx  GET GPSVO time xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			
			return gpsVO;
		}

		return new GPSVO();
	}

	
	/**
	 * Return date in specified format.
	 * @param milliSeconds Date in milliseconds
	 * @param dateFormat Date format 
	 * @return String representing date in specified format
	 */
	public static String getDate(long milliSeconds, String dateFormat)
	{
	    // Create a DateFormatter object for displaying date in specified format.
	    DateFormat formatter = new SimpleDateFormat(dateFormat);

	    // Create a calendar object that will convert the date and time value in milliseconds to date. 
	     Calendar calendar = Calendar.getInstance();
	     calendar.setTimeInMillis(milliSeconds);
	     return formatter.format(calendar.getTime());
	}
	/**
	 * Getter for gpsEnabled.
	 * 
	 * @return
	 */
	public boolean isGpsEnabled() {
		/*//JM */
		gpsEnabled=locationManager
		.isProviderEnabled(LocationManager.GPS_PROVIDER);
		////////////////
		
		return gpsEnabled;
	}

	/**
	 * Setter for gpsEnabled.
	 * 
	 * @param gpsEnabled
	 */
	public void setGpsEnabled(boolean gpsEnabled) {
		this.gpsEnabled = gpsEnabled;
	}

	/**
	 * Getter for networkEnabled.
	 * 
	 * @return
	 */
	public synchronized boolean isNetworkEnabled() {
		/*//JM */ //chekeamos cada vez si est� habilitado, hacerlo solo en el init no es suficiente^
		networkEnabled=locationManager
		.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		/////////
		return networkEnabled;
	}
	
	
	
	


	/**
	 * Setter for networkEnabled.
	 * 
	 * @param networkEnabled
	 */
	public synchronized void setNetworkEnabled(boolean networkEnabled) {
		this.networkEnabled = networkEnabled;
	}

	/**
	 * Getter for location.
	 * 
	 * @return
	 */
	public synchronized Location getLocation() {
		return location;
	}

	/**
	 * Setter for location.
	 * 
	 * The new location is only set if it's better than the existing one.
	 * 
	 * @param location
	 */
	public synchronized void setLocation(Location newLocation) {
		if (isBetterLocation(newLocation, this.location)) {
			this.location = newLocation;
		}
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix.
	 * 
	 * @param location
	 *            The new Location that you want to evaluate.
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one.
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location:
			return true;
		}

		// Check whether the new location fix is newer or older:
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > UtilConstants.TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -UtilConstants.TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location because the user has likely moved:
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse.
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate:
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider:
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy:
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}

		return false;
	}

	/**
	 * Checks whether two providers are the same.
	 * 
	 * @param provider1
	 * @param provider2
	 * @return
	 */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
}
