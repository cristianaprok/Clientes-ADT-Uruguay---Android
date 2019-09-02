package ar.com.localizart.android.report.vo;

import ar.com.localizart.android.report.enums.BatteryStatus;

/**
 * Battery information value object.
 * 
 * @author diego
 * 
 */
public class BatteryVO implements URLSerializable {
	private static final long serialVersionUID = 1L;

	private BatteryStatus status = BatteryStatus.UNKNOWN;
	private int level = -1;
	private int scale = -1;

	public BatteryStatus getStatus() {
		return status;
	}

	public void setStatus(BatteryStatus status) {
		this.status = status;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	/**
	 * Return the battery level percentage.
	 * 
	 * @return
	 */
	private float getPercentage() {
		if (scale == 0) {
			// Return an invalid percentage if the scale is zero.
			return -1;
		}
		return level / (float) scale;
	}

	@Override
	public String toQueryString() {
		StringBuffer sb = new StringBuffer();
		sb.append("&battery_remaining=").append(getPercentage()) //
				.append("&battery_status=").append(getStatus().toString());

		return sb.toString();
	}
}
