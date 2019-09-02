package ar.com.localizart.android.report.enums;

import android.os.BatteryManager;

/**
 * Describes battery status.
 * 
 * @author diego
 * 
 */
public enum BatteryStatus {
	CHARGING(BatteryManager.BATTERY_STATUS_CHARGING, "Charging"), //
	DISCHARGING(BatteryManager.BATTERY_STATUS_DISCHARGING, "Discharging"), //
	FULL(BatteryManager.BATTERY_STATUS_FULL, "Full"), //
	NOT_CHARGING(BatteryManager.BATTERY_STATUS_NOT_CHARGING, "Not-Charging"), //
	UNKNOWN(BatteryManager.BATTERY_STATUS_UNKNOWN, "Unknown");

	private int batteryStatus;
	private String description;

	/**
	 * Enum constructor.
	 * 
	 * @param batteryStatus
	 * @param description
	 */
	private BatteryStatus(int batteryStatus, String description) {
		this.batteryStatus = batteryStatus;
		this.description = description;
	}

	/**
	 * Battery status getter.
	 * 
	 * @return
	 */
	public int getBatteryStatus() {
		return batteryStatus;
	}

	@Override
	public String toString() {
		return description;
	}

	/**
	 * Creates a <code>BatteryStatus</code> enum based on the battery status int
	 * read from the Android API.
	 * 
	 * @param batteryStatus
	 * @return
	 */
	public static BatteryStatus fromStatus(int batteryStatus) {
		BatteryStatus result = UNKNOWN;
		for (BatteryStatus status : BatteryStatus.values()) {
			if (status.getBatteryStatus() == batteryStatus) {
				result = status;
			}
		}

		return result;
	}
}
