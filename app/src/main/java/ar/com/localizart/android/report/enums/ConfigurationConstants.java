package ar.com.localizart.android.report.enums;

import android.content.Context;
import ar.com.localizart.android.report.R;

/**
 * Constants enumeration to access configuration settings..
 * 
 * @author diego
 * 
 */
public enum ConfigurationConstants {
	// Report frequency in seconds.O
	FREQUENCY(R.string.return_param_frequency), //
	// Type of package, antenna or gps.
	PACKAGE_TYPE(R.string.return_param_package_type), //
	// URL of the server where to send reporting information to.
	URL(R.string.return_param_url), //
	//
	// short=send imei, lac, cellid
	// long=send the same as short but with GPS information too.
	SHORT_PACKAGE(R.string.value_short_package_type), //
	LONG_PACKAGE(R.string.value_long_package_type), //
	//
	// Defaults to use when there's no data in configuration (usually the first
	// time we install the application).
	DEFAULT_FREQUENCY(R.string.default_frequency), //
	DEFAULT_PACKAGE_TYPE(R.string.default_package_type), //
	//
	// Whether or not to use the last known location for GPS.
	USE_LAST_KNOWN_LOCATION(R.string.use_last_known_location), //

	
	//JM datos user
	USER_EMAIL(R.string.user_email),
	USER_PASSWORD(R.string.user_password),
	USER_NOMBRE(R.string.user_nombre),
	USER_IMEI(R.string.user_imei),
	USER_CODE(R.string.user_code),
	USER_MCC(R.string.user_mcc),
	USER_MNC(R.string.user_mnc),
	USER_LANGUAGE(R.string.user_language);
	
	
	private int resourceId;

	private ConfigurationConstants(int resourceId) {
		this.resourceId = resourceId;
	}

	public String getString(Context context) {
		return context.getString(resourceId);
	}

	/**
	 * Describes package type, either short (send only antenna info) or long
	 * (send also GPS localization).
	 * 
	 * @author diego
	 * 
	 */
	public static enum PackageType {
		SHORT(SHORT_PACKAGE), //
		LONG(LONG_PACKAGE);

		private ConfigurationConstants name;

		private PackageType(ConfigurationConstants name) {
			this.name = name;
		}

		public static PackageType fromString(String packageType, Context context) {
			PackageType result = SHORT;

			for (PackageType type : PackageType.values()) {
				if (type.name.getString(context).equalsIgnoreCase(packageType)) {
					result = type;
				}
			}

			return result;
		}
	}

}
