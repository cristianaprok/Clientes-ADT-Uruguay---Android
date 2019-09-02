package ar.com.localizart.android.report.vo;

import android.net.Uri;

/**
 * GPS information value object.
 * 
 * @author diego
 * 
 */
public class GPSVO implements URLSerializable {
	private static final long serialVersionUID = 1L;

	private double heading;
	private double longitude;
	private double latitude;
	private double altitude;
	private double speed;
	private String time;

	/*//JM */
	private float accuracy;
	private boolean is_gps_enabled=false;
	/////////////
	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	/*//JM */
	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
	
	public boolean isGPSEnabled() {
		return is_gps_enabled;
	}

	public void setGPSEnabled(boolean is_gps_enabled) {
		this.is_gps_enabled = is_gps_enabled;
	}
	
//	//////////////////////////
	
	@Override
	public String toQueryString() {
		StringBuffer sb = new StringBuffer();

		int gps_enabled = (isGPSEnabled() ? 1 : 0);
		String fecha = Uri.encode(""+getTime());
		
		sb.append("&gps_longitude=").append(getLongitude()) //
				.append("&gps_latitude=").append(getLatitude()) //
				.append("&gps_altitude=").append(getAltitude()) //
				.append("&gps_heading=").append(getHeading()) //
				.append("&gps_speed=").append(getSpeed())
				/*//JM */
				.append("&statusgps=").append(""+gps_enabled)
				.append("&accuracy=").append(getAccuracy())
				.append("&gps_time=").append(fecha);
				///////////

		return sb.toString();
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
