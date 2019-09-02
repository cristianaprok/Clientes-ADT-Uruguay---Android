package ar.com.localizart.android.report.info;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import ar.com.localizart.android.report.listeners.ScanWifiListener;
import ar.com.localizart.android.report.vo.URLSerializable;
import ar.com.localizart.android.report.vo.WifiVO;

/**
 * Handler for wifi data.
 * 
 * @author JM
 * 
 */
public class WifiDataHandler implements DataHandler {

	private final String TAG =  WifiDataHandler.class.getSimpleName();
	/**
	 * Wifi manager.
	 */
	private WifiManager wifiManager;

	/**
	 * Context data.
	 */
	private Context context;

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public WifiDataHandler(Context context) {
		this.context = context;
	}

	/**
	 * Data handler initialization.
	 */
	@Override
	public void init() {
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
	}
	
	BroadcastReceiver receiverScan = null;
	
	public boolean startScan(final ScanWifiListener swl){
		Log.d(TAG, "////////////START SCAN ///////////////////////");
		
		if(!wifiManager.isWifiEnabled())
			return false;
		
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

		receiverScan = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent i) {
		
				//context.unregisterReceiver(receiverScan);
				
				
				Log.d(TAG, "//////////////ON RECEIVE /////////////////");
				
				
				List<ScanResult> l = wifiManager.getScanResults();
						
				String ssid ="";
				String bssid="";
				int maxLevel = -99999999;

				if(l != null)
				for (ScanResult r : l) {

					//use r.SSID or r.BSSID to identify your home network and take action
					//SSID: nombre de la wifi
					//BSSID: MAC addres
					Log.d(TAG, "////////SCAN >>>>> "+r.SSID+" >> "+r.BSSID+" >> "+r.level);
					
					if(r.level > maxLevel){
						ssid = r.SSID;//NOMBRE DEL ACCESO WIFI
						bssid = r.BSSID;//MAC ADRESS DEL ROUTER WIFI
						
						
						maxLevel = r.level;
					}
				}
				
				if(swl !=null){
					WifiVO wifiVO = new WifiVO();
					wifiVO.setMAC(bssid);
					wifiVO.setBSSID(bssid);
					wifiVO.setSSID(ssid);
					swl.scanOk(wifiVO);
				}
				try{
					context.unregisterReceiver(this);
				}
				catch(Exception e){
					Log.d(TAG, "////////SCAN ERROR>>>>> "+e);
				}
				
				//wifiManager.setWifiEnabled(false);
				
			}
		};
		
		context.registerReceiver(receiverScan, i);
		//wifiManager.setWifiEnabled(true);
		boolean start = wifiManager.startScan();
		Log.d(TAG, "////////SCAN >>>>> "+start);
		return start;
	}
	

	/**
	 * Obtain the data and return it.
	 * 
	 * @return
	 */
	@Override
	public URLSerializable handle() {
		if (wifiManager != null) {
			return getWifiInfo();
		}

		return null;
	}

	/**
	 * Execute pre-finalization code.
	 */
	@Override
	public void destroy() {
		
	}

	/**
	 * Get antenna info from the GSM cell listener and telephony manager.
	 * 
	 * @return
	 */
	private WifiVO getWifiInfo() {
		WifiVO wifiVO = new WifiVO();

		if(isWifi()){
			wifiVO.setWifiEnabled(true);
			wifiVO.setMAC(wifiManager.getConnectionInfo().getMacAddress());
			wifiVO.setSSID(wifiManager.getConnectionInfo().getSSID());
			wifiVO.setBSSID(wifiManager.getConnectionInfo().getBSSID());
		}
		
		
		
		return wifiVO;
	}
	
	public boolean isWifi() {
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (cm != null) {
				NetworkInfo networkInfo = cm.getActiveNetworkInfo();
				
				if (networkInfo != null && networkInfo.isConnected()) {
					
					/*//JM devolvemos si es WIFI */
					if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
						return true;
					//////////////////
				}
			}
		}
		return false;
	}
}
