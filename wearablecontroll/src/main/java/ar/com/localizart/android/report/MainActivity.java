package ar.com.localizart.android.report;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DelayedConfirmationView.DelayedConfirmationListener {

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private DelayedConfirmationView mDelayedView;

    private static final String PANIC_LISTENER_WEAR_PATH = "/panic-listener-wear";
    private boolean mResolvingError = false;
    Button alertButton;
    View progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .build();

        //UI elements with a simple CircleImageView
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                alertButton = (Button) stub.findViewById(R.id.button);
                progressLayout = stub.findViewById(R.id.progressLayout);
                //Listener to send the message (it is just an example)
                alertButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Two seconds to cancel the action
                        mDelayedView.setTotalTimeMs(3000);
// Start the timer
                        mDelayedView.start();
                        alertButton.setVisibility(View.GONE);
                        progressLayout.setVisibility(View.VISIBLE);
                    }
                });
                mDelayedView =
                        (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
                mDelayedView.setListener(MainActivity.this);

            }
        });


    }


    /**
     * Send message to mobile handheld
     */
    private void sendMessage() {

        if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), PANIC_LISTENER_WEAR_PATH, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("TAG", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        } else {
            //Improve your code
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Improve your code
        System.out.println("connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Improve your code
        System.out.println("Connection failed");
    }

    @Override
    public void onTimerFinished(View view) {
        mDelayedView.setListener(null);
        if (needToFinish)
            return;

        sendMessage();
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Panic alert sent, Help is on the way");
        startActivity(intent);
        needToFinish = true;
    }

    private boolean needToFinish = false;

    @Override
    public void onTimerSelected(View view) {
        mDelayedView.setListener(null);
        mDelayedView.reset();
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Panic alert cancelled, Please be safe always");
        startActivity(intent);
        needToFinish = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alertButton != null)
            alertButton.setVisibility(View.GONE);
        if (progressLayout != null)
            progressLayout.setVisibility(View.VISIBLE);
        if (needToFinish)
            finish();
    }
}