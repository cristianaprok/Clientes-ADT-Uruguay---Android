package ar.com.localizart.android.report.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import ar.com.localizart.android.report.R
import ar.com.localizart.android.report.config.Configuration
import ar.com.localizart.android.report.enums.ConfigurationConstants
import ar.com.localizart.android.report.enums.Constants
import ar.com.localizart.android.report.info.AntennaDataHandler
import ar.com.localizart.android.report.info.DataSender
import ar.com.localizart.android.report.info.LocationDataHandler
import ar.com.localizart.android.report.info.WifiDataHandler
import ar.com.localizart.android.report.receivers.BatteryReceiver
import ar.com.localizart.android.report.service.InformationService
import ar.com.localizart.android.report.util.Util
import ar.com.localizart.android.report.vo.AntennaVO
import ar.com.localizart.android.report.vo.GPSVO
import ar.com.localizart.android.report.vo.WifiVO
import kotlinx.android.synthetic.main.activity_hotword.*

class HotWordActivity : Activity(), RecognitionListener, View.OnClickListener {

    companion object {
        //                const val PANIC_KEYWORD = "hello"
        const val PANIC_KEYWORD = "ayuda"
        const val REQUEST_AUDIO = 45
        const val TAG = "HotWordActivity"
    }

    /* Configuration.
	 */
    private var configuration: Configuration? = null
    /**
     * Antenna data handling.
     */
    private var antennaDataHandler: AntennaDataHandler? = null

    /**
     * Location data handling.
     */
    private var locationDataHandler: LocationDataHandler? = null

    /**
     * Wifi data handling. //JM
     */
    private var wifiDataHandler: WifiDataHandler? = null
    private val LOCATION_CODE = 1

    /**
     * Broadcast receiver for battery information.
     */
    private val batteryReceiver = BatteryReceiver()

    private var email = ""
    private var imei = ""
    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var audioManager: AudioManager? = null
    private val TAG = "SpeechTest"
    private var spechStarted = false
    private lateinit var mMediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotword)

        // Load configuration:
        configuration = Configuration(this)


        // Initializing the audio Manager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager


        // set VoiceDetection Intent
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech?.setRecognitionListener(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "es-ES")
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                "es-ES")
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent?.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        // check permissions
        if (!(checkPermissionStatusForLocation() && checkPermissionStatusForPhoneState())) {
            requestPermission()
        } else {
            antennaDataHandler = AntennaDataHandler(this)
            antennaDataHandler?.init()
        }
        if (!checkPermissionStatusForRecordAudio()) {
            getRecordAudioPermission()
        }


        // Create data handlers:
        locationDataHandler = LocationDataHandler(this)
        locationDataHandler?.configuration = configuration
        locationDataHandler?.init()

        /* //JM */
        wifiDataHandler = WifiDataHandler(this)
        wifiDataHandler?.init()


        if (intent != null) {
            email = intent.getStringExtra("email")
            imei = intent.getStringExtra("imei")
        } else {
            email = configuration?.userEmail ?: ""
            imei = configuration?.userIMEI ?: ""
        }

        // init data
        // start media Player
        mMediaPlayer = MediaPlayer.create(this, R.raw.hot_word_audio)
        pulsator.start()
        mMediaPlayer.setVolume(1f, 1f)
        mMediaPlayer.start()
        mMediaPlayer.setOnCompletionListener {
            startSpeechDetectionListener()
        }


        // set clickListener
        tvFinalizer.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvFinalizer -> {
//                val bundle = Bundle()
//                bundle.putString("email", email)
//                bundle.putString("imei", imei)
//
//                Log.d("ACTIVITYCOMENZAR", " -------------------------------------------RUNNING")
//
//                val myIntent = Intent(this, InformationActivity::class.java)
//                myIntent.putExtras(bundle)
//
//                startActivity(myIntent)
                finish()
            }
        }
    }

    override fun onPanelClosed(featureId: Int, menu: Menu?) {
        super.onPanelClosed(featureId, menu)
        speech?.destroy()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.e(TAG, "onReadyforSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.e(TAG, "onRmsChanged: " + rmsdB)
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.e(TAG, "onBufferReceived")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.e(TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.e(TAG, "onEvent")
    }

    override fun onBeginningOfSpeech() {
        Log.e(TAG, "onBeginningOfSpeech")
        spechStarted = true
        muteAudio(true)
    }

    override fun onEndOfSpeech() {
        muteAudio(true)
        spechStarted = false
        speech?.startListening(recognizerIntent)
    }

    override fun onError(error: Int) {
        Log.e(TAG, "FAILED ")

        if (SpeechRecognizer.ERROR_RECOGNIZER_BUSY == error) {
            Handler().postDelayed(Runnable { startSpeechDetectionListener() }, 1000)
            return
        }
        if (!spechStarted)
            startSpeechDetectionListener()
    }

    override fun onResults(results: Bundle?) {
        Log.e(TAG, "onResults")
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val speechString = matches!![0]
        if (speechString.toLowerCase().contains(PANIC_KEYWORD)) {
            sendPanic(this)

        }
    }

    private fun startSpeechDetectionListener() {
        if (null == speech) {
            speech = SpeechRecognizer.createSpeechRecognizer(this)
            speech?.setRecognitionListener(this)
        }
        speech?.setRecognitionListener(this)
        speech?.startListening(recognizerIntent)
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {


            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE)
            ActivityCompat.requestPermissions(this, permissions, LOCATION_CODE)
        }
    }

    private fun getRecordAudioPermission(): Boolean {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_AUDIO)
                return false

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_AUDIO)
                return false

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            return true
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission granted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_toast), Toast.LENGTH_SHORT).show()
                val showRationaleForLocation = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                val showRationaleForPhoneState = shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)
                if (!showRationaleForLocation && !showRationaleForPhoneState) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 101)
                }
            }
        }
        if (requestCode == REQUEST_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechDetectionListener()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_toast), Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(this@HotWordActivity)
                // Set the alert dialog title
                builder.setTitle(getString(R.string.permission_denied_toast))
                // Display a message on alert dialog
                builder.setMessage(getString(R.string.mic_permission_retry))

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton(getString(R.string.try_again)){dialog, which ->
                    // Do something when user press the positive button
                    /*val showRationaleForRecordAudio = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                    if (!showRationaleForRecordAudio) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, 101)
                    }*/
                    getRecordAudioPermission()
                }
                // Display a negative button on alert dialog
                builder.setNegativeButton("No"){dialog,which ->
//                    val bundle = Bundle()
//                    bundle.putString("email", email)
//                    bundle.putString("imei", imei)
//                    Log.d("ACTIVITYCOMENZAR", " -------------------------------------------RUNNING")
//                    val myIntent = Intent(this, InformationActivity::class.java)
//                    myIntent.putExtras(bundle)
//                    startActivity(myIntent)
                    finish()
                }
                /*// Display a neutral button on alert dialog
                builder.setNeutralButton("No"){_,_ ->

                }*/

                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()

                // Display the alert dialog on app interface
                dialog.show()


            }
        }
    }

    private fun checkPermissionStatusForLocation(): Boolean {
        val resultForLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return resultForLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionStatusForPhoneState(): Boolean {
        val resultForLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        return resultForLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionStatusForRecordAudio(): Boolean {
        val resultForLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return resultForLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun sendPanic(context: Context?) {
        Log.v(">>>>>>>", "sendPanic")

        if (context != null && configuration != null) {
            val packageType = configuration?.getPackageType(ConfigurationConstants.DEFAULT_PACKAGE_TYPE
                    .getString(context))

            val reportingURLs = Util.getReportingURLs(Constants.STATE_PANIC)

            val dataSender = DataSender(context)
            dataSender.configuration = configuration

            var antennaVO: AntennaVO? = if (antennaDataHandler?.handle() != null) antennaDataHandler?.handle() as AntennaVO else null
            val batteryVO = batteryReceiver.batteryVO
            var gpsVO: GPSVO? = null

            // Check configuration to see if we have to send the full info
            // (antenna + battery + GPS).

            if (ConfigurationConstants.PackageType.fromString(packageType, context) == ConfigurationConstants.PackageType.LONG) {
                gpsVO = locationDataHandler?.handle() as GPSVO
            }

            /* // JM WIFI DATA */
            if (wifiDataHandler?.handle() == null)
                return

            val wifiVO = wifiDataHandler?.handle() as WifiVO

            // Send the data to the server.
            dataSender.send(reportingURLs, antennaVO, batteryVO, gpsVO, wifiVO, Constants.STATE_PANIC, 0,
                    InformationService.getFormattedTicket())
            val mVibrator = context
                    .getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 500, 100, 1500, 100, 500, 100, 1500, 100, 3000)
            mVibrator.vibrate(pattern, -1)

            InformationService.setOnPanic(true)

            val params = dataSender.buildSMS("", antennaVO, batteryVO,
                    gpsVO, wifiVO, 1, 0,
                    InformationService.getFormattedTicket())
            println("////////////////// SEND SMS PARAMS /////////> $params")
        }
    }

    /**
     * Mutes (or) un mutes the audio
     *
     * @param mute The mute audio status
     */
    private fun muteAudio(mute: Boolean) {
        try {
            // mute (or) un mute audio based on status
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE, 0)
            } else {
                audioManager?.setStreamMute(AudioManager.STREAM_MUSIC, mute)
            }
        } catch (e: Exception) {
            if (audioManager == null) return

            // un mute the audio if there is an exception
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            } else {
                audioManager?.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }
        }

    }


    override fun onPause() {
        speech?.destroy()
        speech = null
        super.onPause()
    }


    override fun onDestroy() {
        mMediaPlayer.reset()
        mMediaPlayer.release()
        muteAudio(false)
        super.onDestroy()
    }
}
