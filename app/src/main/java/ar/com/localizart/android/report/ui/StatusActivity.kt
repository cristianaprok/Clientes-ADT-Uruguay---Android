package ar.com.localizart.android.report.ui

import android.app.Activity
import android.app.ProgressDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import ar.com.localizart.android.report.R
import ar.com.localizart.android.report.config.Configuration
import ar.com.localizart.android.report.enums.Constants
import ar.com.localizart.android.report.info.DataSender
import ar.com.localizart.android.report.listeners.UrlResponseListener
import ar.com.localizart.android.report.service.MyJobService
import ar.com.localizart.android.report.service.MyJobServicePreM
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import kotlinx.android.synthetic.main.layout_activity_status.*


/**
@author

 */

// TODO added by PS start
class StatusActivity : Activity(), View.OnClickListener {

    private val TAG = "StatusActivity"
    private var email = ""
    private var imei = ""
    private var Push_id = ""
    private lateinit var mMediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity_status)


        // get data from intent
        if (intent != null) {
            email = intent.getStringExtra("email")
            imei = intent.getStringExtra("imei")
        }
        Push_id = Configuration(this).pushToken ?: ""
        Log.e("push_id", Push_id)

        // start media Player
        mMediaPlayer = MediaPlayer.create(this, R.raw.ready_to_start_audio_status_1)

        // starting Job for reportLocationImmediately() method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val componentName = ComponentName(this, MyJobService::class.java)
            val jobInfo = JobInfo.Builder(12, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build()
            val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val resultCode = jobScheduler.schedule(jobInfo)
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled!")
            } else {
                Log.d(TAG, "Job not scheduled")
            }
        } else {
            val driver = GooglePlayDriver(this)
            val firebaseJobDispatcher = FirebaseJobDispatcher(driver)
            val job = firebaseJobDispatcher.newJobBuilder()
                    .setService(MyJobServicePreM::class.java)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .build()
            firebaseJobDispatcher.schedule(job)
        }
        // need to hit API here
        val url = Constants.STATUS_URL + "email=$email&IMEI=$imei&push_id=$Push_id"

        val datasender = DataSender(this, UrlResponseListener { code_response, response ->
            hideProgressDialog()
            if (200 == code_response && response.isNotEmpty() && response.contains("status_mobile")) {
                runOnUiThread {
                    when (response.substring(response.indexOf(":") + 1, response.indexOf(":") + 2)) {
                        "0" -> {
                            tvReturn.visibility = View.VISIBLE
                            btnContinue.visibility = View.GONE
                            tvOliviaText.visibility = View.GONE
                            tvOliviaTextMore.visibility = View.VISIBLE
                            tvOliviaTextMore.text = getString(R.string.status_alert_0)
                        }
                        "1" -> {
                            tvReturn.visibility = View.VISIBLE
                            btnContinue.visibility = View.GONE
                            tvOliviaText.visibility = View.GONE
                            tvOliviaTextMore.visibility = View.VISIBLE
                            tvOliviaTextMore.text = getString(R.string.alert_status_1_text)
                        }
                        "2" -> {
                            //tvStatusMobile.text = ""
                            btnContinue.visibility = View.VISIBLE
                            tvReturn.visibility = View.GONE
                            tvOliviaText.visibility = View.VISIBLE
                            tvOliviaTextMore.visibility = View.VISIBLE
                            tvOliviaText.text = getString(R.string.alert_status_2_text_one)
                            tvOliviaTextMore.text = getString(R.string.alert_status_2_text_two)
                            mMediaPlayer.setVolume(1f, 1f)
                            mMediaPlayer.start()
                        }
                    }
                }
            }
        })

        datasender.getURLNew(url)
        showProgressDialog()

        // set clickListener
        btnContinue.setOnClickListener(this)
        tvReturn.setOnClickListener(this)
        ivClose.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnContinue -> {
                val bundle = Bundle()
                bundle.putString("email", email)
                bundle.putString("imei", imei)

                Log.d("ACTIVITYCOMENZAR", " -------------------------------------------RUNNING")

                val myIntent = Intent(this, HotWordActivity::class.java)
                myIntent.putExtras(bundle)

                startActivity(myIntent)
                finish()
            }
            R.id.tvReturn, R.id.ivClose
            -> {
                onBackPressed()
            }
        }
    }

    private fun hideProgressDialog() {

        if (dialog?.isShowing!!) {
            val run = Runnable { dialog?.dismiss() }
            runOnUiThread(run)
        }
    }


    private var dialog: ProgressDialog? = null

    internal fun showProgressDialog() {
        if (dialog != null)
            if (dialog!!.isShowing)
                return

        val run = Runnable {
            dialog = ProgressDialog(this@StatusActivity)
            dialog!!.setTitle(getString(R.string.cartel_espereporfavor))
            dialog!!.setMessage(getString(R.string.cartel_essolounmomento))
            dialog!!.isIndeterminate = true

            // Don't disable the progress dialog if we tap on the screen:
            dialog!!.setCancelable(false)
            dialog!!.setCanceledOnTouchOutside(false)

            // api 5	dialog.onBackPressed();
            dialog!!.show()
        }
        runOnUiThread(run)
    }

    override fun onDestroy() {
        mMediaPlayer.reset()
        mMediaPlayer.release()
        super.onDestroy()
    }
}
// TODO added by PS end