package ar.com.localizart.android.report.ui;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ar.com.localizart.android.report.R;
import ar.com.localizart.android.report.config.Configuration;
import ar.com.localizart.android.report.enums.ConfigurationConstants;
import ar.com.localizart.android.report.enums.Constants;
import ar.com.localizart.android.report.info.DataSender;
import ar.com.localizart.android.report.listeners.UrlResponseListener;
import ar.com.localizart.android.report.util.ADTApplication;
import ar.com.localizart.android.report.util.SlideToActView;

//import com.ebanx.swipebtn.SwipeButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.loopj.android.http.RequestParams;


public class IniciarActivity extends Activity {


    public static final String TAG = "FireBaseMessaging";
    private final int SCREEN_INICIAR = 0;
    private final int SCREEN_REGISTRAR = 1;
    private final int SCREEN_CONFIRMAR = 2;
    private final int SCREEN_REGISTRADO = 3;
    private final int SCREEN_NUM = 4;
    Configuration configuration;
    boolean shouldExit = false;
    private String user_nombre = "";
    private String user_email = "";
    private String user_imei = "";
    private String user_code = "";
    private String user_mnc = "";
    private String user_mcc = "";
    private String user_language = "";
    private String device_token = "";
    private EditText editNOMBRE;
    private EditText editEMAIL;
    private EditText editCODE;
    private TextView textEMAIL;
    private LinearLayout buttonIniciar;
    private FloatingActionButton buttonRegistrarSiguiente;
    private FloatingActionButton buttonConfirmarSiguiente;
    private Thread internalThread;
    private View[] screen = new View[SCREEN_NUM];
    private int SCREEN_CURRENT = SCREEN_INICIAR;
    private boolean SCREEN_MOVING = false;
    private int TMSG_DURACION = Toast.LENGTH_LONG;
    private int LOCATION_CODE = 1;
    private ProgressDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (!(checkPermissionStatusForLocation() && checkPermissionStatusForPhoneState())) {
//            requestPermission();
//        }
        configuration = new Configuration(this);

        user_email = configuration.getUserEmail();
        user_nombre = configuration.getUserName();
        user_imei = configuration.getUserIMEI();

        user_mnc = configuration.getUserMNC();
        user_mcc = configuration.getUserMCC();
        user_language = configuration.getUserLanguage();


        device_token = FirebaseInstanceId.getInstance().getToken();
        if (device_token != null) {
            configuration.setPushToken(device_token);
            Log.d(TAG, "Device Token : " + device_token);
        }
        if (user_language == null || user_language.equals("")) {
            user_language = Locale.getDefault().toString();
        }

        if (user_email == null || user_email.equals("")) {

            /// NO HA REGISTRADO
            Log.e("tag", "onCreate");
            setContentView(R.layout.layout_main);

            // Start thread to get IMEI:
            internalThread = new Thread(new Runnable() {

                private String maskIMEI(String imei) {
                    if (imei == null) {
                        Log.e(">>>>>>>>", "Error! imei is null");
                        return "00000000";
                    }

                    int len = imei.length();
                    // Return last 8 digits:
                    int limit = 8;

                    if (len <= limit) {
                        Log.w(">>>>>>>>", "Warning IMEI too short: " + imei);
                        return imei;
                    }

                    return imei;//JM .substring(len - limit);
                }

                public void run() {
                    // Get IMEI:
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    user_imei = maskIMEI(telephonyManager.getDeviceId());

                    // Get MCC/MNC:
                    String networkOperator = telephonyManager.getNetworkOperator();

                    if (networkOperator != null && networkOperator != "") {
                        user_mcc = networkOperator.substring(0, 3);
                        user_mnc = networkOperator.substring(3);
                    }

                }
            });
            if (getGPSPermission() && getPhoneStatePermission()) {
                if (internalThread.getState() == Thread.State.NEW) {
                    internalThread.start();
                }
            }


            screen[SCREEN_INICIAR] = findViewById(R.id.includeIniciar);
            screen[SCREEN_REGISTRAR] = findViewById(R.id.includeRegistrar);
            screen[SCREEN_CONFIRMAR] = findViewById(R.id.includeConfirmacion);
            screen[SCREEN_REGISTRADO] = findViewById(R.id.includeRegistrado);

            setScreen(SCREEN_CURRENT);

            buttonIniciar = findViewById(R.id.llIniciarButonIniciar);
            buttonRegistrarSiguiente = findViewById(R.id.imageRegistrarButtonSiguiente);
            ImageView buttonRegistrarAtras = findViewById(R.id.imageRegistrarButtonAtras);
            buttonConfirmarSiguiente = findViewById(R.id.imageConfirmacionButtonSiguiente);
            ImageView buttonConfirmarAtras = findViewById(R.id.imageConfirmarButtonAtras);

            SlideToActView buttonFinalizar = findViewById(R.id.llRegistradoButonFinalizar);
            //LinearLayout buttonFoto = (LinearLayout) findViewById(R.id.llRegistrarButtonFoto);

            editEMAIL = findViewById(R.id.editTextIniciarEmail);
            editNOMBRE = findViewById(R.id.editTextRegistrarNombre);
            editCODE = findViewById(R.id.editTextConfirmacionCodigo);
            textEMAIL = findViewById(R.id.textConfirmacionCuentaEmail);

            buttonIniciar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onButtonIniciar())
                        buttonIniciar.setEnabled(false);
                }
            });

            buttonRegistrarSiguiente.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onButtonRegistrar())
                        buttonRegistrarSiguiente.setEnabled(false);
                }
            });

            buttonRegistrarAtras.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonAtras();
                    buttonIniciar.setEnabled(true);
                }
            });

            buttonConfirmarAtras.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonAtras();
                    buttonRegistrarSiguiente.setEnabled(true);
                }
            });

            buttonConfirmarSiguiente.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonConfirmar();
                }
            });


            buttonFinalizar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonFinalizar();
                }
            });
        } else {
            // REGISTRADO
            activityAutoConfirming();
            finish();
        }
    }

    private boolean checkPermissionStatusForLocation() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean checkPermissionStatusForPhoneState() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
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
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    getGPSPermission();
                }
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                    getPhoneStatePermission();
                } else {
                    internalThread.start();
                }
            } else {
                Toast.makeText(this, getString(R.string.permission_denied_toast), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("tag", "onResume");
        if (!(checkPermissionStatusForPhoneState() && checkPermissionStatusForLocation())) {
            requestPermission();
        }
//        if (getGPSPermission() && getPhoneStatePermission()) {
//            if (internalThread.getStaxte() == Thread.State.NEW) {
//                internalThread.start();
//            }
//        }
        editEMAIL.requestFocus();
    }

    private boolean getGPSPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                return false;

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return false;

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            return true;
        }
    }

    private boolean getPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                return false;

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return false;
            }
        } else {
            return true;
        }
    }

    private void setScreen(int idscreen) {
        screen[SCREEN_CURRENT].setVisibility(View.INVISIBLE);
        SCREEN_CURRENT = idscreen;
        screen[SCREEN_CURRENT].setVisibility(View.VISIBLE);
    }

    private synchronized void nextScreen() {

        if (SCREEN_CURRENT >= SCREEN_NUM || SCREEN_MOVING) {
            return;
        }

        SCREEN_MOVING = true;

        final View vcurrent = screen[SCREEN_CURRENT];

        View vnext = screen[SCREEN_CURRENT + 1];

        int w1 = vcurrent.getWidth();
        TranslateAnimation outLeft = new TranslateAnimation(0, -w1, 0, 0);
        outLeft.setDuration(500);
        vcurrent.startAnimation(outLeft);

        vcurrent.setVisibility(View.VISIBLE);
        vnext.setVisibility(View.VISIBLE);

        outLeft.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vcurrent.setVisibility(View.INVISIBLE);
                SCREEN_CURRENT++;
                SCREEN_MOVING = false;
            }
        });


        int w2 = vnext.getWidth();
        TranslateAnimation inLeft = new TranslateAnimation(w2, 0, 0, 0);
        inLeft.setDuration(500);
        vnext.startAnimation(inLeft);
    }

    private synchronized void prevScreen() {

        if (SCREEN_CURRENT < 1 || SCREEN_MOVING)
            return;


        final View vcurrent = screen[SCREEN_CURRENT];

        View vprev = screen[SCREEN_CURRENT - 1];

        int w1 = vcurrent.getWidth();
        TranslateAnimation outLeft = new TranslateAnimation(0, w1, 0, 0);
        outLeft.setDuration(500);
        vcurrent.startAnimation(outLeft);

        vcurrent.setVisibility(View.VISIBLE);
        vprev.setVisibility(View.VISIBLE);

        outLeft.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vcurrent.setVisibility(View.INVISIBLE);
                SCREEN_CURRENT--;
                SCREEN_MOVING = false;
            }
        });


        int w2 = vprev.getWidth();
        TranslateAnimation inLeft = new TranslateAnimation(-w2, 0, 0, 0);
        inLeft.setDuration(500);
        vprev.startAnimation(inLeft);

    }

    private boolean onButtonRegistrar() {

        if (SCREEN_MOVING) {
            return false;
        }

        user_nombre = editNOMBRE.getText().toString();

        // verificacion datos
        if (user_nombre.length() < 1 || user_nombre.length() > 15) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getString(R.string.cartel_nombredebeser), TMSG_DURACION);

            toast.setGravity(Gravity.TOP, 10, 10);
            toast.show();
            return false;
        }

        RequestParams params = new RequestParams();
        params.put("email", user_email);
        params.put("IMEI", user_imei);
        params.put("MCC", user_mcc);
        params.put("MNC", user_mnc);
        params.put("LANG", user_language);
        params.put("mobilename", user_nombre);

        System.out.println("SENDING URL >>>>>>>>>>>>>>>>>>>> " + Constants.ADD_URL);
        DataSender datasender = new DataSender(IniciarActivity.this, new UrlResponseListener() {
            @Override
            public void onUrlResponse(final int code_response, final String response) {
                //si 1  next
                // si 0 reintentar

                System.out.println(" URL RESPONSE >>>>>>>>>>>>>>>>>>>> " + code_response + " : " + response);
                hideProgressDialog();
                Runnable run = new Runnable() {
                    public void run() {
                        if (code_response == 200) {
                            if (response.equals("1")) {
                                nextScreen();
                            }
                        }
                        buttonRegistrarSiguiente.setEnabled(true);
                    }
                };
                runOnUiThread(run);
                return;
            }
        });
        datasender.getURL(Constants.ADD_URL, params);
        showProgressDialog();

        return true;
    }

    private boolean onButtonIniciar() {
        if (SCREEN_MOVING)
            return false;
        //copiar datos nombre, tfno
        user_email = editEMAIL.getText().toString();
        // user_pass = editPASS.getText().toString();
//        textEMAIL.setText(getString(R.string.confirmacion_text_confirmacionsenvio) + user_email);

        String emailString = getString(R.string.confirmacion_text_confirmacionsenvio) + " " + user_email;
        SpannableString spanString = new SpannableString(emailString);
        spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), emailString.indexOf(":") + 1,
                emailString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textEMAIL.setText(spanString);


        //verificacion datos

        if (!isValidEmailAddr(user_email)) {
            //toast

            Toast toast = Toast.makeText(getApplicationContext(),
                    getString(R.string.cartel_escribacorrectamentemail), TMSG_DURACION);

            toast.setGravity(Gravity.TOP, 10, 10);
            toast.show();
            return false;
        }

        RequestParams params = new RequestParams();
        params.put("email", user_email);
        params.put("IMEI", user_imei);
        params.put("MCC", user_mcc);
        params.put("MNC", user_mnc);
        params.put("LANG", user_language);

        Log.e("tag", user_imei + " ");
        Log.e("tag", user_mnc + " ");
        Log.e("tag", user_mcc + " ");


        System.out.println("SENDING URL >>>>>>>>>>>>>>>>>>>> " + Constants.ADD_URL);
        DataSender datasender = new DataSender(IniciarActivity.this, new UrlResponseListener() {

            @Override
            public void onUrlResponse(final int code_response, final String response) {
                //si OK next Screen
                // si no OK nada
                System.out.println(" URL RESPONSE >>>>>>>>>>>>>>>>>>>> " + code_response + " : " + response);
                hideProgressDialog();
                Runnable run = new Runnable() {
                    public void run() {
                        if (code_response == 200) {

                            if (response.equals("1")) {

                                nextScreen();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getString(R.string.cartel_verifiquecontrasena),
                                        TMSG_DURACION);

                                toast.setGravity(Gravity.TOP, 10, 10);
                                toast.show();
                            }
                        }
                        buttonIniciar.setEnabled(true);
                    }
                };
                runOnUiThread(run);

                return;
            }
        });
        datasender.getURL(Constants.ADD_URL, params);
        showProgressDialog();
        return true;
    }

    private void onButtonConfirmar() {
        if (SCREEN_MOVING)
            return;
        //copiar datos codigo
        user_code = editCODE.getText().toString();
        //verificar datos
        if (user_code.length() != 4) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getString(R.string.cartel_compruebesucodigo),
                    TMSG_DURACION);

            toast.setGravity(Gravity.TOP, 10, 10);
            toast.show();
            return;
        }

        RequestParams params = new RequestParams();
        params.put("email", user_email);
        params.put("IMEI", user_imei);
        params.put("MCC", user_mcc);
        params.put("MNC", user_mnc);
        params.put("LANG", user_language);
        params.put("mobilename", user_nombre);
        params.put("code", user_code);
        params.put("device_token", configuration.getPushToken());

        System.out.println("SENDING URL >>>>>>>>>>>>>>>>>>>> " + Constants.ADD_URL);
        DataSender datasender = new DataSender(IniciarActivity.this, new UrlResponseListener() {

            @Override
            public void onUrlResponse(final int code_response, final String response) {
                //si OK next Screen
                // si no OK nada
                System.out.println(" URL RESPONSE >>>>>>>>>>>>>>>>>>>> " + code_response + " : " + response);
                hideProgressDialog();
                Runnable run = new Runnable() {
                    public void run() {
                        if (code_response == 200) {

                            if (response.equals("1")) {
                                Map<String, String> map = new HashMap<String, String>();

                                map.put(ConfigurationConstants.USER_EMAIL.toString(), user_email);
                                map.put(ConfigurationConstants.USER_NOMBRE.toString(), user_nombre);
                                map.put(ConfigurationConstants.USER_IMEI.toString(), user_imei);
                                map.put(ConfigurationConstants.USER_CODE.toString(), user_code);
                                map.put(ConfigurationConstants.USER_MCC.toString(), user_mcc);
                                map.put(ConfigurationConstants.USER_MNC.toString(), user_mnc);
                                map.put(ConfigurationConstants.USER_LANGUAGE.toString(), user_language);

                                configuration.saveValues(map);

                                nextScreen();
                            } else if (response.equals("2")) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getString(R.string.cartel_nolicencia),
                                        TMSG_DURACION);

                                toast.setGravity(Gravity.TOP, 10, 10);
                                toast.show();
                            }
                        }

                    }
                };

                runOnUiThread(run);

                return;
            }
        });
        datasender.getURL(Constants.ADD_URL, params);
        showProgressDialog();
    }

    private void onButtonFinalizar() {
        //visualizar datos ultima pantalla
        activityAutoConfirming();
        finish();

    }

    private void onButtonAtras() {

        prevScreen();

    }


    private void activityAutoConfirming() {
        Bundle bundle = new Bundle();
        bundle.putString("email", user_email);
        bundle.putString("imei", user_imei);

        Log.d("ACTIVITYAUTO_CONFIRMING", " -------------------------------------------RUNNING");

        Intent myIntent = new Intent(getBaseContext(), AfterConfirmingActivity.class);
        myIntent.putExtras(bundle);

        ////myIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(myIntent);
        ///api 5	overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    private void activityComenzar() {
        Bundle bundle = new Bundle();
        bundle.putString("email", user_email);
        bundle.putString("imei", user_imei);

        Log.d("ACTIVITYCOMENZAR", " -------------------------------------------RUNNING");

        Intent myIntent = new Intent(getBaseContext(), InformationActivity.class);
        myIntent.putExtras(bundle);

        ////myIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(myIntent);
        ///api 5	overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

/*
	public final static boolean isValidEmailAddr(CharSequence target) {
	    if (target == null) {
	        return false;
	    } else {
	        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	    }
	}
*/






    /**
     * Validates an email address. Checks that there is an "@"
     * in the field and that the address ends with a host that
     * has a "." with at least two other characters after it and
     * no ".." in it. More complex logic might do better.
     */
    public final static boolean isValidEmailAddr(String address) {
        int at = address.indexOf("@");
        int len = address.length();
        if (at <= 0 || at > len - 5) {
            return false;
        }
        String host = address.substring(at + 1, len);
        len = host.length();
        if (host.indexOf("..") >= 0) {
            return false;
        }
        int dot = host.lastIndexOf(".");
        return (dot > 0 && dot <= len - 2);
    }


    void showProgressDialog() {
        if (dialog != null)
            if (dialog.isShowing())
                return;

        Runnable run = new Runnable() {
            public void run() {
                dialog = new ProgressDialog(IniciarActivity.this);
                dialog.setTitle(getString(R.string.cartel_espereporfavor));
                dialog.setMessage(getString(R.string.cartel_essolounmomento));
                dialog.setIndeterminate(true);

                // Don't disable the progress dialog if we tap on the screen:
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);

                // api 5	dialog.onBackPressed();
                dialog.show();
            }
        };
        runOnUiThread(run);


    }

    void hideProgressDialog() {
        if (dialog != null)
            if (dialog.isShowing()) {
                Runnable run = new Runnable() {
                    public void run() {
                        dialog.dismiss();
                    }
                };
                runOnUiThread(run);
            }
        //	dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (shouldExit) {
            super.onBackPressed();
            return;
        }

        this.shouldExit = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                shouldExit = false;
            }
        }, 2000);
    }
}
