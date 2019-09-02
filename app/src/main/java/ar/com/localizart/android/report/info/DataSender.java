package ar.com.localizart.android.report.info;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.enums.ConfigurationConstants;
import ar.com.localizart.android.report.enums.UtilConstants;
import ar.com.localizart.android.report.listeners.UrlResponseListener;
import ar.com.localizart.android.report.service.InformationService;
import ar.com.localizart.android.report.vo.AntennaVO;
import ar.com.localizart.android.report.vo.BatteryVO;
import ar.com.localizart.android.report.vo.GPSVO;
import ar.com.localizart.android.report.vo.URLVO;
import ar.com.localizart.android.report.vo.WifiVO;

/**
 * Helper class to send the phone information to a server through a HTTP
 * request.
 *
 * @author diego
 */
public class DataSender {

    private static final String TAG = DataSender.class.getSimpleName();

    /**
     * Context.
     */
    private Context context;

    /**
     * Configuration.
     */
    private Configuration configuration;

    /*
     * Listener para resultado de la url
     */
    private UrlResponseListener url_listener = null;

    private boolean panic;

    /**
     * Constructor.
     *
     * @param context
     */
    public DataSender(Context context) {
        this.context = context;
        url_listener = null;
    }

    /**
     * Constructor.
     *
     * @param context
     * @param url_listener UrlResponseListener
     */
    public DataSender(Context context, UrlResponseListener url_listener) {
        this.context = context;
        this.url_listener = url_listener;
    }

    /**
     * Send the phone information to a server through HTTP.
     *
     * @param antennaVO
     * @param batteryVO
     * @param gpsVO
     */
    public void send(List<String> urlPrefixes, AntennaVO antennaVO,
                     BatteryVO batteryVO, GPSVO gpsVO, WifiVO wifiVO, boolean panic,
                     int event, String ticket) {

        System.out.println("////////////////// SEND URL /////////" + gpsVO);

		/*if (!panic // Under panic the message is always sent.
				&& antennaVO == null && batteryVO == null && gpsVO == null) {
			Log.i(TAG, "There's no information to send to the server.");
			return;
		}*/

        this.panic = panic;

        for (String urlPrefix : urlPrefixes) {
            URLVO urlVO = buildURL(urlPrefix, antennaVO, batteryVO, gpsVO,
                    wifiVO, panic, event, ticket);

            boolean online = isOnline();

            if (!online) {
                Log.i(TAG,
                        "Can't send phone information to server, the network is unavailable.");
            }

            if (online || panic) {
                ServerReportTask rt = new ServerReportTask();
                rt.execute(urlVO);
            }

        }
    }

    public void getURL(String url, RequestParams params) {
        System.out.println(">>>>>>>>>>>>>GET URL PARAMS: >> " + url + params);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                Log.w("async", "success!!!!");
                if (url_listener != null) {
                    url_listener.onUrlResponse(200, response);
                }

            }

            @Override
            public void onFailure(Throwable error, String response) {
                if (url_listener != null) {
                    url_listener.onUrlResponse(-1, response);
                }
                super.onFailure(error, response);
            }
        });
    }

    // TODO added by PS start
    public void getURLNew(String url) {
        System.out.println(">>>>>>>>>>>>>GET URL PARAMS: >> " + url);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                Log.w("async", "success!!!!");
                if (url_listener != null) {
                    url_listener.onUrlResponse(200, response);
                }
            }

            @Override
            public void onFailure(Throwable error, String response) {
                if (url_listener != null) {
                    url_listener.onUrlResponse(-1, response);
                }
                super.onFailure(error, response);
            }
        });
    }

    /**
     * Build a URL to call based on the configured URL prefix, and antenna,
     * battery and GPS data.
     *
     * @param urlPrefix
     * @param antennaVO
     * @param batteryVO
     * @param gpsVO
     * @return
     */
    public URLVO buildURL(String urlPrefix, AntennaVO antennaVO,
                          BatteryVO batteryVO, GPSVO gpsVO, WifiVO wifiVO, boolean panic,
                          int event, String ticket) {
        System.out.println("////////////////// BUILD URL/////////");
        StringBuffer sb = new StringBuffer();

        sb.append(urlPrefix);
        sb.append("?");

        // Append antenna info:
        if (antennaVO != null) {
            sb.append(antennaVO.toQueryString());
        }

        if (panic) {
            sb.append("&event=1");
            this.panic = true;
        } else {
            sb.append("&event=" + event);
        }

        String appname = context.getResources().getString(R.string.app_name);
        String versionName = "1";
        try {
            versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        String appencoded = Uri.encode("" + appname + "_" + versionName);
        sb.append("&app=" + appencoded);

        if (batteryVO != null) {
            sb.append(batteryVO.toQueryString());
        }
        System.out.println("////////////////// BUILD URL GPS/////////" + gpsVO);
        if (gpsVO != null) {
            System.out
                    .println("////////////////// BUILD URL GPS APPEND/////////");
            sb.append(gpsVO.toQueryString());
        } else {
            sb.append("&statusgps=0");
        }

        sb.append("&ticket=" + ticket);
        sb.append(wifiVO.toQueryString());
        sb.append("&rcode=" + configuration.getUserCode("-"));
        sb.append("&device_token=" + configuration.getPushToken());
        URLVO urlVO = new URLVO();
        urlVO.setPrefix(urlPrefix);
        urlVO.setUrl(sb.toString());
        return urlVO;
    }
    // TODO added by PS end

    public String buildSMS(String urlPrefix, AntennaVO antennaVO,
                           BatteryVO batteryVO, GPSVO gpsVO, WifiVO wifiVO, int panic,
                           int event, String ticket) {
        System.out.println("////////////////// BUILD SMS/////////");
        StringBuffer sb = new StringBuffer();

        sb.append(ticket + ";");
        if (antennaVO != null) {
            sb.append(antennaVO.getImei() + ";");
        } else {
            sb.append(";");
        }
        sb.append("1;");

        if (antennaVO != null) {
            sb.append(antennaVO.getCellId() + ";");
        } else {
            sb.append(";");
        }

        if (antennaVO != null) {
            sb.append(antennaVO.getLac() + ";");
        } else {
            sb.append(";");
        }

        System.out.println("////////////////// BUILD URL GPS/////////" + gpsVO);
        if (gpsVO != null) {
            System.out
                    .println("////////////////// BUILD URL GPS APPEND/////////");
            sb.append(gpsVO.getLatitude() + ";");
            sb.append(gpsVO.getLongitude() + ";");
            sb.append(gpsVO.getSpeed() + ";");
            sb.append(gpsVO.getAccuracy() + ";");
        } else {
            sb.append(";;;;");
        }

        // SEND WIFI DATA //JM
        sb.append(wifiVO.getBSSID());

        System.out.println("BUILD SMS : " + sb.toString());

        return sb.toString();
    }

    /**
     * Parse a line of text coming from the server response.
     *
     * @param line    The line we read from the server.
     * @param context The context.
     */
    void parseLine(String line, Context context) {
        System.out.println("------------- PARSE LINE---------> " + line);
        if (line != null) {
            /*
             * A line looks like this: "tiempo=60 tipo_loc=antena"
             */

            // Get eack key/value pair:
            String[] keyValuePairs = line.trim().split(" ");

            for (String keyValuePair : keyValuePairs) {
                // Parse it:
                parseKeyValuePair(keyValuePair, context);
            }
        }
    }

    /**
     * Parse each key/value pair read from a line from the server response.
     *
     * @param keyValuePair The key/value pair embedded in a string.
     * @param context      The context.
     */
    void parseKeyValuePair(String keyValuePair, Context context) {

        System.out.println("------------- PARSE KEY---------> " + keyValuePair);
        if (keyValuePair != null) {
            String[] parts = keyValuePair.split("=");

            System.out.println("------------- PARSE LINE---------> " + parts[0]
                    + "  " + parts[1]);

            Map<String, String> map = new HashMap<String, String>();

            if (parts[0].equalsIgnoreCase(context
                    .getString(R.string.return_param_frequency))) {
                Log.v(TAG, "Received frequency: " + parts[1]);
                String frequency = parts[1];

                System.out.println("------------- FREQUENCY---------> "
                        + parts[1]);

                map.put(ConfigurationConstants.FREQUENCY.toString(), frequency);
            }

            if (parts[0].equalsIgnoreCase(context
                    .getString(R.string.return_param_package_type))) {
                Log.v(TAG, "Received package type: " + parts[1]);
                String packageType = parts[1];

                map.put(ConfigurationConstants.PACKAGE_TYPE.toString(),
                        packageType);
            }

            if (parts[0].equalsIgnoreCase(context
                    .getString(R.string.return_param_url))) {
                System.out.println("RESPONSE parts  : " + parts);
                System.out.println("RESPONSE parts length : " + parts.length);

                if (parts.length > 1) {
                    Log.v(TAG, "Received url: " + parts[1]);
                    String url = parts[1];

                    map.put(ConfigurationConstants.URL.toString(), url);
                }
            }

            configuration.saveValues(map);
        }
    }

    /**
     * Returns <code>true</code> if the network is available.
     *
     * @return
     */
    private boolean isOnline() {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }
        return false;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Inner class to process the calls to the server into another thread since
     * Android doens't let us do it in the main execution thread, to avoid ANR
     * problems (Application Not Responding).
     *
     * @author diego
     */
    public class ServerReportTask extends AsyncTask<URLVO, Integer, Void> {

        // Do the long-running work in here:
        protected Void doInBackground(URLVO... urls) {
            for (URLVO urlVO : urls) {
                InputStream is = null;
                int retries = 0;

                while (true) {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URL(
                                urlVO.getUrl()).openConnection();

                        conn.setReadTimeout(UtilConstants.TWO_MINUTES);
                        conn.setConnectTimeout(UtilConstants.TWO_MINUTES);
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);

                        conn.connect();

                        int response = conn.getResponseCode();

                        Log.v(TAG, "URL RESPONSE: " + urlVO.getUrl()
                                + " returned: " + response + " retries: "
                                + retries);
                        Answers.getInstance().logCustom(new CustomEvent("Tracking")
                                .putCustomAttribute("url", urlVO.getUrl())
                                .putCustomAttribute("response", response)
                                .putCustomAttribute("device_token", configuration.getPushToken()));

                        is = conn.getInputStream();

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is), 512);

                        String line;

                        if (url_listener != null) {
                            StringBuilder sb = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            url_listener.onUrlResponse(response, sb.toString());
                        } else {
                            // Only change the state if the URL is not a
                            // "report only" one.
                            if (!urlVO.isReportOnly()) {
                                InformationService.increaseTicket();

                                if (DataSender.this.panic && response == 200) {
                                    InformationService.setOnPanic(false);
                                }
                            }
                        }

                        // Only parse the response if the URL is not a
                        // "report only" one.
                        if (!urlVO.isReportOnly()) {
                            while ((line = reader.readLine()) != null) {
                                System.out.println("RESPONSE line : " + line);
                                parseLine(line, context);
                            }
                        }

                        // If the server returned a status 200 (OK), then
                        // continue with the next URL.
                        // If an error was returned, only retry if we are under
                        // panic mode.
                        if (response == 200 || !DataSender.this.panic) {
                            break;
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Error while sending phone information to: "
                                + urlVO.getUrl(), t);
                        if (url_listener != null) {
                            url_listener.onUrlResponse(-1, t.toString());
                        }
                    } finally {
                        // Makes sure that the InputStream is closed:
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Throwable t) {
                            }
                        }
                    } // end try/catch/finally
                } // end while
            } // end for

            return null;
        }
    }

}
