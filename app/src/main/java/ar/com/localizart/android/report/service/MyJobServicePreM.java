package ar.com.localizart.android.report.service;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

import java.util.List;

import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.enums.ConfigurationConstants;
import ar.com.localizart.android.report.enums.Constants;
import ar.com.localizart.android.report.info.AntennaDataHandler;
import ar.com.localizart.android.report.info.DataSender;
import ar.com.localizart.android.report.info.LocationDataHandler;
import ar.com.localizart.android.report.info.WifiDataHandler;
import ar.com.localizart.android.report.listeners.ScanWifiListener;
import ar.com.localizart.android.report.power.WakeLock;
import ar.com.localizart.android.report.receivers.BatteryReceiver;
import ar.com.localizart.android.report.util.Util;
import ar.com.localizart.android.report.vo.AntennaVO;
import ar.com.localizart.android.report.vo.BatteryVO;
import ar.com.localizart.android.report.vo.GPSVO;
import ar.com.localizart.android.report.vo.WifiVO;


public class MyJobServicePreM extends JobService {
    public static final String CHANNEL_ONE_ID = "ar.com.localizart.android.report";
    public static final String CHANNEL_ONE_NAME = "ADT FindU";

    //Set the channel’s user-visible name//
    //Set the channel’s ID//
    private static final String TAG = MyJobServicePreM.class.getSimpleName();
    public static boolean onPanic = false;
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
    private static BatteryReceiver batteryReceiver = new BatteryReceiver();
    private static int ticket = 0;
    private NotificationManager notificationManager;

    public static boolean isOnPanic() {
        return onPanic;
    }

    public static void setOnPanic(boolean onPanic) {
        InformationService.onPanic = onPanic;
    }


    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        // Do some work here
        reportLocationImmediately();
        return true; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "MyJobService Created");
        configuration = new Configuration(this);
        // Make the CPU always be ON.
        WakeLock.acquireWakeLock(this);
        // Register the battery receiver:
        registerReceiver(batteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        // Create data handlers:
        antennaDataHandler = new AntennaDataHandler(this);
        antennaDataHandler.init();
        locationDataHandler = new LocationDataHandler(this);
        locationDataHandler.setConfiguration(configuration);
        locationDataHandler.init();
        /* //JM */
        wifiDataHandler = new WifiDataHandler(this);
        wifiDataHandler.init();
        super.onCreate();

    }


    private void reportLocationImmediately() {
        {

            String packageType = configuration
                    .getPackageType(ConfigurationConstants.DEFAULT_PACKAGE_TYPE
                            .getString(this));
            final List<String> reportingURLs = Util.getReportingURLs(Constants.STATE_NORMAL);

            final DataSender dataSender = new DataSender(this);
            dataSender.setConfiguration(configuration);

            final AntennaVO antennaVO = (AntennaVO) antennaDataHandler
                    .handle();
            final BatteryVO batteryVO = batteryReceiver.getBatteryVO();
            GPSVO gpsVO = null;

            // Check configuration to see if we have to send the full info
            // (antenna + battery + GPS).
            if (ConfigurationConstants.PackageType.fromString(packageType, this) == ConfigurationConstants.PackageType.LONG) {
                gpsVO = (GPSVO) locationDataHandler.handle();
            }

            final GPSVO gpsVO2 = gpsVO; // tiene que ser final , hacemos
            // copia

            if (wifiDataHandler.isWifi()) {
                WifiVO wifiVO = (WifiVO) wifiDataHandler.handle();

                // Send the data to the server.
                dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO,
                        Constants.STATE_NORMAL, 0, InformationService.getFormattedTicket());
            } else {
                boolean start = wifiDataHandler
                        .startScan(new ScanWifiListener() {
                            @Override
                            public void scanOk(WifiVO wifiVO) {
                                dataSender.send(reportingURLs, antennaVO, batteryVO,
                                        gpsVO2, wifiVO, Constants.STATE_NORMAL, 0,
                                        InformationService
                                                .getFormattedTicket());
                            }
                        });
                if (!start) {
                    dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO,
                            new WifiVO(), Constants.STATE_NORMAL, 0,
                            InformationService.getFormattedTicket());
                }
            }
        }
    }


}
