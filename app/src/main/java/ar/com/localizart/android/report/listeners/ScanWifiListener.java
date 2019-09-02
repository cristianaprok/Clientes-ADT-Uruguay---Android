package ar.com.localizart.android.report.listeners;

import ar.com.localizart.android.report.vo.WifiVO;

/**
 * Wifi scan results listener.
 * 
 * @author JM
 * 
 */
public interface ScanWifiListener {
	public void scanOk(WifiVO wifiVO);
	
}