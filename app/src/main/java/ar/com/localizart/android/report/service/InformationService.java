package ar.com.localizart.android.report.service;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.config.Configuration.OnConfigurationChangeListener;
import ar.com.localizart.android.report.enums.ConfigurationConstants;
import ar.com.localizart.android.report.enums.ConfigurationConstants.PackageType;
import ar.com.localizart.android.report.enums.Constants;
import ar.com.localizart.android.report.info.AntennaDataHandler;
import ar.com.localizart.android.report.info.DataSender;
import ar.com.localizart.android.report.info.LocationDataHandler;
import ar.com.localizart.android.report.info.WifiDataHandler;
import ar.com.localizart.android.report.listeners.ScanWifiListener;
import ar.com.localizart.android.report.power.WakeLock;
import ar.com.localizart.android.report.receivers.BatteryReceiver;
import ar.com.localizart.android.report.ui.InformationActivity;
import ar.com.localizart.android.report.util.ADTApplication;
import ar.com.localizart.android.report.util.Util;
import ar.com.localizart.android.report.vo.AntennaVO;
import ar.com.localizart.android.report.vo.BatteryVO;
import ar.com.localizart.android.report.vo.GPSVO;
import ar.com.localizart.android.report.vo.WifiVO;

/**
 * This is the main service of this application, it takes care of the setup of
 * the classes, helpers and receivers needed to obtain and report antenna,
 * battery and GPS data.
 * 
 * @author diego
 * 
 */
public class InformationService extends Service {

	private static final String TAG = InformationService.class.getSimpleName();

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

	/**
	 * Alarm to run this service at a scheduled time.
	 */
	private static AlarmReceiver alarmReceiver = new AlarmReceiver();

	/**
	 * Broadcast receiver for power button information.
	 */
	private ScreenReceiver screenReceiver;
	/**
	 * Broadcast receiver for wearable.
	 */
	private WearablePanicReceiver wearableReceiver;

	/**
	 * numero de SMS to send. //JM
	 */
	// private static final String NUM_SMS="5491133373340";
	private static final String NUM_SMS = "5491133450186";

	/**
	 * EVENTS PARA PANIC, BOOT Y SHUTDOWN. //JM
	 */
	private static final int EVENT_BOOT = 2;
	private static final int EVENT_SHUTDOWN = 3;

	/**
	 * Broadcast receiver for SHUTDOWN. //JM
	 */
	private static ShutdownReceiver shutdownReceiver = new ShutdownReceiver();

	private static int ticket = 0;

	public static boolean onPanic = false;

	private NotificationManager notificationManager;

	public static boolean isOnPanic() {
		return onPanic;
	}

	public static void setOnPanic(boolean onPanic) {
		InformationService.onPanic = onPanic;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null; // no binding allowed
	}

	public InformationService getService() {
		return InformationService.this;
	}

	/**
	 * Alarm implementation, this code is called at a fixed time by the Android
	 * alarm code.
	 * 
	 * @author diego
	 * 
	 */
	public static class AlarmReceiver extends BroadcastReceiver {

		/**
		 * Context.
		 */
		private Context context;

		/**
		 * Intent for the alarm.
		 */
		private PendingIntent pendingIntent;

		/**
		 * The alarm manager.
		 */
		private AlarmManager alarmManager;

		/**
		 * Listener for changes in configuration (to reschedule the alarm when
		 * the frequency changes).
		 */
		private OnConfigurationChangeListener configurationChangeListener;

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,
					"-------------- ALARM RECEIVER ------------- ON RECEIVE: "
							+ context);

			if (context != null && configuration != null && !isOnPanic()) {
				Log.d(TAG,"context,configuration,isOnPanic Verified.");
				this.context = context;
				String packageType = configuration
						.getPackageType(ConfigurationConstants.DEFAULT_PACKAGE_TYPE
								.getString(context));
				final List<String> reportingURLs = Util.getReportingURLs(Constants.STATE_NORMAL);

				final DataSender dataSender = new DataSender(context);
				dataSender.setConfiguration(configuration);

				final AntennaVO antennaVO = (AntennaVO) antennaDataHandler
						.handle();
				final BatteryVO batteryVO = batteryReceiver.getBatteryVO();
				GPSVO gpsVO = null;

				// Check configuration to see if we have to send the full info
				// (antenna + battery + GPS).
				if (PackageType.fromString(packageType, context) == PackageType.LONG) {
					gpsVO = (GPSVO) locationDataHandler.handle();
				}

				final GPSVO gpsVO2 = gpsVO; // tiene que ser final , hacemos
											// copia

				if (wifiDataHandler.isWifi()) {
					Log.d(TAG,"wifiDataHandler.isWifi()" + wifiDataHandler.isWifi());
					WifiVO wifiVO = (WifiVO) wifiDataHandler.handle();

					// Send the data to the server.
					dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO,
							Constants.STATE_NORMAL, 0, InformationService.getFormattedTicket());
				} else {
					boolean start = wifiDataHandler
							.startScan(new ScanWifiListener() {
								@Override
								public void scanOk(WifiVO wifiVO) {
									Log.d(TAG,
											"///////////////ALARM SCAN OK///////////////////////");
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

			}else{
				Log.d(TAG,"Verify Failed :context"+ context +"\nconfiguration " + configuration +"\nisOnPanic" + isOnPanic());
			}
		}

		/**
		 * Return the interval (in milliseconds) to use to repeat the alarm.
		 * 
		 * @return
		 */
		private long getAlarmInterval() {
			String defaultFrequency = ConfigurationConstants.DEFAULT_FREQUENCY
					.getString(context);
			String frequency = configuration.getFrequency(defaultFrequency);

			System.out.println("------------- GET ALARM INTERVAL ---------> "
					+ frequency);
			long interval = 0;

			try {
				interval = Long.parseLong(frequency);
			} catch (Throwable t) {
				Log.e(TAG, "Error parsing frequency, using default.", t);

				try {
					interval = Long.parseLong(defaultFrequency);
				} catch (Throwable t2) {
					Log.e(TAG,
							"Error parsing default frequency, using default.",
							t2);
					// Set a default if the default in configuration also has
					// problems:
					interval = 180;
				}
			}

			// The interval never must be below 30 seconds:
			if (interval < 30) {
				interval = 30;
			}

			return interval * 1000;
		}

		/**
		 * Schedule the alarm.
		 * 
//		 * @param context
		 */
		public void scheduleAlarm() {
			alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, AlarmReceiver.class);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			// Schedule the alarm:
			rescheduleAlarm();

			// Add a listener to schedule the alarm if the frequency was
			// modified.
			configurationChangeListener = new OnConfigurationChangeListener() {
				@Override
				public void onConfigurationChanged(Configuration configuration,
						String key) {
					if (key.equals(ConfigurationConstants.FREQUENCY.toString())) {
						rescheduleAlarm();
					}
				}
			};

			// Register the listener.
			configuration
					.registerOnConfigurationChangeListener(configurationChangeListener);

		}

		/**
		 * Schedule the alarm again, if the frequency was modified.
		 */
		public void rescheduleAlarm() {
			long interval = getAlarmInterval();

			Date oldDate = new Date();
			Calendar gcal = new GregorianCalendar();
			gcal.setTime(oldDate);
			gcal.add(Calendar.MILLISECOND, (int) interval);
			Date newDate = gcal.getTime();
			Log.d(TAG,"NEXT_TIME_SUBMIT" + newDate);
			// Cancel existing alarms:
			alarmManager.cancel(pendingIntent);
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
				// Marshmallow+
				alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, pendingIntent);
			} else {
				//below Marshmallow
				alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, pendingIntent);
//				alarmManager.setInexactRepeating(
//						AlarmManager.ELAPSED_REALTIME_WAKEUP,
//						SystemClock.elapsedRealtime() + interval, interval,
//						pendingIntent);
			}

			// Set the new alarm:

		}

		/**
		 * Setter for context.
		 * 
		 * @param context
		 */
		public void setContext(Context context) {
			this.context = context;
		}

		/**
		 * Class pre-finalization.
		 */
		public void destroy() {
			Answers.getInstance().logCustom(new CustomEvent("Service")
					.putCustomAttribute("event", "destroy")
					.putCustomAttribute("device_token", configuration.getPushToken()));
			if (configuration != null && configurationChangeListener != null) {
				configuration
						.unRegisterOnConfigurationChangeListener(configurationChangeListener);
			}
		}

	}

	public class ScreenReceiver extends BroadcastReceiver {

		protected PowerClickEvent powerClickEvent;

		public ScreenReceiver() {
			resetEvent();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(">>>>>>>", "ScreenReceiver onReceive");
			String action = intent.getAction();
			Log.v(">>>>>>>", "ScreenReceiver ACTION = " + action);
//			Answers.getInstance().logCustom(new CustomEvent("ScreenReceiver")
//					.putCustomAttribute("event", action)
//					.putCustomAttribute("device_token", configuration.getPushToken()));
			if (!isCallActive(context)&& (action.equals(ACTION_SCREEN_OFF) || action.equals(ACTION_SCREEN_ON))) {
				powerClickEvent.registerClick(System.currentTimeMillis());

				if (powerClickEvent.isActivated()) {
					Log.v("*****", "alerts activated");
					sendPanic(context);
					resetEvent();
				}
			}else{
				Log.v(">>>>>>>", "Power Action Not Valid");
			}
		}

		/**
		 * Is the screen of the device on.
		 * 
		 * @param context
		 *            the context
		 * @return true when (at least one) screen is on
		 */
		private boolean isScreenOn(Context context) {
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			Log.v("*****", "is screen on?: " + pm.isScreenOn());
			if (Build.VERSION.SDK_INT >= 20) {
				return pm.isInteractive();
			}else{
				return pm.isScreenOn();
			}
		}

		protected void sendPanic(Context context) {
			Log.v(">>>>>>>", "sendPanic");
			Answers.getInstance().logCustom(new CustomEvent("sendPanic")
					.putCustomAttribute("event", "panic")
					.putCustomAttribute("device_token", configuration.getPushToken()));

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

				if (PackageType.fromString(packageType, context) == PackageType.LONG) {
					gpsVO = (GPSVO) locationDataHandler.handle();
				}

				/* // JM WIFI DATA */
				WifiVO wifiVO = (WifiVO) wifiDataHandler.handle();

				// Send the data to the server.
				dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO, Constants.STATE_PANIC, 0,
						InformationService.getFormattedTicket());
				Vibrator mVibrator = (Vibrator) context
						.getSystemService(Context.VIBRATOR_SERVICE);
				long[] pattern = { 0, 500, 100, 1500, 100, 500, 100, 1500, 100,
						3000 };
				mVibrator.vibrate(pattern, -1);

				setOnPanic(true);

				String params = dataSender.buildSMS("", antennaVO, batteryVO,
						gpsVO, wifiVO, 1, 0,
						InformationService.getFormattedTicket());
				System.out
						.println("////////////////// SEND SMS PARAMS /////////> "
								+ params);

//				if (antennaVO.getMcc().equals("722")) {
//					sendSMS(context, NUM_SMS, params);
//				}
			}
		}

		protected void resetEvent() {
			powerClickEvent = new PowerClickEvent();
		}

		private boolean isCallActive(Context context) {
			AudioManager manager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			return manager.getMode() == AudioManager.MODE_IN_CALL;
		}

		ExecutorService getExecutorService() {
			return Executors.newSingleThreadExecutor();
		}
	}

	public class WearablePanicReceiver extends BroadcastReceiver {
		protected PowerClickEvent powerClickEvent;
		public WearablePanicReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(">>>>>>>", "WearablePanicReceiver onReceive");
		if (!isCallActive(context))
				sendPanic(context);
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

			if (PackageType.fromString(packageType, context) == PackageType.LONG) {
				gpsVO = (GPSVO) locationDataHandler.handle();
			}

				/* // JM WIFI DATA */
			WifiVO wifiVO = (WifiVO) wifiDataHandler.handle();

			// Send the data to the server.
			dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO, Constants.STATE_PANIC, 0,
					InformationService.getFormattedTicket());
			Vibrator mVibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 0, 500, 100, 1500, 100, 500, 100, 1500, 100,
					3000 };
			mVibrator.vibrate(pattern, -1);

			setOnPanic(true);

			String params = dataSender.buildSMS("", antennaVO, batteryVO,
					gpsVO, wifiVO, 1, 0,
					InformationService.getFormattedTicket());
			System.out
					.println("////////////////// SEND SMS PARAMS /////////> "
							+ params);

			if (antennaVO.getMcc().equals("722")) {
				sendSMS(context, NUM_SMS, params);
			}
		}
	}



	private boolean isCallActive(Context context) {
		AudioManager manager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		return manager.getMode() == AudioManager.MODE_IN_CALL;
	}

	ExecutorService getExecutorService() {
		return Executors.newSingleThreadExecutor();
	}
}


	private static void sendSMS(Context context, String phoneNumber,
			String message) {
//		Log.v("phoneNumber", phoneNumber);
//		Log.v("MEssage", message);
//		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(
//				context, InformationService.class), 0);
//		SmsManager sms = SmsManager.getDefault();
//		sms.sendTextMessage(phoneNumber, null, message, pi, null);

		// SmsManager sms = SmsManager.getDefault();
		// ArrayList<String> parts = sms.divideMessage(message);
		// sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
	}

	public static class ShutdownReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "--------------SHUTDOWN -------------");
			Log.d(TAG, "--------------SHUTDOWN -------------");
			Log.d(TAG, "--------------SHUTDOWN -------------");

			onShutDown_Boot(context, EVENT_SHUTDOWN);
		}

	}

	private static void onShutDown_Boot(Context context, final int event) {
		if (context != null && configuration != null) {
			String packageType = configuration
					.getPackageType(ConfigurationConstants.DEFAULT_PACKAGE_TYPE
							.getString(context));

			final List<String> reportingURLs = Util.getReportingURLs(Constants.STATE_NORMAL);

			DataSender dataSender = new DataSender(context);
			dataSender.setConfiguration(configuration);

			final AntennaVO antennaVO = (AntennaVO) antennaDataHandler.handle();
			final BatteryVO batteryVO = batteryReceiver.getBatteryVO();
			GPSVO gpsVO = null;

			// Check configuration to see if we have to send the full info
			// (antenna + battery + GPS).
			if (PackageType.fromString(packageType, context) == PackageType.LONG) {
				gpsVO = (GPSVO) locationDataHandler.handle();
			}

			WifiVO wifiVO = (WifiVO) wifiDataHandler.handle();

			// Send the data to the server.
			dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO, Constants.STATE_NORMAL, event,
					InformationService.getFormattedTicket());
		}

	}

	// ///////////////////////////////////////////////////////////////////

	@SuppressLint("DefaultLocale")
	public static String getFormattedTicket() {
		return String.format("%04d", InformationService.ticket);
	}

	public static void increaseTicket() {
		InformationService.ticket++;

		if (InformationService.ticket > 9999) {
			InformationService.ticket = 0;
		}
	}

	private void setupNotification() {
		// Muestra la notificacion del servicio esta corriendo.
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Set the icon, scrolling text and timestamp.
//		Notification notification = new Notification(
//				R.drawable.background_notification_icon, null,
//				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		/*
		 * PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new
		 * Intent(this, InformationActivity.class), 0);
		 */
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);


		// Set the info for the views that show in the notification panel.
//		notification.setLatestEventInfo(this,
//				getText(R.string.status_bar_notifications_running_title),
//				getText(R.string.status_bar_notifications_running_text),
//				contentIntent);

		// Send the notification.
//		notificationManager.notify(1, notification);


		//////anshad
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.background_notification_icon)
						.setContentTitle(getText(R.string.status_bar_notifications_running_title))
						.setContentText(getText(R.string.status_bar_notifications_running_text));
		mBuilder.setOngoing(true);
		Notification notification=mBuilder.build();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;

		mBuilder.setContentIntent(contentIntent);
		// Sets an ID for the notification
		int mNotificationId = 001;
// Builds the notification and issues it.
		notificationManager.notify(mNotificationId, notification);
	}

	private void buildNotification(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String NOTIFICATION_CHANNEL_ID = "com.adt.findu";
			String channelName = "ADT";
			NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
			chan.setLightColor(Color.BLUE);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);
			Intent notificationIntent = new Intent(this, InformationActivity.class);
//			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
			Notification notification = notificationBuilder.setOngoing(true)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(getString(R.string.tracking_message))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.build();
			startForeground(1224, notification);
		}else{
			Intent notificationIntent = new Intent(this, InformationActivity.class);
//			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			Notification notification = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(getString(R.string.app_name))
					.setContentText(getString(R.string.tracking_message))
					.setChannelId(getString(R.string.app_name))
					.setContentIntent(pendingIntent).build();


			startForeground(1224, notification);
		}



	}

	/**
	 * Service onCreate() implementation.
	 */
	@Override
	public void onCreate() {

		if (Build.VERSION.SDK_INT >= 26) {
			buildNotification();
		} else {

		}

		Log.v(TAG, "Service onCreate()");

		// Load configuration:
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

		// Configure and start alarm:
		alarmReceiver.setContext(this);
		alarmReceiver.scheduleAlarm();

		// REGISTER ScreenReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		screenReceiver = new ScreenReceiver();
		registerReceiver(screenReceiver, filter);

		// REGISTER WearableReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_WEARABLE_PANIC_ALERT);
		wearableReceiver = new WearablePanicReceiver();
		registerReceiver(wearableReceiver, intentFilter);


		IntentFilter filter2 = new IntentFilter(Intent.ACTION_SHUTDOWN);
		filter2.addAction("android.intent.action.QUICKBOOT_POWEROFF");
		registerReceiver(shutdownReceiver, filter2);

		// JM RESET DEL SERVICIO LLAMAMOS A NEW BOOT
		onShutDown_Boot(this, EVENT_BOOT);
		// ////////////////////////

		setupNotification();

		// Call super:
		super.onCreate();
	}

	/**
	 * Service onDestroy() implementation.
	 */
	@Override
	public void onDestroy() {
		Log.v(TAG, "Service onDestroy()");

		// Unregister listeners and receivers:
		unregisterReceiver(batteryReceiver);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		unregisterReceiver(screenReceiver);
		unregisterReceiver(wearableReceiver);
		unregisterReceiver(shutdownReceiver); // JM

		// Finalize data handlers:
		if (antennaDataHandler != null) {
			antennaDataHandler.destroy();
		}

		if (locationDataHandler != null) {
			locationDataHandler.destroy();
		}

		if (alarmReceiver != null) {
			alarmReceiver.destroy();
		}

		// Turn off wake lock:
		WakeLock.releaseWakeLock(this);

		// Call super:
		super.onDestroy();
	}

}
