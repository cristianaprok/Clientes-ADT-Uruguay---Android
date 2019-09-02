package ar.com.localizart.android.report.vo;

import java.io.Serializable;

/**
 * Defines the behavior of a value object which can render itself as a URL query
 * string.
 * 
 * @author diego
 * 
 */
public interface URLSerializable extends Serializable {

	/**
	 * Returns the value object parameters as a query string (for example
	 * "&field1=value1&field2=value2").
	 * 
	 * @return
	 */
	public String toQueryString();

}
