package com.zendesk.adtapp.ui;


import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zendesk.adtapp.R;
import com.zendesk.adtapp.storage.UserProfileStorage;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.zendesk.logger.Logger;
import com.zendesk.adtapp.model.UserProfile;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.util.StringUtils;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;


public class CreateProfileActivity extends AppCompatActivity {

    private UserProfileStorage mUserProfileStorage;
    private PlaceholderFragment mPlaceHolderFragment;
    private Button forgotButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zendesk.adtapp.R.layout.activity_create_profile);


        getPhoneStatePermission();
        getInternetPermission();
        getCAMERAPermission();
        getWakeLockPermission();
        getEXternalPermission();
        mUserProfileStorage = new UserProfileStorage(this);
        final FragmentManager fm = getFragmentManager();
        PlaceholderFragment dataFragment = (PlaceholderFragment) fm.findFragmentByTag(PlaceholderFragment.TAG);

        if (dataFragment == null) {
            dataFragment = new PlaceholderFragment();
            fm.beginTransaction().add(com.zendesk.adtapp.R.id.container, dataFragment, PlaceholderFragment.TAG).commit();
            dataFragment.setCurrentBitmap(mUserProfileStorage.getProfile().getAvatar());
        }

        this.mPlaceHolderFragment = dataFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showStoredProfile();
    }

    private void showStoredProfile() {
        UserProfile userProfile = mUserProfileStorage.getProfile();

        forgotButton = (Button)findViewById(com.zendesk.adtapp.R.id.button4);
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                intent.putExtra("IS_PASSWORD", true);
                startActivity(intent);
            }
        });

        ImageButton button = (ImageButton) this.findViewById(com.zendesk.adtapp.R.id.imageButton);
        EditText nameText = (EditText) this.findViewById(com.zendesk.adtapp.R.id.nameText);
        EditText emailText = (EditText) this.findViewById(com.zendesk.adtapp.R.id.emailText);
        EditText accontText = (EditText) this.findViewById(com.zendesk.adtapp.R.id.accountNumber);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

        if (mPlaceHolderFragment.getCurrentBitmap() != null) {
            button.setImageBitmap(mPlaceHolderFragment.getCurrentBitmap());
        }

        if(!StringUtils.hasLength(nameText.getText().toString())){
            nameText.setText(userProfile.getName());
        }
        if(!StringUtils.hasLength(accontText.getText().toString())){
            accontText.setText(userProfile.getAccountNumber());
        }

        if(!StringUtils.hasLength(emailText.getText().toString())){
            emailText.setText(userProfile.getEmail());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1001) {
            Bitmap bitmap = null;

            if(data.getData() != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if(data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap){
                bitmap = (Bitmap) data.getExtras().get("data");

            }

            if(bitmap != null){
                mPlaceHolderFragment.setCurrentBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));
                ((ImageButton) this.findViewById(com.zendesk.adtapp.R.id.imageButton)).setImageBitmap(mPlaceHolderFragment.getCurrentBitmap());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.zendesk.adtapp.R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == com.zendesk.adtapp.R.id.action_save) {

            EditText nameText = (EditText) this.findViewById(com.zendesk.adtapp.R.id.nameText);
            EditText emailText = (EditText) this.findViewById(com.zendesk.adtapp.R.id.emailText);
            EditText accontText = (EditText) this.findViewById(com.zendesk.adtapp.R.id.accountNumber);

            String email = emailText.getText().toString();
            String accountNumber = accontText.getText().toString();

            if (StringUtils.hasLength(email)) {
                mUserProfileStorage.storeUserProfile(
                        nameText.getText().toString(),
                        email,
                        mPlaceHolderFragment.getCurrentBitmap(),accountNumber
                );
                if (getPhoneStatePermission()){
                    logUser(nameText.getText().toString(),email);
                }
                final UserProfile profile = mUserProfileStorage.getProfile();
                if (StringUtils.hasLength(profile.getEmail())) {
                    Logger.i("Identity", "Setting identity");
                    //ZendeskConfig.INSTANCE.setIdentity(new JwtIdentity(profile.getEmail()));
                    ZendeskConfig.INSTANCE.setIdentity(new AnonymousIdentity.Builder().withNameIdentifier(profile.getName()).withEmailIdentifier(profile.getEmail()).build());
                    // Init Zopim Visitor info
                    final VisitorInfo.Builder build = new VisitorInfo.Builder()
                            .email(profile.getEmail());

                    if (StringUtils.hasLength(profile.getName())) {
                        build.name(profile.getName());
                    }

                    ZopimChat.setVisitorInfo(build.build());
                }

                finish();

            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(com.zendesk.adtapp.R.string.fragment_profile_invalid_email), Toast.LENGTH_LONG).show();

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private boolean getPhoneStatePermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                return false;

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1);
                return  false;

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else {
            return true;
        }
    }

    private boolean getInternetPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                return false;

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        1);
                return  false;

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else {
            return true;
        }
    }
    private boolean getWakeLockPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WAKE_LOCK)) {
                return false;

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WAKE_LOCK},
                        1);
                return  false;

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else {
            return true;
        }
    }
    private boolean getCAMERAPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                return false;

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
                return  false;

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else {
            return true;
        }
    }
    private boolean getEXternalPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                return false;

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.ret

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
                return  false;

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }else {
            return true;
        }
    }

    private void logUser(String name,String email) {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        String ts = Context.TELEPHONY_SERVICE;
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(ts);
        String imsi = mTelephonyMgr.getSubscriberId();
        String imei = mTelephonyMgr.getDeviceId();
        Crashlytics.setUserIdentifier(imei);
        Crashlytics.setUserEmail(email);
        Crashlytics.setUserName(name);


        // TODO: Use your own attributes to track content views in your app
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Profile")
                .putContentType("Video")
                .putContentId(imei)
                .putCustomAttribute("IMEI", imei)
                .putCustomAttribute("IMSI", imsi)
                .putCustomAttribute("Name",name)
                .putCustomAttribute("Email",email));

    }


    private void openImageIntent() {

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        startActivityForResult(chooserIntent, 1001);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public static final String TAG = "placeholder_fragment_create_profile";

        private Bitmap currentBitmap;

        public PlaceholderFragment() {
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(com.zendesk.adtapp.R.layout.fragment_create_profile, container, false);
        }

        public Bitmap getCurrentBitmap() {
            return currentBitmap;
        }

        public void setCurrentBitmap(final Bitmap currentBitmap) {
            this.currentBitmap = currentBitmap;
        }
    }
}
