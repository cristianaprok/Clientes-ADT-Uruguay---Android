package ar.com.localizart.android.report.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.List;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.enums.ConfigurationConstants;
import ar.com.localizart.android.report.enums.Constants;
import ar.com.localizart.android.report.info.AntennaDataHandler;
import ar.com.localizart.android.report.info.DataSender;
import ar.com.localizart.android.report.info.LocationDataHandler;
import ar.com.localizart.android.report.info.WifiDataHandler;
import ar.com.localizart.android.report.receivers.BatteryReceiver;
import ar.com.localizart.android.report.service.InformationService;
import ar.com.localizart.android.report.util.ADTApplication;
import ar.com.localizart.android.report.util.Util;
import ar.com.localizart.android.report.vo.AntennaVO;
import ar.com.localizart.android.report.vo.BatteryVO;
import ar.com.localizart.android.report.vo.GPSVO;
import ar.com.localizart.android.report.vo.WifiVO;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static ar.com.localizart.android.report.service.InformationService.setOnPanic;

public class InformationActivity extends Activity {
	private Button panicButton;
	private PulsatorLayout pulsator;
	private LinearLayout llBeforeLoading,llAfterLoading;
	private TextView tvSOS;
	public final static int REQUEST_CODE = 10101;
	private boolean open;
    private int LOCATION_CODE = 2;
	public Integer MY_IGNORE_OPTIMIZATION_REQUEST = 1;
	private static final String TAG = InformationActivity.class.getSimpleName();

	/**
	 * Configuration.
	 */
	private static Configuration configuration = null;

	/**
	 * Antenna data handling.
	 */
	private static AntennaDataHandler antennaDataHandler = null;

	/**
	 * Location data handling.
	 */
	private static LocationDataHandler locationDataHandler = null;

	/**
	 * Wifi data handling. //JM
	 */
	private static WifiDataHandler wifiDataHandler = null;
	/**
	 * Broadcast receiver for battery information.
	 */
	private static BatteryReceiver batteryReceiver = new BatteryReceiver();



	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Answers.getInstance().logCustom(new CustomEvent("InformationActivity"));
		// Load configuration:
		configuration = new Configuration(this);

		// Create data handlers:

		if (!(checkPermissionStatusForLocation() && checkPermissionStatusForPhoneState())) {
			requestPermission();
		} else{
			antennaDataHandler = new AntennaDataHandler(this);
			antennaDataHandler.init();
		}


		locationDataHandler = new LocationDataHandler(this);
		locationDataHandler.setConfiguration(configuration);
		locationDataHandler.init();

		/* //JM */
		wifiDataHandler = new WifiDataHandler(this);
		wifiDataHandler.init();


		Bundle params = getIntent().getExtras();

		String email = "";
		String imei = "";

		if (params != null) {
			email = params.getString("email");
			imei = params.getString("imei");
		}else{
			email = configuration.getUserEmail();
			imei = configuration.getUserIMEI();
		}

//		checkBGPermission();
		// takeKeyEvents(true);
		Log.v(TAG, "----------------------------------");
		Log.v(TAG, "----------------------------------");
		Log.v(TAG, "----------------------------------");
		Log.v(TAG, "Starting Information service.");

		if (!ADTApplication.getInstance().isServiceRunning()){
			startService(new Intent(this, InformationService.class));
		}

		setContentView(R.layout.layout_comenzarrastreo);

		panicButton = (Button) findViewById(R.id.panic_button);
		WebView webView = (WebView) findViewById(R.id.webViewLastScreen);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		webView.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Toast.makeText(getApplicationContext(),
						R.string.banner_verify_connection, Toast.LENGTH_SHORT)
						.show();
			}
        });
		Log.v(TAG, Constants.FINAL_URL + "email=" + email + "&imei=" + imei);
		webView.loadUrl(Constants.FINAL_URL + "email=" + email + "&imei=" + imei);
		panicButton.setVisibility(View.GONE);
		panicButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendPanic(InformationActivity.this);
			}
		});
	}

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("tag", "onResume");
        if (!(checkPermissionStatusForLocation() && checkPermissionStatusForPhoneState())) {
            requestPermission();
        }

    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
        ) {

        }
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE};
        ActivityCompat.requestPermissions(this, permissions, LOCATION_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission granted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission is denied!", Toast.LENGTH_SHORT).show();
                boolean showRationaleForLocation = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                boolean showRationaleForPhoneState = shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE);
                if (!showRationaleForLocation && !showRationaleForPhoneState) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 101);
                }
            }
        }
    }


    private boolean checkPermissionStatusForLocation() {
        int resultForLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return resultForLocation == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermissionStatusForPhoneState() {
        int resultForLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        return resultForLocation == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    public void checkBGPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Marshmallow+
            Context context = this.getBaseContext();
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//			if (open){
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
//					intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					context.startActivity(intent);
//					checkDrawOverlayPermission();
            } else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, MY_IGNORE_OPTIMIZATION_REQUEST);
            }
//			}
        } else {
            //below Marshmallow
        }
    }

    @Override
//	@TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_IGNORE_OPTIMIZATION_REQUEST) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                boolean isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
                if (isIgnoringBatteryOptimizations) {
                    // Ignoring battery optimization

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        // Marshmallow+
//						checkDrawOverlayPermission();
//						Context context = this.getBaseContext();
//						Intent intent = new Intent();
//						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//						context.startActivity(intent);
                    } else {
                        //below Marshmallow
                    }


                } else {
                    // Not ignoring battery optimization
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void sendPanic(Context context) {
        Log.v(">>>>>>>", "sendPanic");

        if (context != null && configuration != null) {
            String packageType = configuration
                    .getPackageType(ConfigurationConstants.DEFAULT_PACKAGE_TYPE
                            .getString(context));

            final List<String> reportingURLs = Util.getReportingURLs(Constants.STATE_PANIC);

            DataSender dataSender = new DataSender(context);
            dataSender.setConfiguration(configuration);

            AntennaVO antennaVO = (AntennaVO) antennaDataHandler.handle();
            BatteryVO batteryVO = batteryReceiver.getBatteryVO();
            GPSVO gpsVO = null;

            // Check configuration to see if we have to send the full info
            // (antenna + battery + GPS).

            if (ConfigurationConstants.PackageType.fromString(packageType, context) == ConfigurationConstants.PackageType.LONG) {
                gpsVO = (GPSVO) locationDataHandler.handle();
            }

            /* // JM WIFI DATA */
            WifiVO wifiVO = (WifiVO) wifiDataHandler.handle();

            // Send the data to the server.
            dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO, Constants.STATE_PANIC, 0,
                    InformationService.getFormattedTicket());
            Vibrator mVibrator = (Vibrator) context
                    .getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 500, 100, 1500, 100, 500, 100, 1500, 100,
                    3000};
            mVibrator.vibrate(pattern, -1);

            setOnPanic(true);

            String params = dataSender.buildSMS("", antennaVO, batteryVO,
                    gpsVO, wifiVO, 1, 0,
                    InformationService.getFormattedTicket());
            System.out
                    .println("////////////////// SEND SMS PARAMS /////////> "
                            + params);


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
