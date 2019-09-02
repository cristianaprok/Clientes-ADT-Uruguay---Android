package ar.com.localizart.android.report.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.com.localizart.android.report.enums.Constants;

public class Util {

	public static List<String> getReportingURLs(boolean panic) {
		List<String> reportingURLs = new ArrayList<String>();

		// Add the main reporting URL:
		reportingURLs.add(Constants.MAIN_REPORT_URL);

		// If we are sending a panic message, add all the additional URLs:
		if (panic) {
			List<String> panicList = Arrays
					.asList(Constants.ADDITIONAL_REPORT_URLS);
			reportingURLs.addAll(panicList);
		}

		return reportingURLs;
	}

}
