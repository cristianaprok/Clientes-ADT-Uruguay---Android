package ar.com.localizart.android.report.info;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import ar.com.localizart.android.report.listeners.CellListener;
import ar.com.localizart.android.report.vo.AntennaVO;
import ar.com.localizart.android.report.vo.URLSerializable;

/**
 * Handler for antenna data.
 *
 * @author diego
 *
 */
public class AntennaDataHandler implements DataHandler {

	/**
	 * Telephony manager.
	 */
	private TelephonyManager telephonyManager;

	/**
	 * Cell data state change listener.
	 */
	private CellListener cellListener;

	/**
	 * Context data.
	 */
	private Context context;

	/**
	 * Constructor.
	 *
	 * @param context
	 */
	public AntennaDataHandler(Context context) {
		this.context = context;
	}

	/**
	 * Data handler initialization.
	 */
	@Override
	public void init() {
		if (ContextCompat.checkSelfPermission(this.context,Manifest.permission.READ_PHONE_STATE)==
				PackageManager.PERMISSION_GRANTED){
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);}
		else return;
		if (telephonyManager != null) {
			cellListener = new CellListener(/*//JM */ telephonyManager);
			if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				// Permission is not granted
				telephonyManager.listen(cellListener,
						PhoneStateListener.LISTEN_CELL_LOCATION
								| PhoneStateListener.LISTEN_SERVICE_STATE /*//JM */ | PhoneStateListener.LISTEN_SIGNAL_STRENGTH/*PhoneStateListener.LISTEN_SIGNAL_STRENGTHS api 7*/);
				CellLocation.requestLocationUpdate();
			}

		}
	}

	/**
	 * Obtain the data and return it.
	 *
	 * @return
	 */
	@Override
	public URLSerializable handle() {
		if (telephonyManager != null) {
			return getAntennaInfo();
		}

		return null;
	}

	/**
	 * Execute pre-finalization code.
	 */
	@Override
	public void destroy() {
		if (telephonyManager != null) {
			telephonyManager.listen(cellListener,
					PhoneStateListener.LISTEN_NONE);
		}
	}

	/**
	 * Get antenna info from the GSM cell listener and telephony manager.
	 *
	 * @return
	 */
	private AntennaVO getAntennaInfo() {
		AntennaVO antennaVO = new AntennaVO();


		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.

			antennaVO.setImei(telephonyManager.getDeviceId());
//			return null;
		}


		// Set MCC/MNC:
		String networkOperator = telephonyManager.getNetworkOperator();

		if (networkOperator != null && !networkOperator.equals("")) {
			antennaVO.setMcc(networkOperator.substring(0,3));
			antennaVO.setMnc(networkOperator.substring(3));
		}

		GsmCellLocation gsmCellLocation = cellListener.getGsmCellLocation();

		if (gsmCellLocation != null) {
			antennaVO.setCellId(gsmCellLocation.getCid());
			antennaVO.setLac(gsmCellLocation.getLac());
		} else {
			// No location, use -1 to flag that there's no data.
			antennaVO.setCellId(-1);
			antennaVO.setLac(-1);
		}


		/*//JM */
		antennaVO.setSignal(cellListener.getSignal());

		return antennaVO;
	}
}
