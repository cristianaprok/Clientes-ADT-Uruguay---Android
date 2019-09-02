package ar.com.localizart.android.report.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import ar.com.localizart.android.report.util.Util;
import ar.com.localizart.android.report.vo.AntennaVO;
import ar.com.localizart.android.report.vo.BatteryVO;
import ar.com.localizart.android.report.vo.GPSVO;
import ar.com.localizart.android.report.vo.WifiVO;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static ar.com.localizart.android.report.service.InformationService.setOnPanic;

public class AfterConfirmingActivity extends Activity {

    /* Configuration.
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
    public Integer MY_IGNORE_OPTIMIZATION_REQUEST = 1;
    private int LOCATION_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_confirming);

        // Load configuration:
        configuration = new Configuration(this);

        unMuteAudio();

        // Create data handlers:
        if (!(checkPermissionStatusForLocation() && checkPermissionStatusForPhoneState())) {
            requestPermission();
        } else {
            checkBGPermission();
            antennaDataHandler = new AntennaDataHandler(this);
            antennaDataHandler.init();
        }


        locationDataHandler = new LocationDataHandler(this);
        locationDataHandler.setConfiguration(configuration);
        locationDataHandler.init();

        /* //JM */
        wifiDataHandler = new WifiDataHandler(this);
        wifiDataHandler.init();
        startService(new Intent(this, InformationService.class));

        String email = "";
        String imei = "";

        Bundle params = getIntent().getExtras();
        if (params != null) {
            email = params.getString("email");
            imei = params.getString("imei");
        } else {
            email = configuration.getUserEmail();
            imei = configuration.getUserIMEI();
        }

        TextView tvSos = findViewById(R.id.tvSOS);
        tvSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPanic(AfterConfirmingActivity.this);
            }
        });

        PulsatorLayout pulsator = findViewById(R.id.pulsator);
        pulsator.start();

        //TODO added by PS start
        ImageView ivPulsatorArrow = findViewById(R.id.ivPulsatorArrow);
        ImageView civCheckStatus = findViewById(R.id.civCheckStatus);
        //TODO added by PS end


        // onClick Listener
        final String finalEmail = email;
        final String finalImei = imei;
        ivPulsatorArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO commented by PS, Flow not clear here
                Bundle bundle = new Bundle();
                bundle.putString("email", finalEmail);
                bundle.putString("imei", finalImei);

                Log.d("ACTIVITYCOMENZAR", " -------------------------------------------RUNNING");

                Intent myIntent = new Intent(getBaseContext(), InformationActivity.class);
                myIntent.putExtras(bundle);

                startActivity(myIntent);
                finish();
            }
        });

        // TODO added by PS
        civCheckStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("email", finalEmail);
                bundle.putString("imei", finalImei);

                Intent myIntent = new Intent(getBaseContext(), StatusActivity.class);
                myIntent.putExtras(bundle);
                startActivity(myIntent);
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


            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE};
            ActivityCompat.requestPermissions(this, permissions, LOCATION_CODE);
        }
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

    public void checkBGPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Context context = this.getBaseContext();
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {

            } else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, MY_IGNORE_OPTIMIZATION_REQUEST);
            }
        } else {

        }
    }

    private void unMuteAudio() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        try {
            // mute (or) un mute audio based on status
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
        } catch (Exception e) {
            if (audioManager == null) return;

            // un mute the audio if there is an exception
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
        }
    }

}
