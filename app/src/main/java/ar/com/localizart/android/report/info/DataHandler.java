package ar.com.localizart.android.report.info;

import ar.com.localizart.android.report.vo.URLSerializable;

/**
 * Describes data handling behavior.
 * 
 * @author diego
 * 
 */
public interface DataHandler {

	/**
	 * Data handler initialization.
	 */
	public void init();

	/**
	 * Handles the retrieval of data and returns it in a value object whose data
	 * can be serializable through an URL.
	 * 
	 * @return
	 */
	public URLSerializable handle();

	/**
	 * Data handler finalization.
	 */
	public void destroy();
}
