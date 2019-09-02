package ar.com.localizart.android.report.vo;

/**
 * Antenna information value object (Cell id, LAC, IMEI).
 * 
 * @author diego
 * 
 */
public class AntennaVO implements URLSerializable {
	private static final long serialVersionUID = 1L;

	private int cellId;
	private int lac;
	private String imei;
	private String mcc;
	private String mnc;

	/* //JM */
	private int signal = 0;

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public int getLac() {
		return lac;
	}

	public void setLac(int lac) {
		this.lac = lac;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}

	public int getSignal() {
		return signal;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getMnc() {
		return mnc;
	}

	public void setMnc(String mnc) {
		this.mnc = mnc;
	}

	@Override
	public String toQueryString() {
		StringBuffer sb = new StringBuffer();

		sb.append("imei=").append(imei) //
				.append("&cellid=").append(cellId) //
				.append("&lac=").append(lac) //
				.append("&mcc=").append(mcc) //
				.append("&mnc=").append(mnc) //
				/* //JM */
				.append("&signal=").append(getSignal());

		return sb.toString();
	}

}
