package ar.com.localizart.android.report.vo;

import java.io.Serializable;

import ar.com.localizart.android.report.enums.Constants;

/**
 * Report URL value object.
 * 
 * @author diego
 * 
 */
public class URLVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Base URL prefix.
	 */
	private String prefix;

	/**
	 * The full URL.
	 */
	private String url;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Report only URLs sends data to the URL,but doesn't parse the response nor
	 * change the state.
	 * 
	 * @return
	 */
	public boolean isReportOnly() {
		return this.prefix != Constants.MAIN_REPORT_URL;
	}

}
