package ar.com.localizart.android.report.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import ar.com.localizart.android.report.enums.ConfigurationConstants;

/**
 * Configuration object.
 * 
 * It allows us to save and retrieve configuration from both Shared Preferences
 * and Internal Storage (we can add new storage options too), for devices in
 * which preferences are lost.
 * 
 * @author diego
 * 
 */
public class Configuration {

	private static final String TAG = Configuration.class.getSimpleName();

	private Context context;

	private SharedPreferences preferences;

	private Set<OnConfigurationChangeListener> listeners;

	private static final String INTERNAL_STORAGE_FILE_PREFIX = "localizart_config";

	public Configuration(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		listeners = new HashSet<OnConfigurationChangeListener>();
	}

	public interface OnConfigurationChangeListener {
		void onConfigurationChanged(Configuration configuration, String key);
	}

	public void registerOnConfigurationChangeListener(
			OnConfigurationChangeListener listener) {
		listeners.add(listener);
	}

	public void unRegisterOnConfigurationChangeListener(
			OnConfigurationChangeListener listener) {
		listeners.remove(listener);
	}

	private String getStringFromInternalStorage(String key) {
		try {
			String filename = INTERNAL_STORAGE_FILE_PREFIX + "_" + key;
			BufferedReader br = new BufferedReader(new InputStreamReader(
					this.context.openFileInput(filename)));

			return br.readLine();
		} catch (FileNotFoundException fne) {
			// Don't print anything on stderr if the file is not found (although
			// this is only see during development).
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setStringToInternalStorage(String key, String value) {
		try {
			String filename = INTERNAL_STORAGE_FILE_PREFIX + "_" + key;
			FileOutputStream fileOutputStream = this.context.openFileOutput(
					filename, Context.MODE_PRIVATE);
			fileOutputStream.write(value.getBytes());
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getStringValue(String key) {
		return getStringValue(key, "");
	}

	public String getStringValue(String key, String defaultValue) {
		String value = preferences.getString(key, defaultValue);
		Log.v(TAG, "Got '" + value + "' for '" + key
				+ "' from shared preferences.");
		if (value == null || value.equals("")) {
			value = getStringFromInternalStorage(key);
			Log.v(TAG, "Got '" + value + "' for '" + key
					+ "' from internal storage.");
		}
		return value;
	}

	public Boolean getBooleanValue(String key, Boolean defaultValue) {
		Boolean value = preferences.getBoolean(key, defaultValue);
		Log.v(TAG, "Got '" + value + "' for '" + key
				+ "' from shared preferences.");
		if (value == null) {
			String stringValue = getStringFromInternalStorage(key);
			Log.v(TAG, "Got '" + value + "' for '" + key
					+ "' from internal storage.");
			value = Boolean.valueOf(stringValue);
		}
		return value;
	}

	public String getUserName() {
		return this.getStringValue(ConfigurationConstants.USER_NOMBRE.toString());
	}

	public String getUserEmail() {
		return this
				.getStringValue(ConfigurationConstants.USER_EMAIL.toString());
	}

	// public String getUserTFNO() {
	// return this.getValue(PreferencesConstants.USER_TFNO.toString());
	// }

	public String getUserIMEI() {
		return this.getStringValue(ConfigurationConstants.USER_IMEI.toString());
	}

	public String getUserMNC() {
		return this.getStringValue(ConfigurationConstants.USER_MNC.toString());
	}

	public String getUserMCC() {
		return this.getStringValue(ConfigurationConstants.USER_MCC.toString());
	}

	public String getUserLanguage() {
		return this.getStringValue(ConfigurationConstants.USER_LANGUAGE
				.toString());
	}

	public String getPackageType(String defaultValue) {
		return this.getStringValue(
				ConfigurationConstants.PACKAGE_TYPE.toString(), defaultValue);
	}

	public String getURL(String defaultValue) {
		return this.getStringValue(ConfigurationConstants.URL.toString(),
				defaultValue);
	}

	public String getUserCode(String defaultValue) {
		return this.getStringValue(ConfigurationConstants.USER_CODE.toString(),
				defaultValue);
	}

	public String getFrequency(String defaultValue) {
		return this.getStringValue(ConfigurationConstants.FREQUENCY.toString(),
				defaultValue);
	}

	public Boolean getUseLastKnownLocation(Boolean defaultValue) {
		return this.getBooleanValue(
				ConfigurationConstants.USE_LAST_KNOWN_LOCATION.toString(),
				defaultValue);
	}
	public String getPushToken() {
		return this.getStringValue("PUSH_TOKEN",
				"NO_TOKEN");
	}

	public void setPushToken(String pushToken) {
		Editor preferencesEditor = preferences.edit();
		preferencesEditor.putString("PUSH_TOKEN", pushToken);
		preferencesEditor.apply();
	}

	/**
	 * Save the values in all available storage options (preferences, internal
	 * storage, etc).
	 * 
	 * @param values
	 */
	public void saveValues(Map<String, String> values) {
		Editor preferencesEditor = preferences.edit();
		for (Map.Entry<String, String> entry : values.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			Log.v(TAG, "Saving '" + key + "' = '" + value + "'");
			preferencesEditor.putString(key, value);
			setStringToInternalStorage(key, value);
			for (OnConfigurationChangeListener listener : listeners) {
				if (listener != null) {
					Log.v(TAG, "Launching listener for " + key);
					listener.onConfigurationChanged(this, key);
				}
			}
		}
		preferencesEditor.commit();
	}
}
