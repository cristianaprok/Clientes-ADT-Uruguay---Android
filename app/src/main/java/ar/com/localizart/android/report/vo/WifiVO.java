package ar.com.localizart.android.report.vo;

import android.net.Uri;

/**
 * Wifi information value object (SSID, MAC, isWifi()).
 * 
 * @author JM
 * 
 */
public class WifiVO implements URLSerializable {
	private static final long serialVersionUID = 1L;

	private String SSID ="";
	private String MAC="";
	private String BSSID ="";

	private boolean wifiEnabled = false;
	

	public String getSSID() {
		return SSID;
	}

	public void setSSID(String sSID) {
		SSID = sSID;
	}

	public String getMAC() {
		return MAC;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public boolean isWifiEnabled(){
		return wifiEnabled;
	}

	public void setWifiEnabled(boolean enabled){
		wifiEnabled=enabled;
	}
	
	@Override
	public String toQueryString() {
		StringBuffer sb = new StringBuffer();

		//codifica espacios y caracteres raros
		String wifiname = Uri.encode(""+getSSID());
		String appencoded = Uri.encode(wifiname);
		if(isWifiEnabled()){
			
			sb.append("&statuswifi=1")
			.append("&wifinetname="+appencoded)
			.append("&wifinetmac="+getBSSID());
		}
		else{
			
			sb.append("&statuswifi=0");
			if(!getSSID().equals(""))
				sb.append("&wifinetname="+appencoded);
			if(!getBSSID().equals(""))
				sb.append("&wifinetmac="+getBSSID());
		}
		
		return sb.toString();
	}

	public String getBSSID() {
		return BSSID;
	}

	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}

}
